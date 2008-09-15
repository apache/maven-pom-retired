package org.apache.maven.mercury.repository.remote.m2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.repository.api.AbstracRepositoryReader;
import org.apache.maven.mercury.repository.api.AbstractRepOpResult;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.local.m2.ArtifactLocation;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;
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
  
  List<LocalRepository> _localRepos;
  File _defaultRoot;
  //---------------------------------------------------------------------------------------------------------------
  public RemoteRepositoryReaderM2( RemoteRepository repo, DependencyProcessor mdProcessor )
  throws RepositoryException
  {
    this( repo, mdProcessor, null );
  }
  //---------------------------------------------------------------------------------------------------------------
  public RemoteRepositoryReaderM2( RemoteRepository repo, DependencyProcessor mdProcessor, List<LocalRepository> localRepos )
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
      File temp = File.createTempFile( "temp-", "-locator" );
      _defaultRoot = new File( temp.getParentFile(), "repo" );
      _defaultRoot.mkdirs();
      _log.info( "temporary repository  folder set to "+_defaultRoot.getCanonicalPath() );
    }
    catch( IOException e )
    {
      throw new RepositoryException(e);
    }

    if( localRepos == null || localRepos.isEmpty() )
    {
      _localRepos = new ArrayList<LocalRepository>(1);
      _localRepos.add( new LocalRepositoryM2("temp", _defaultRoot ) );
    }
    else
      _localRepos = localRepos;

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
  private final ArtifactLocation calculateLocation( String root, ArtifactBasicMetadata bmd, AbstractRepOpResult res ) throws RepositoryException, MetadataReaderException, MetadataException
  {
    ArtifactLocation loc = new ArtifactLocation( root, bmd );

    byte [] mdBytes = readRawData( loc.getGaPath()+loc.SEP+ _repo.getMetadataName() );
    if( mdBytes == null )
      throw new RepositoryException( _lang.getMessage( "no.group.md", _repo.getServer().getURL().toString(), loc.getGaPath() ) );
      
    Metadata groupMd = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    if( groupMd == null || groupMd.getVersioning() == null )
      throw new RepositoryException( _lang.getMessage( "group.md.no.versions", _repo.getServer().getURL().toString(), loc.getGaPath() ) );
      
    List<String> versions = groupMd.getVersioning().getVersions();
    if( versions == null || versions.size() < 1 )
      throw new RepositoryException( _lang.getMessage( "group.md.no.versions", _repo.getServer().getURL().toString(), loc.getGaPath() ) );

    Quality vq = new Quality( loc.getVersion() );
    
    // RELEASE = LATEST - SNAPSHOTs
    if( Artifact.RELEASE_VERSION.equals( loc.getVersion() )
        ||
        Artifact.LATEST_VERSION.equals( loc.getVersion() ) 
      )
    {
      boolean noSnapshots = Artifact.RELEASE_VERSION.equals( loc.getVersion() );
      String ver = VersionRange.findLatest( versions, noSnapshots );

      if( ver == null )
      {
        res.addError( bmd, new RepositoryException( _lang.getMessage( "gav.not.found", bmd.toString(), loc.getGaPath() ) ) );
        return null;
      }
      
      loc.setVersion( ver );
      
      // LATEST is a SNAPSHOT :(
      if( loc.getVersion().endsWith( Artifact.SNAPSHOT_VERSION ) )
      {
        loc.setVersionDir( loc.getVersion() );

        if( !findLatestSnapshot( bmd, loc, res ) )
          return null;
      }
      else // R or L found and actual captured in loc.version
        loc.setVersionDir( loc.getVersion() );
    }
    // regular snapshot requested
    else if( loc.getVersion().endsWith( Artifact.SNAPSHOT_VERSION ) )
    {
      if( !versions.contains( loc.getVersion() ) && !findLatestSnapshot( bmd, loc, res ) )
        return null;
    }
    // time stamped snapshot requested
    else if( vq.equals( Quality.SNAPSHOT_TS_QUALITY ))
    {
      loc.setVersionDir( loc.getBaseVersion()+loc.DASH+Artifact.SNAPSHOT_VERSION );
    }
    
    return loc;
  }
  //---------------------------------------------------------------------------------------------------------------
  private boolean findLatestSnapshot( ArtifactBasicMetadata bmd, ArtifactLocation loc, AbstractRepOpResult res )
  throws MetadataReaderException, MetadataException, RemoteRepositoryM2OperationException
  {
    DefaultArtifactVersion dav = new DefaultArtifactVersion( loc.getVersion() );
    
    String mdPath = loc.getGavPath()+'/'+_repo.getMetadataName();

    byte [] mdBytes = readRawData( mdPath );
    if( mdBytes == null )
    {
      res.addError( bmd, new RepositoryException( _lang.getMessage( "no.gav.md", _repo.getServer().getURL().toString(), mdPath ) ) );
      return false;
    }
      
    Metadata gavMd = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    if( gavMd == null || gavMd.getVersioning() == null )
    {
      res.addError( bmd, new RepositoryException( _lang.getMessage( "gav.md.no.versions", _repo.getServer().getURL().toString(), mdPath ) ) );
      return false;
    }
      
    List<String> versions = gavMd.getVersioning().getVersions();
    if( versions == null || versions.size() < 1 )
    {
      res.addError( bmd, new RepositoryException( _lang.getMessage( "gav.md.no.versions", _repo.getServer().getURL().toString(), mdPath ) ) );
      return false;
    }

    // version-SNAPSHOT or exact TS exists?
    if( versions.contains( loc.getVersion() ) )
    {
      return true;
    }
    
    // nothing to do, but find latest
    String ver = VersionRange.findLatest( versions, true );

    if( ver == null )
    {
      res.addError( bmd, new RepositoryException( _lang.getMessage( "snapshot.not.found", _repo.getServer().getURL().toString(), mdPath ) ) );
      return false;
    }
    
    loc.setVersion( ver );
    return true;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * TODO og: parallelize as soon as code stabilizes
   */
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
        readArtifact( bmd, res );
      }
      catch( Exception e )
      {
        res.addError( bmd, e );
      }
    }

    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  private File findLocalRoot( Quality vq )
  {
    for( LocalRepository lr : _localRepos )
      if( !lr.isReadOnly() && lr.getVersionRangeQualityRange().isAcceptedQuality( vq ) )
        return lr.getDirectory();

    return _defaultRoot;
  }
  //---------------------------------------------------------------------------------------------------------------
  public void readArtifact( ArtifactBasicMetadata bmd, ArtifactResults res )
  throws IOException, RepositoryException, MetadataReaderException, MetadataException
  {
    DefaultArtifact da = bmd instanceof DefaultArtifact ? (DefaultArtifact)bmd : new DefaultArtifact( bmd );
    
    ArtifactLocation loc = calculateLocation( _repo.getServer().getURL().toString(), bmd, res );
    
    if( loc == null )
      return;
    
    da.setVersion( loc.getVersion() );

    Quality vq = new Quality( loc.getVersion() );
    
    File root = findLocalRoot( vq );
    
    File binFile = new File( root, loc.getRelPath() ); 
    File pomFile = null; 
    
    DefaultRetrievalRequest drr = new DefaultRetrievalRequest();
    
    Binding binBinding = new Binding( new URL(loc.getAbsPath()), binFile );
    drr.addBinding( binBinding );

    boolean isPom = "pom".equals( bmd.getType() ); 
    if( ! isPom ) 
    {
      pomFile = new File( root, loc.getRelPomPath() );
      Binding pomBinding = new Binding( new URL(loc.getAbsPomPath()), pomFile );
      drr.addBinding( pomBinding );
    }
    
    RetrievalResponse resp = _transport.retrieve( drr );
    
    if( resp.hasExceptions() )
    {
      res.addError( bmd, new RepositoryException(resp.getExceptions().toString() ) );
    }
    else
    {
      da.setFile( binFile );
      da.setPomBlob(  FileUtil.readRawData( isPom ? binFile : pomFile ) );
      res.add( bmd, da );
    }
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
      catch( MetadataReaderException e )
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
    
    String root = _repo.getServer().getURL().toString();
    
    for( ArtifactBasicMetadata bmd : query )
    {
      ArtifactLocation loc = new ArtifactLocation( root, bmd );
      
      byte[] mavenMetadata;
      try
      {
        mavenMetadata = readRawData( loc.getGaPath()+loc.SEP+_repo.getMetadataName() );
      }
      catch( MetadataReaderException e )
      {
        continue;
      }
      
      if( mavenMetadata == null )
        continue;
      
      Metadata mmd;
      try
      {
        mmd = MetadataBuilder.getMetadata( mavenMetadata );
      }
      catch( MetadataException e1 )
      {
        res.addError( bmd, e1 );
        continue;
      }

      if( mmd == null || mmd.getVersioning() == null )
      {
        _log.warn( _lang.getMessage( "maven.bad.metadata", loc.getGaPath()+loc.SEP+_repo.getMetadataName(), _repo.getId() ) );
        continue;
      }
      
      List<String> versions = mmd.getVersioning().getVersions(); 

      if( mmd == null || mmd.getVersioning() == null )
      {
        _log.warn( _lang.getMessage( "maven.metadata.no.versions", loc.getGaPath()+loc.SEP+_repo.getMetadataName(), _repo.getId() ) );
        continue;
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

      Quality vq = new Quality( bmd.getVersion() );
      
      if(    vq.equals( Quality.FIXED_RELEASE_QUALITY ) 
          || vq.equals( Quality.FIXED_LATEST_QUALITY )
          || vq.equals( Quality.SNAPSHOT_QUALITY     )
      )
      {
        try
        {
          loc = calculateLocation( root, bmd, res );
        }
        catch( Exception e )
        {
          res.addError( bmd, e );
          continue;
        }
        
        if( loc == null )
          continue;
          
        ArtifactBasicMetadata vmd = new ArtifactBasicMetadata();
        vmd.setGroupId( bmd.getGroupId() );
        vmd.setArtifactId(  bmd.getArtifactId() );
        vmd.setClassifier( bmd.getClassifier() );
        vmd.setType( bmd.getType() );
        vmd.setVersion( loc.getVersion() );
          
        res = ArtifactBasicResults.add( res, bmd, vmd );
        
        continue;

      }
      
      for( String version : versions )
      {
        Quality q = new Quality( version );

        if( ! _repo.isAcceptedQuality( q ) )
          continue;
        
        if( !versionQuery.includes(  version )  )
          continue;
        
        ArtifactBasicMetadata vmd = new ArtifactBasicMetadata();
        vmd.setGroupId( bmd.getGroupId() );
        vmd.setArtifactId(  bmd.getArtifactId() );
        vmd.setClassifier( bmd.getClassifier() );
        vmd.setType( bmd.getType() );
        vmd.setVersion( version );
        
        res = ArtifactBasicResults.add( res, bmd, vmd );
      }
      
    }
    
    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  public byte[] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataReaderException
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
  throws MetadataReaderException
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
      
      String url = (path.startsWith( "http" ) ? "" : _repo.getServer().getURL().toString() + separator) + path;
      
      Binding binding = new Binding( new URL( url ) , baos );
      DefaultRetrievalRequest request = new DefaultRetrievalRequest();
      request.addBinding( binding );
      
      RetrievalResponse response = _transport.retrieve( request );
      
      if( response.hasExceptions() )
      {
       _log.info(  _lang.getMessage( "read.raw.exceptions", path, response.getExceptions().toString() ) );
        return null;
      }
      
      return baos.toByteArray();
    }
    catch( IOException e )
    {
      throw new MetadataReaderException(e);
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
