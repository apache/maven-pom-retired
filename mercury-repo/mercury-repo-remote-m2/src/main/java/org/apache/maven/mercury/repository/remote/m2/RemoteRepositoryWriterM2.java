package org.apache.maven.mercury.repository.remote.m2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.Snapshot;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployRequest;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.spi.http.client.deploy.DeployResponse;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

public class RemoteRepositoryWriterM2
implements RepositoryWriter
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( RemoteRepositoryWriterM2.class ); 
  private static final Language _lang = new DefaultLanguage( RemoteRepositoryWriterM2.class );
  //---------------------------------------------------------------------------------------------------------------
  private static final String [] _protocols = new String [] { "http", "dav", "webdav" };
  
  RemoteRepository _repo;
  Server _server;
  RepositoryReader _reader;
  
  // TODO og: 2008-08-22 should be replaced with real transport
  DefaultDeployer _transport;
  //---------------------------------------------------------------------------------------------------------------
  public RemoteRepositoryWriterM2( RemoteRepository repo )
  throws RepositoryException
  {
    if( repo == null )
      throw new IllegalArgumentException("localRepo cannot be null");
    
    _server = repo.getServer();
    if( _server == null )
      throw new IllegalArgumentException( _lang.getMessage( "bad.repository.server.null" ) );
    
    if( _server.getURL() == null )
      throw new IllegalArgumentException(_lang.getMessage( "bad.repository.server.url.null" ));

    _repo = repo;
    
    _reader = _repo.getReader( 
        new MetadataProcessor() {

      public List<ArtifactBasicMetadata> getDependencies( ArtifactBasicMetadata bmd, MetadataReader mdReader, Hashtable env )
      throws MetadataReaderException
      {
        return null;
      }
                                }
                              );

    try
    {
      _transport = new DefaultDeployer();
      HashSet<Server> servers = new HashSet<Server>(1);
      servers.add( _server );
      _transport.setServers( servers );
    }
    catch( HttpClientException e )
    {
      throw new RepositoryException(e);
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public Repository getRepository()
  {
    return _repo;
  }
  //---------------------------------------------------------------------------------------------------------------
  public boolean canHandle( String protocol )
  {
    return AbstractRepository.DEFAULT_LOCAL_READ_PROTOCOL.equals( protocol );
  }
  //---------------------------------------------------------------------------------------------------------------
  public String[] getProtocols()
  {
    return _protocols;
  }
  //---------------------------------------------------------------------------------------------------------------
  public void close()
  {
  }
  //---------------------------------------------------------------------------------------------------------------
  public void writeArtifact( Collection<Artifact> artifacts )
      throws RepositoryException
  {
    if( artifacts == null || artifacts.size() < 1 )
      return;
    
    Set<StreamVerifierFactory> vFacs = null;
    Server server = _repo.getServer();
    if( server != null && server.hasWriterStreamVerifierFactories() )
      vFacs = server.getWriterStreamVerifierFactories();
    
    if( vFacs == null ) // let it be empty, but not null
      vFacs = new HashSet<StreamVerifierFactory>(1);
      
    for( Artifact artifact : artifacts )
    {
      writeArtifact( artifact, vFacs );
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public void writeArtifact( Artifact artifact, Set<StreamVerifierFactory> vFacs )
  throws RepositoryException
  {
    if( artifact == null )
      throw new RepositoryException( _lang.getMessage( "null.artifact") );
    
    if( artifact.getFile() == null || !artifact.getFile().exists() )
      throw new RepositoryException( _lang.getMessage( "bad.artifact.file", artifact.toString(), (artifact.getFile() == null ? "null" : artifact.getFile().getAbsolutePath()) ) );
    
    boolean isPom = "pom".equals( artifact.getType() );
    
    byte [] pomBlob = artifact.getPomBlob();
    boolean hasPomBlob = pomBlob != null && pomBlob.length > 0;
    
    if( !artifact.hasClassifier() && !hasPomBlob )
      throw new RepositoryException( _lang.getMessage( "no.pom.in.primary.artifact", artifact.toString() ) );
    
    InputStream in = artifact.getStream();
    if( in == null )
    {
      File aFile = artifact.getFile();
      if( aFile == null && !isPom )
      {
        throw new RepositoryException( _lang.getMessage( "artifact.no.stream", artifact.toString() ) );
      }

      try
      {
        in = new FileInputStream( aFile );
      }
      catch( FileNotFoundException e )
      {
        if( !isPom )
          throw new RepositoryException( _lang.getMessage( "artifact.no.file", artifact.toString(), aFile.getAbsolutePath(), e.getMessage() ) );
      }
    }
    DefaultArtifactVersion dav = new DefaultArtifactVersion( artifact.getVersion() );
    Quality aq = dav.getQuality();
    boolean isSnapshot = aq.equals( Quality.SNAPSHOT_QUALITY ) || aq.equals( Quality.SNAPSHOT_TS_QUALITY );

    String relGroupPath = artifact.getGroupId().replace( '.', '/' )+"/"+artifact.getArtifactId();
    String relVersionPath = relGroupPath + '/' + (isSnapshot ? (dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION) : artifact.getVersion() );

    try
    {
      if( isPom )
      {
        if( in == null && !hasPomBlob )
          throw new RepositoryException( _lang.getMessage( "pom.artifact.no.stream", artifact.toString() ) );
        
        if( in != null )
        {
          byte [] pomBlobBytes = FileUtil.readRawData( in );
          hasPomBlob = pomBlobBytes != null && pomBlobBytes.length > 0;
          if( hasPomBlob )
            pomBlob = pomBlobBytes;
        }
          
      }
      
      String url = _server.getURL().toString(); 
      
      // read metadata
      String gaMdUrl    = url+'/'+relGroupPath+'/'+_repo.getMetadataName();
      byte [] gaMdBytes = _reader.readRawData( gaMdUrl );
      
      String gavMdUrl = url+'/'+relVersionPath+'/'+_repo.getMetadataName();
      byte [] gavMdBytes = _reader.readRawData( gavMdUrl );

      HashSet<Binding> bindings = new HashSet<Binding>(4);
      
      // first - take care of the binary
      String binUrl = url+'/'+relVersionPath+'/'+artifact.getBaseName()+'.'+artifact.getType();
      bindings.add( new Binding( new URL(binUrl), artifact.getFile() ) );
      
      // GA metadata
      Metadata md = gaMdBytes == null ? null :  MetadataBuilder.getMetadata( gaMdBytes );
      
      if( md == null )
      {
        md = new Metadata();
        md.setGroupId( artifact.getGroupId() );
        md.setArtifactId( artifact.getArtifactId() );
      }
      
      MetadataOperation mdOp = null;
      
      if( isSnapshot )
      {
        Snapshot sn = MetadataBuilder.createSnapshot( artifact.getVersion() );
        sn.setLocalCopy( true );
        mdOp = new SetSnapshotOperation( new SnapshotOperand(sn) );
      }
      else
        mdOp = new AddVersionOperation( new StringOperand(artifact.getVersion()) ); 
      
      byte [] gaResBytes = MetadataBuilder.changeMetadata( md, mdOp );
      bindings.add( new Binding(new URL(gaMdUrl), new ByteArrayInputStream(gaResBytes)) );

      // now - GAV metadata
      md = gavMdBytes == null ? null : MetadataBuilder.getMetadata( gavMdBytes );
      
      if( md == null )
      {
        md = new Metadata();
        md.setGroupId( artifact.getGroupId() );
        md.setArtifactId( artifact.getArtifactId() );
        md.setVersion( artifact.getVersion() );
      }
      
      byte [] gavResBytes = MetadataBuilder.changeMetadata( md, mdOp );
      bindings.add( new Binding( new URL(gavMdUrl), new ByteArrayInputStream(gavResBytes)) );
      
      if( !isPom && hasPomBlob )
      {
        String pomUrl = url+'/'+relVersionPath+'/'+artifact.getArtifactId()+'-'+artifact.getVersion()+".pom";
        bindings.add( new Binding( new URL(pomUrl), new ByteArrayInputStream(pomBlob) ) );
      }
      
      DefaultDeployRequest request = new DefaultDeployRequest();
      request.setBindings( bindings );
      
      DeployResponse response = _transport.deploy( request );
      
      if( response.hasExceptions() )
        throw new RepositoryException( response.getExceptions().toString() );
        
    }
    catch( Exception e )
    {
      throw new RepositoryException( e );
    }
    
  }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
}
