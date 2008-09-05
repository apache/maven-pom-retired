package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.repository.api.AbstracRepositoryReader;
import org.apache.maven.mercury.repository.api.AbstractRepOpResult;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

public class LocalRepositoryReaderM2
extends AbstracRepositoryReader
implements RepositoryReader, MetadataReader
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( LocalRepositoryReaderM2.class ); 
  private static final Language _lang = new DefaultLanguage( LocalRepositoryReaderM2.class );
  //---------------------------------------------------------------------------------------------------------------
  private static final String [] _protocols = new String [] { "file" };
  
  LocalRepository _repo;
  File _repoDir;
  //---------------------------------------------------------------------------------------------------------------
  public LocalRepositoryReaderM2( LocalRepository repo, MetadataProcessor mdProcessor )
  {
    if( repo == null )
      throw new IllegalArgumentException("localRepo cannot be null");
    
    _repoDir = repo.getDirectory();
    if( _repoDir == null )
      throw new IllegalArgumentException("localRepo directory cannot be null");
    
    if( !_repoDir.exists() )
      throw new IllegalArgumentException("localRepo directory \""+_repoDir.getAbsolutePath()+"\" should exist");

    _repo = repo;
    
    if( mdProcessor == null )
      throw new IllegalArgumentException("MetadataProcessor cannot be null ");
    
    setMetadataProcessor(  mdProcessor );
  }
  //---------------------------------------------------------------------------------------------------------------
  public Repository getRepository()
  {
    return _repo;
  }
  //---------------------------------------------------------------------------------------------------------------
  private static ArtifactLocation calculateLocation( String root, ArtifactBasicMetadata bmd, AbstractRepOpResult res )
  {
    ArtifactLocation loc = new ArtifactLocation( root, bmd );

    File   gaDir = new File( root, loc.getGaPath() );
    
    if( !gaDir.exists() )
    {
      res.addError( bmd, new RepositoryException( _lang.getMessage( "ga.not.found", bmd.toString(), loc.getGaPath() ) ) );
      return null;
    }
    
    Quality vq = new Quality( loc.getVersion() );
    
    // RELEASE = LATEST - SNAPSHOTs
    if( Artifact.RELEASE_VERSION.equals( loc.getVersion() )
        ||
        Artifact.LATEST_VERSION.equals( loc.getVersion() ) 
      )
    {
      boolean noSnapshots = Artifact.RELEASE_VERSION.equals( loc.getVersion() );
      loc.setVersion( null );
      DefaultArtifactVersion tempDav = null;
      DefaultArtifactVersion tempDav2 = null;

      File [] files = gaDir.listFiles();

      // find latest
      for( File vf : files )
      {
        if( vf.isFile() )
          continue;
        
        String vn = vf.getName();
        
        // RELEASE?
        if( noSnapshots && vn.endsWith( Artifact.SNAPSHOT_VERSION ))
          continue;
        
        if( loc.getVersion() == null )
        {
          loc.setVersion( vn );
          tempDav = new DefaultArtifactVersion( vn );
          continue;
        }
        
        tempDav2 = new DefaultArtifactVersion( vn );
        if( tempDav2.compareTo( tempDav ) > 0 )
        {
          loc.setVersion( vn );
          tempDav = tempDav2;
        }
        
      }

      if( loc.getVersion() == null )
      {
        res.addError( bmd, new RepositoryException( _lang.getMessage( "gav.not.found", bmd.toString(), loc.getGaPath() ) ) );
        return null;
      }
      
      // LATEST is a SNAPSHOT :(
      if( loc.getVersion().endsWith( Artifact.SNAPSHOT_VERSION ) )
      {
        loc.setVersionDir( loc.getVersion() );

        if( !findLatestSnapshot( bmd, loc, res ) )
          return null;
      }
      else
        // R or L found and actual captured in loc.version
        loc.setVersionDir( loc.getVersion() );
    }
    // regular snapshot requested
    else if( loc.getVersion().endsWith( Artifact.SNAPSHOT_VERSION ) )
    {
      File gavDir = new File( gaDir, loc.getVersion() );
      if( !gavDir.exists() )
      {
        res.addError( bmd, new RepositoryException( _lang.getMessage( "gavdir.not.found", bmd.toString(), gavDir.getAbsolutePath() ) ) );
        return null;
      }
      
      if( !findLatestSnapshot( bmd, loc, res ) )
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
  public ArtifactResults readArtifacts( List<ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.isEmpty() )
      throw new IllegalArgumentException( _lang.getMessage( "empty.query", query==null?"null":"empty" ) );
    
    ArtifactResults res = new ArtifactResults();
    
    Set<StreamVerifierFactory> vFacs = null;
    
    if( _repo.hasServer() && _repo.getServer().hasReaderStreamVerifierFactories() )
      vFacs = _repo.getServer().getReaderStreamVerifierFactories();
    
    for( ArtifactBasicMetadata bmd : query )
    {
      DefaultArtifact da = bmd instanceof DefaultArtifact ? (DefaultArtifact)bmd : new DefaultArtifact( bmd );
      
      ArtifactLocation loc = calculateLocation( _repoDir.getAbsolutePath(), bmd, res );
      
      if( loc == null )
        continue;
      
      File binary = new File( loc.getAbsPath() );
      
      // binary calculated 
      if( ! binary.exists() )
      {
        res.addError( bmd, new RepositoryException( _lang.getMessage( "binary.not.found", bmd.toString(), binary.getAbsolutePath() ) ) );
        continue;
      }

      try // reading pom if one exists
      {
        if( checkFile( binary, vFacs ) )
          da.setFile( binary );

        if( "pom".equals( bmd.getType() ) ) 
        {
            da.setPomBlob( FileUtil.readRawData( binary ) );
        }
        else
        {
          File pomFile = new File( loc.getAbsPomPath() );
          if( pomFile.exists() )
          {
            if( checkFile( pomFile, vFacs ) )
              da.setPomBlob( FileUtil.readRawData( pomFile ) );
          }
          else
            _log.warn( _lang.getMessage( "pom.not.found", bmd.toString()) );
        }

        da.setVersion( loc.getVersion() );
        res.add( bmd, da );
      }
      catch( Exception e )
      {
        throw new RepositoryException( e );
      }
    }
    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  private static boolean checkFile( File f, Set<StreamVerifierFactory> vFacs )
  throws RepositoryException, StreamVerifierException
  {
    if( vFacs != null )
    {
      String fileName = f.getAbsolutePath();
      
      HashSet<StreamVerifier> vs = new HashSet<StreamVerifier>( vFacs.size() );
      
      for( StreamVerifierFactory svf : vFacs )
      {
        StreamVerifier sv = svf.newInstance();
        String ext = sv.getAttributes().getExtension();
        String sigFileName = fileName+(ext.startsWith( "." )?"":".")+ext;
        File sigFile = new File( sigFileName );
        if( sigFile.exists() )
        {
          try
          {
            sv.initSignature( FileUtil.readRawDataAsString( sigFile ) );
          }
          catch( IOException e )
          {
            throw new RepositoryException( _lang.getMessage( "cannot.read.signature.file", sigFileName, e.getMessage() ) );
          }
          vs.add( sv );
        }
        else if( ! sv.getAttributes().isLenient() )
        {
          throw new RepositoryException( _lang.getMessage( "no.signature.file", ext, sigFileName ) );
        }
        // otherwise ignore absence of signature file, if verifier is lenient
      }

      FileInputStream fin = null;
      try
      {
        fin = new FileInputStream( f );
        byte [] buf = new byte[ 1024 ];
        int n = -1;
        while( (n = fin.read( buf )) != -1 )
        {
          for( StreamVerifier sv : vs )
            try
            {
              sv.bytesReady( buf, 0, n );
            }
            catch( StreamObserverException e )
            {
              if( ! sv.getAttributes().isLenient() )
                throw new RepositoryException(e);
            }
        }
        
        for( StreamVerifier sv : vs )
        {
          if( sv.verifySignature() )
          {
            if( sv.getAttributes().isSufficient() )
              break;
          }
          else
          {
            if( !sv.getAttributes().isLenient() )
              throw new RepositoryException( _lang.getMessage( "signature.failed", sv.getAttributes().getExtension(), fileName ) );
          }
        }
      }
      catch( IOException e )
      {
        throw new RepositoryException(e);
      }
      finally
      {
        if( fin != null ) try { fin.close(); } catch( Exception any ) {}
      }
    }
    return true;
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

    ArtifactBasicResults ror = null;
    
    File pomFile = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      String pomPath = bmd.getGroupId().replace( '.', '/' )
                      + "/" + bmd.getArtifactId()
                      + "/" + bmd.getVersion()
                      + "/" + bmd.getArtifactId()+'-'+bmd.getVersion()
                      + ".pom"
                      ;
      
      pomFile = new File( _repoDir, pomPath );
      if( ! pomFile.exists() )
      {
        _log.warn( "file \""+pomPath+"\" does not exist in local repo" );
        continue;
      }
      
      // TODO HIGH og: delegate POM processing to maven-project
      // for testing purpose - I plug in my test processor
      try
      {
        List<ArtifactBasicMetadata> deps = _mdProcessor.getDependencies( bmd, this, System.getProperties() );
        ror = ArtifactBasicResults.add( ror, bmd, deps );
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
  private static boolean findLatestSnapshot( ArtifactBasicMetadata bmd, ArtifactLocation loc, AbstractRepOpResult res )
  {
    File binary = new File( loc.getAbsPath() );
    
    if( binary.exists() )
      return true;

    // no real SNAPSHOT file, let's try to find one
    File gavDir = new File( loc.getGavPath() );
    File [] files = gavDir.listFiles();
    loc.setVersion( null );
    DefaultArtifactVersion tempDav = null;
    DefaultArtifactVersion tempDav2 = null;
    
    int aLen = loc.getBaseName().length();
    
    // find latest
    for( File vf : files )
    {
      if( vf.isFile() )
        continue;
      
      String vn = vf.getName().substring( aLen+1 );
      
      // no snapshots
      if( vn.endsWith( Artifact.SNAPSHOT_VERSION ))
        continue;

      if( loc.getVersion() == null )
      {
        loc.setVersion( vn );
        tempDav = new DefaultArtifactVersion( vn );
        continue;
      }
      
      tempDav2 = new DefaultArtifactVersion( vn );
      if( tempDav2.compareTo( tempDav ) > 0 )
      {
        loc.setVersion( vn );
        tempDav = tempDav2;
      }
    
    }

    if( loc.getVersion() == null )
    {
      res.addError( bmd, new RepositoryException( _lang.getMessage( "snapshot.not.found", bmd.toString(), gavDir.getAbsolutePath() ) ) );
      return false;
    }
    
    return true;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * direct disk search, no redirects - I cannot process pom files :(
   */
  public ArtifactBasicResults readVersions( List<ArtifactBasicMetadata> query )
  throws RepositoryException, IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;
    
    ArtifactBasicResults res = new ArtifactBasicResults( query.size() );
    
    File gaDir = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      gaDir = new File( _repoDir, bmd.getGroupId().replace( '.', '/' )+"/"+bmd.getArtifactId() );
      if( ! gaDir.exists() )
        continue;
      
      File [] versionFiles = gaDir.listFiles();
      
      VersionRange versionQuery;
      try
      {
        versionQuery = new VersionRange( bmd.getVersion(), _repo.getVersionRangeQualityRange() );
      }
      catch( VersionException e )
      {
        res = ArtifactBasicResults.add( res, bmd, new RepositoryException(e) );
        continue;
      }
      
      Quality vq = new Quality( bmd.getVersion() );
      
      if(    vq.equals( Quality.FIXED_RELEASE_QUALITY ) 
          || vq.equals( Quality.FIXED_LATEST_QUALITY )
          || vq.equals( Quality.SNAPSHOT_QUALITY     )
      )
      {
        ArtifactLocation loc = calculateLocation( _repoDir.getAbsolutePath(), bmd, res );
        
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
      
      for( File vf : versionFiles )
      {
        if( !vf.isDirectory() )
          continue;
        
        String version = vf.getName();
        
        Quality q = new Quality( version );
        if( ! _repo.isAcceptedQuality( q ) )
          continue;
        
        if( !versionQuery.includes(  vf.getName() )  )
          continue;
        
        ArtifactBasicMetadata vmd = new ArtifactBasicMetadata();
        vmd.setGroupId( bmd.getGroupId() );
        vmd.setArtifactId(  bmd.getArtifactId() );
        vmd.setClassifier( bmd.getClassifier() );
        vmd.setType( bmd.getType() );
        vmd.setVersion( vf.getName() );
        
        res = ArtifactBasicResults.add( res, bmd, vmd );
      }
    }
    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  public byte[] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataReaderException
  {
    return readRawData( relPathOf(bmd, classifier, type, null ) );
  }
  //---------------------------------------------------------------------------------------------------------------
  private static String relPathOf( ArtifactBasicMetadata bmd, String classifier, String type, DefaultArtifactVersion inDav )
  {
    DefaultArtifactVersion dav = inDav;
    if( inDav == null )
      dav = new DefaultArtifactVersion( bmd.getVersion() );
    Quality aq = dav.getQuality();
    boolean isSnapshot = aq.equals( Quality.SNAPSHOT_QUALITY ) || aq.equals( Quality.SNAPSHOT_TS_QUALITY );
    
    String bmdPath = bmd.getGroupId().replace( '.', '/' )+'/'+bmd.getArtifactId()
                +'/' + ( isSnapshot ? dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION : bmd.getVersion() );
    
    String path = bmdPath+'/'+bmd.getBaseName(classifier)+'.' + (type == null ? bmd.getType() : type );
    
    return path ;
  }
  //---------------------------------------------------------------------------------------------------------------
  public byte[] readRawData( String path )
  throws MetadataReaderException
  {
    File file = new File( _repoDir, path );
    
    if( ! file.exists() )
      return null;
    
    FileInputStream fis = null;
    
    try
    {
      fis = new FileInputStream( file );
      int len = (int)file.length();
      byte [] pom = new byte [ len ];
      fis.read( pom );
      return pom;
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
  public String readStringData( String path )
  throws MetadataReaderException
  {
    byte [] data = readRawData( path );
    if( data == null )
      return null;

    return new String( data );
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
}
