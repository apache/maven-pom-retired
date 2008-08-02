package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;

public class LocalRepositoryReaderM2
extends AbstracRepositoryReader
implements RepositoryReader, MetadataReader
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( LocalRepositoryReaderM2.class ); 
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
    
    File pomFile = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      String pomPath = bmd.getGroupId().replace( '.', '/' )
                      + "/" + bmd.getArtifactId()
                      + "/" + bmd.getVersion()
                      + "/" + bmd.getBaseName()
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
        List<ArtifactBasicMetadata> deps = _mdProcessor.getDependencies( bmd, this );
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
  public Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> readVersions(
      List<? extends ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
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
        versionQuery = new VersionRange( bmd.getVersion() );
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
    String bmdPath = bmd.getGroupId().replace( '.', '/' )+"/"+bmd.getArtifactId()+"/"+bmd.getVersion();
    
    String path = bmdPath+"/"+bmd.getBaseName(classifier)+'.' + (type == null ? bmd.getType() : type );
    
    return readRawData( path );
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
