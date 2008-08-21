package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
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
  public RepositoryOperationResult<DefaultArtifact> readArtifacts( List<? extends ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.isEmpty() )
      throw new IllegalArgumentException( _lang.getMessage( "empty.query", query==null?"null":"empty" ) );
    
    RepositoryOperationResult<DefaultArtifact> res = new RepositoryOperationResult<DefaultArtifact>();
    
    for( ArtifactBasicMetadata bmd : query )
    {
      DefaultArtifact da = bmd instanceof DefaultArtifact ? (DefaultArtifact)bmd : new DefaultArtifact( bmd );
      
      String version = bmd.getVersion();
      DefaultArtifactVersion dav = new DefaultArtifactVersion( version );
      Quality vq = dav.getQuality();
      
      String relGaPath = bmd.getGroupId().replace( '.', '/' ) + '/' + bmd.getArtifactId();
      File   gaDir = new File( _repoDir, relGaPath );
      
      if( !gaDir.exists() )
      {
        res.add( new RepositoryException( _lang.getMessage( "ga.not.found", bmd.toString(), relGaPath ) ) );
        continue;
      }
      

      File binary = null;
      
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
        {
          res.add( new RepositoryException( _lang.getMessage( "gav.not.found", bmd.toString(), relGaPath ) ) );
          continue;
        }
        
        // LATEST is a SNAPSHOT :(
        if( version.endsWith( Artifact.SNAPSHOT_VERSION ) )
        {
          binary = findLatestSnapshot( new File( gaDir, version ), da, res );

          if( binary == null )
            continue;
        }
        else
        {
          binary = new File( gaDir, version+'/'+bmd.getArtifactId()+'-'+version+( bmd.hasClassifier() ? '-'+bmd.getClassifier() : "" )+'.'+bmd.getType() );
          da.setVersion( version );
        }
      }
      // regular snapshot requested
      else if( version.endsWith( Artifact.SNAPSHOT_VERSION ) )
      {
        File gavDir = new File( gaDir, version );
        if( !gavDir.exists() )
        {
          res.add( new RepositoryException( _lang.getMessage( "gavdir.not.found", bmd.toString(), gavDir.getAbsolutePath() ) ) );
          continue;
        }
        
        binary = findLatestSnapshot( gavDir, da, res );
        
        if( binary == null )
          continue;
          
      }
      // time stamped snapshot requested
      else if( vq.equals( Quality.SNAPSHOT_TS_QUALITY ))
      {
        binary = new File( gaDir,  dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION 
            + '/' + bmd.getArtifactId() + '-' + bmd.getVersion() + ( bmd.hasClassifier() ? '-'+bmd.getClassifier() : "" ) 
            + '.' + bmd.getType()
                          );
      }
      else
      {
        binary = new File( gaDir,  version 
            + '/' + bmd.getArtifactId() + '-' + bmd.getVersion() + ( bmd.hasClassifier() ? '-'+bmd.getClassifier() : "" ) 
            + '.' + bmd.getType()
                          );
      }
      
      // binary calculated 
      if( ! binary.exists() )
      {
        res.add( new RepositoryException( _lang.getMessage( "binary.not.found", bmd.toString(), binary.getAbsolutePath() ) ) );
        continue;
      }

      da.setFile( binary );
      
      try // reading pom if one exists
      {
        if( "pom".equals( bmd.getType() ) ) 
        {
            da.setPomBlob( FileUtil.readRawData( binary ) );
        }
        else
        {
          File pomFile = new File( _repoDir, relPathOf( da, null, "pom", dav) );
          if( pomFile.exists() )
              da.setPomBlob( FileUtil.readRawData( pomFile ) );
          else
            _log.warn( _lang.getMessage( "pom.not.found", bmd.toString()) );
        }

        res.add( da );
      }
      catch( IOException e )
      {
        throw new RepositoryException( e );
      }
    }
    return res;
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
  private static File findLatestSnapshot( File gavDir, DefaultArtifact da, RepositoryOperationResult<DefaultArtifact> res )
  {
      
    String version = gavDir.getName();
    DefaultArtifactVersion dav = new DefaultArtifactVersion( version );
    
    File binary = new File( gavDir, da.getArtifactId()+'-'+dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION
            +( da.hasClassifier() ? '-'+da.getClassifier() : "" )
            +'.'+da.getType()
    );
    
    // no real SNAPSHOT file, let's look for one
    if( binary.exists() )
    {
      da.setVersion( dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION );
      return binary;
    }

    
    File [] files = gavDir.listFiles();
    version = null;
    DefaultArtifactVersion tempDav = null;
    DefaultArtifactVersion tempDav2 = null;
    
    int aLen = da.getArtifactId().length();
    
    // find latest
    for( File vf : files )
    {
      if( vf.isFile() )
        continue;
      
      String vn = vf.getName().substring( aLen+1 );
      
      // no snapshots
      if( vn.endsWith( Artifact.SNAPSHOT_VERSION ))
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
    {
      res.add( new RepositoryException( _lang.getMessage( "snapshot.not.found", da.toString(), gavDir.getAbsolutePath() ) ) );
      return null;
    }
    
    binary = new File( gavDir, da.getArtifactId()+'-'+dav.getBase()+'-'+version
                            + ( da.hasClassifier() ? '-'+da.getClassifier() : "" )
                            + '.'+da.getType()
                      );
    da.setVersion( version );
    
    return binary;
  }
  //---------------------------------------------------------------------------------------------------------------
  /**
   * direct disk search, no redirects, first attempt
   */
  public Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>>
                                      readVersions( List<? extends ArtifactBasicMetadata> query )
  throws RepositoryException, IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;
    
    Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> res = new HashMap<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>>( query.size() );
    
    File gaDir = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      gaDir = new File( _repoDir, bmd.getGroupId().replace( '.', '/' )+"/"+bmd.getArtifactId() );
      if( ! gaDir.exists() )
        continue;
      
      File [] versionFiles = gaDir.listFiles();
      
      RepositoryOperationResult<ArtifactBasicMetadata> rr = null;
      VersionRange versionQuery;
      try
      {
        versionQuery = new VersionRange( bmd.getVersion(), _repo.getVersionRangeQualityRange() );
      }
      catch( VersionException e )
      {
        rr = RepositoryOperationResult.add( rr, new RepositoryException(e) );
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
  throws MetadataProcessingException
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
      throw new MetadataProcessingException(e);
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public String readStringData( String path )
  throws MetadataProcessingException
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
