package org.apache.maven.mercury.repository.remote.m2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.builder.api.MetadataProcessingException;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.repository.api.AbstracRepositoryReader;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.transport.api.Binding;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
/**
 * implementation of M2 remote repository reader. Actual Transport (protocol, URL) come from RemoteRepository Server URL
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
  private DefaultRetriever _transport;
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
  public RepositoryOperationResult<DefaultArtifact> readArtifacts( List<? extends ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    // TODO Auto-generated method stub
    return null;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public Map<ArtifactBasicMetadata, ArtifactMetadata> readDependencies( List<? extends ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;

    Map<ArtifactBasicMetadata, ArtifactMetadata> ror = new HashMap<ArtifactBasicMetadata, ArtifactMetadata>(16);
    
    for( ArtifactBasicMetadata bmd : query )
    {
      try
      {
        List<ArtifactBasicMetadata> deps = _mdProcessor.getDependencies( bmd, this, System.getProperties() );
        ArtifactMetadata md = new ArtifactMetadata( bmd );
        md.setDependencies( deps );
        
        ror.put( bmd, md );
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
  public Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> readVersions( List<? extends ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;

    Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> res = new HashMap<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>>( query.size() );
    
    String gaPath = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      gaPath = bmd.getGroupId().replace( '.', '/' )+'/'+bmd.getArtifactId()+"/maven-metadata.xml";
      
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

      RepositoryOperationResult<ArtifactBasicMetadata> rr = null;
      VersionRange versionQuery;
      try
      {
        versionQuery = new VersionRange( bmd.getVersion() );
      }
      catch( VersionException e )
      {
        rr = RepositoryOperationResult.add( rr, new RepositoryException(e) );
        continue;
      }
      
      for( Object vo : mmd.getVersioning().getVersions() )
      {
        if( vo == null || !(vo instanceof String) )
          continue;
        
        String v = (String)vo;
        
        if( !versionQuery.includes(  v )  )
          continue;
        
        ArtifactBasicMetadata vmd = new ArtifactBasicMetadata();
        vmd.setGroupId( bmd.getGroupId() );
        vmd.setArtifactId(  bmd.getArtifactId() );
        vmd.setClassifier( bmd.getClassifier() );
        vmd.setType( bmd.getType() );
        vmd.setVersion( v );
        
        rr = RepositoryOperationResult.add( rr, vmd );
      }
      if( rr != null )
        res.put( bmd, rr );
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
    File tempFile = null;
    try
    {
      // transport workaround - until it can do in-memory Bindings
      tempFile = File.createTempFile( "mercury", "readraw" );
      
      String separator = "/";
      if( path.startsWith( separator ))
        separator = "";
      
      Binding binding = new Binding( new URL(_repo.getServer().getURL().toString() + separator + path) , tempFile );
      DefaultRetrievalRequest request = new DefaultRetrievalRequest();
      request.addBinding( binding );
      
      RetrievalResponse response = _transport.retrieve( request );
      
      if( response.hasExceptions() )
      {
        throw new MetadataProcessingException( response.getExceptions().toString() );
      }
    
      fis = new FileInputStream( tempFile );
      int len = (int)tempFile.length();
      byte [] pom = new byte [ len ];
      fis.read( pom );
      return pom;
    }
    catch( IOException e )
    {
      throw new MetadataProcessingException(e);
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
      if( tempFile != null ) try { if(tempFile.exists()) tempFile.delete(); } catch( Exception any ) {}
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
