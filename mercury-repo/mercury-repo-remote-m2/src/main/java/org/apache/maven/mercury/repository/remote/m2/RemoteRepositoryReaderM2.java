package org.apache.maven.mercury.repository.remote.m2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.builder.api.MetadataProcessingException;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.repository.api.AbstracRepositoryReader;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.AbstractRepOpResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.transport.api.TransportTransaction;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
/**
 * implementation of M2 remote repository reader. Actual Transport (protocol, URL) [should] come from RemoteRepository Server URL
 * 
 *  Current implementation does not do the check and uses jetty-client directly. 
 *  TODO - re-implements after jetty-client implements ReaderTransport 
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RemoteRepositoryReaderM2
extends AbstracRepositoryReader
implements RepositoryReader, MetadataReader
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( RemoteRepositoryReaderM2.class ); 
  private static final Language _lang = new DefaultLanguage( RemoteRepositoryReaderM2.class );
  // TODO - replace with known Transport's protocols. Should be similar to RepositoryReader/Writer registration
  private static final String [] _protocols = new String [] { "http", "https", "dav", "webdav" };
  
  // TODO replace with Transport
  DefaultRetriever _transport;
  //---------------------------------------------------------------------------------------------------------------
  RemoteRepository _repo;
  //---------------------------------------------------------------------------------------------------------------
  public RemoteRepositoryReaderM2( RemoteRepository repo, MetadataProcessor mdProcessor )
  throws RepositoryException
  {
    
    if( repo == null )
      throw new IllegalArgumentException( _lang.getMessage( "bad.repository.null") );
    
    if( repo.getServer() == null )
      throw new IllegalArgumentException( _lang.getMessage( "bad.repository.server.null") );
    
    if( repo.getServer().getURL() == null )
      throw new IllegalArgumentException( _lang.getMessage( "bad.repository.server.url.null") );
    
    _repo = repo;
    
    if( mdProcessor == null )
      throw new IllegalArgumentException("MetadataProcessor cannot be null ");
    
    setMetadataProcessor(  mdProcessor );
    
    try
    {
      // TODO 2008-07-29 og: here I should analyze Server protocol
      //                     and come with appropriate Transport implementation 
      _transport = new DefaultRetriever();
      HashSet<Server> servers = new HashSet<Server>(1);
      servers.add( repo.getServer() );
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
  public ArtifactResults readArtifacts( List<ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;
    
    ArtifactResults res = new ArtifactResults();
    
    for( ArtifactBasicMetadata bmd : query )
    {
      try
      {
        DefaultArtifact da = readArtifact( bmd );
        res.add( bmd, da );
      }
      catch( Exception e )
      {
        res.addError( bmd, e );
      }
    }

    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  private String findLatestSnapshot( String gavPath, String version, DefaultArtifact da )
  throws RepositoryException, MetadataProcessingException, MetadataException
  {
    String ver = version;
    DefaultArtifactVersion dav = new DefaultArtifactVersion( ver );

    byte [] mdBytes = readRawData( gavPath+'/'+ver+'/'+_repo.getMetadataName() );
    if( mdBytes == null )
      throw new RepositoryException( _lang.getMessage( "no.gav.md", _repo.getServer().getURL().toString(), gavPath+'/'+ver+'/'+_repo.getMetadataName() ) );
      
    Metadata gavMd = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    if( gavMd == null || gavMd.getVersioning() == null )
      throw new RepositoryException( _lang.getMessage( "gav.md.no.versions", _repo.getServer().getURL().toString(), gavPath+'/'+ver+'/'+_repo.getMetadataName() ) );
      
    List<String> versions = gavMd.getVersioning().getVersions();
    if( versions == null || versions.size() < 1 )
      throw new RepositoryException( _lang.getMessage( "gav.md.no.versions", _repo.getServer().getURL().toString(), gavPath+'/'+ver+'/'+_repo.getMetadataName() ) );

    if( versions.contains( ver ) )
    {
      da.setVersion( ver );
      
      return gavPath+'/'+ dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION
              + '/'+da.getArtifactId()+'-'+dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION
              + ( da.hasClassifier() ? '-'+da.getClassifier() : "" )
              + '.'+da.getType()
      ;
    }
    
    ver = null;
    DefaultArtifactVersion tempDav = null;
    DefaultArtifactVersion tempDav2 = null;
    
    // find latest
    for( String vn : versions )
    {
      // no snapshots any more
      if( vn.endsWith( Artifact.SNAPSHOT_VERSION ))
        continue;

      if( ver == null )
      {
        ver = vn;
        tempDav = new DefaultArtifactVersion( vn );
        continue;
      }
      
      tempDav2 = new DefaultArtifactVersion( vn );
      if( tempDav2.compareTo( tempDav ) > 0 )
      {
        ver = vn;
        tempDav = tempDav2;
      }
    
    }

    if( ver == null )
    {
      throw new RepositoryException( _lang.getMessage( "snapshot.not.found", _repo.getServer().getURL().toString(), gavPath+'/'+ver+'/'+_repo.getMetadataName() ) );
    }
    
    da.setVersion( ver );
    
    return gavPath + '/' + dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION
    + '/'+da.getArtifactId()+'-'+ver
    + ( da.hasClassifier() ? '-'+da.getClassifier() : "" )
    + '.'+da.getType()
;
  }
  //---------------------------------------------------------------------------------------------------------------
  public DefaultArtifact readArtifact( ArtifactBasicMetadata bmd )
  throws IOException, RepositoryException, MetadataProcessingException, MetadataException
  {
    DefaultArtifact da = bmd instanceof DefaultArtifact ? (DefaultArtifact)bmd : new DefaultArtifact( bmd );
    
    String version = bmd.getVersion();
    DefaultArtifactVersion dav = new DefaultArtifactVersion( version );
    Quality vq = dav.getQuality();
    
    String relGaPath = bmd.getGroupId().replace( '.', '/' ) + '/' + bmd.getArtifactId();
    String versionDir = bmd.getVersion();

    byte [] mdBytes = readRawData( relGaPath+'/'+_repo.getMetadataName() );
    if( mdBytes == null )
      throw new RepositoryException( _lang.getMessage( "no.group.md", _repo.getServer().getURL().toString(), relGaPath ) );
      
    Metadata groupMd = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    if( groupMd == null || groupMd.getVersioning() == null )
      throw new RepositoryException( _lang.getMessage( "group.md.no.versions", _repo.getServer().getURL().toString(), relGaPath ) );
      
    List<String> versions = groupMd.getVersioning().getVersions();
    if( versions == null || versions.size() < 1 )
      throw new RepositoryException( _lang.getMessage( "group.md.no.versions", _repo.getServer().getURL().toString(), relGaPath ) );
    
    String relBinaryPath = null;
    
    // merge RELEASE and LATEST, should be no difference
    if( Artifact.RELEASE_VERSION.equals( version )
        ||
        Artifact.LATEST_VERSION.equals( version ) 
      )
    {
      boolean noSnapshots = Artifact.RELEASE_VERSION.equals( version );
      version = null;
      DefaultArtifactVersion tempDav = null;
      DefaultArtifactVersion tempDav2 = null;

      // find latest
      for( String vn : versions )
      {
        // RELEASE?
        if( noSnapshots && vn.endsWith( Artifact.SNAPSHOT_VERSION ))
          continue;
        
        if( version == null )
        {
          version = vn;
          tempDav = new DefaultArtifactVersion( vn );
          continue;
        }
        
        tempDav2 = new DefaultArtifactVersion( vn );
        if( tempDav2.compareTo( tempDav ) > 0 )
        {
          version = vn;
          tempDav = tempDav2;
        }
        
      }

      if( version == null )
        throw new RepositoryException( _lang.getMessage( "gav.not.found", bmd.toString(), relGaPath ) );
      
      // LATEST is a SNAPSHOT :(
      if( version.endsWith( Artifact.SNAPSHOT_VERSION ) )
      {
        relBinaryPath = findLatestSnapshot( relGaPath, version, da );

        if( relBinaryPath == null )
          throw new RepositoryException( _lang.getMessage( "gav.not.found", bmd.toString(), relGaPath ) );

        versionDir = version;
      }
      else
      {
        versionDir = version;
        relBinaryPath = relGaPath+'/'+versionDir
            +'/'+bmd.getArtifactId()+'-'+version
            + ( bmd.hasClassifier() ? '-'+bmd.getClassifier() : "" )+'.'+bmd.getType()
        ;

        da.setVersion( version );
      }
    }
    // regular snapshot requested
    else if( version.endsWith( Artifact.SNAPSHOT_VERSION ) )
    {
      relBinaryPath = findLatestSnapshot( relGaPath, version, da );
      versionDir = dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION;

      if( relBinaryPath == null )
        throw new RepositoryException( _lang.getMessage( "gav.not.found", bmd.toString(), relGaPath ) );
    }
    // time stamped snapshot requested
    else if( vq.equals( Quality.SNAPSHOT_TS_QUALITY ))
    {
      versionDir = dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION;
      relBinaryPath = relGaPath + '/' + versionDir 
          + '/' + bmd.getArtifactId() + '-' + bmd.getVersion() + ( bmd.hasClassifier() ? '-'+bmd.getClassifier() : "" ) 
          + '.' + bmd.getType()
      ;
    }
    else
    {
      relBinaryPath = relGaPath + '/'+version
                      + '/'+bmd.getArtifactId()+'-'+version
                      + ( bmd.hasClassifier() ? '-'+bmd.getClassifier() : "" )
                      + '.'+bmd.getType()
      ;
    }
    
    // binary calculated 
    
    File binFile = File.createTempFile( "remote-repo-" + _repo.getId(), bmd.getArtifactId()+bmd.getVersion()+'.'+bmd.getType() );
    File pomFile = null;

    Binding binBinding = new Binding( new URL(_repo.getServer().getURL().toString() + '/'+ relBinaryPath) , binFile );

    DefaultRetrievalRequest request = new DefaultRetrievalRequest();
    request.addBinding( binBinding );
    
    if( !"pom".equals( bmd.getType() ) ) 
    {
      String relPomPath = relGaPath + '/' + versionDir
                          + '/'+bmd.getArtifactId()+'-'+version
                          + ".pom"
      ;

      pomFile = File.createTempFile( "remote-repo-" + _repo.getId(), bmd.getArtifactId()+bmd.getVersion()+".pom" );
      Binding pomBinding = new Binding( new URL(_repo.getServer().getURL().toString() + '/'+ relPomPath) , pomFile );
      request.addBinding( pomBinding );
    }
    
    RetrievalResponse response = _transport.retrieve( request );
    
    if( response.hasExceptions() )
    {
      throw new RepositoryException( response.getExceptions().toString() );
    }
    
    da.setFile( binFile );

    if( pomFile != null ) 
    {
      da.setPomBlob( FileUtil.readRawData( pomFile ) );
    }
    else
    {
      da.setPomBlob( FileUtil.readRawData( binFile ) );
    }

    return da;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public ArtifactBasicResults readDependencies( List<ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;

    ArtifactBasicResults ror = new ArtifactBasicResults(16);
    
    for( ArtifactBasicMetadata bmd : query )
    {
      try
      {
        List<ArtifactBasicMetadata> deps = _mdProcessor.getDependencies( bmd, this, System.getProperties() );
        ror.add( bmd, deps );
      }
      catch( MetadataProcessingException e )
      {
        _log.warn( "error reading "+bmd.toString()+" dependencies", e );
        continue;
      }
      
    }
    
    return ror;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * direct disk search, no redirects, first attempt
   */
  public ArtifactBasicResults readVersions( List<ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;

    ArtifactBasicResults res = new ArtifactBasicResults( query.size() );
    
    String gaPath = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      gaPath = bmd.getGroupId().replace( '.', '/' )+'/'+bmd.getArtifactId()+'/'+_repo.getMetadataName();
      
      byte[] mavenMetadata;
      try
      {
        mavenMetadata = readRawData( gaPath );
      }
      catch( MetadataProcessingException e )
      {
        continue;
      }
      
      if( mavenMetadata == null )
        continue;
      
      MetadataXpp3Reader mmReader = new MetadataXpp3Reader();
      Metadata mmd;
      try
      {
        mmd = mmReader.read( new ByteArrayInputStream(mavenMetadata) );
      }
      catch( IOException e )
      {
        _log.warn( _lang.getMessage( "maven.metadata.xml.exception", e.getMessage(), gaPath, _repo.getId() ) );
        continue;
      }
      catch( XmlPullParserException pe )
      {
        _log.error( pe.getMessage() );
        throw new RepositoryException( pe );
      }

      VersionRange versionQuery;
      try
      {
        versionQuery = new VersionRange( bmd.getVersion(), _repo.getVersionRangeQualityRange() );
      }
      catch( VersionException e )
      {
        res.addError( bmd, new RepositoryException(e) );
        continue;
      }
      
      for( Object vo : mmd.getVersioning().getVersions() )
      {
        if( vo == null || !(vo instanceof String) )
          continue;
        
        String v = (String)vo;
        
        Quality q = new Quality( v );
        if( ! _repo.isAcceptedQuality( q ) )
          continue;
        
        if( !versionQuery.includes(  v )  )
          continue;
        
        ArtifactBasicMetadata vmd = new ArtifactBasicMetadata();
        vmd.setGroupId( bmd.getGroupId() );
        vmd.setArtifactId(  bmd.getArtifactId() );
        vmd.setClassifier( bmd.getClassifier() );
        vmd.setType( bmd.getType() );
        vmd.setVersion( v );
        
        res.add( bmd, vmd );
      }
    }
    
    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  public byte[] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataProcessingException
  {
    String bmdPath = bmd.getGroupId().replace( '.', '/' )
                    + '/'+bmd.getArtifactId()
                    + '/'+bmd.getVersion()
                    + '/'+bmd.getBaseName(classifier)
                    + '.' + (type == null ? bmd.getType() : type )
                    ;
    
    return readRawData( bmdPath );
  }
  //---------------------------------------------------------------------------------------------------------------
  public byte[] readRawData( String path )
  throws MetadataProcessingException
  {
    if( path == null || path.length() < 1 )
      return null;
    
    FileInputStream fis = null;
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
      
      String separator = "/";
      if( path.startsWith( separator ))
        separator = "";
      
      Binding binding = new Binding( new URL(_repo.getServer().getURL().toString() + separator + path) , baos );
      DefaultRetrievalRequest request = new DefaultRetrievalRequest();
      request.addBinding( binding );
      
      RetrievalResponse response = _transport.retrieve( request );
      
      if( response.hasExceptions() )
        throw new MetadataProcessingException( response.getExceptions().toString() );
      
      return baos.toByteArray();
    }
    catch( IOException e )
    {
      throw new MetadataProcessingException(e);
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public boolean canHandle( String protocol )
  {
    return AbstractRepository.DEFAULT_REMOTE_READ_PROTOCOL.equals( protocol );
  }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
  public void close()
  {
    // TODO Auto-generated method stub
    
  }
  public String[] getProtocols()
  {
    return _protocols;
  }
}
