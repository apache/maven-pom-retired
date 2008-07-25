package org.apache.maven.mercury.repository;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.ArtifactBasicMetadata;
import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.DefaultArtifact;
import org.apache.maven.mercury.metadata.version.VersionException;
import org.apache.maven.mercury.metadata.version.VersionRange;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;

public class LocalRepositoryReader
implements RepositoryReader
{
  //---------------------------------------------------------------------------------------------------------------
  LocalRepository _repo;
  File _repoDir;
  //---------------------------------------------------------------------------------------------------------------
  public LocalRepositoryReader( LocalRepository repo )
  {
    if( repo == null )
      throw new IllegalArgumentException("localRepo cannot be null");
    
    _repoDir = repo.getDirectory();
    if( _repoDir == null )
      throw new IllegalArgumentException("localRepo directory cannot be null");
    
    if( !_repoDir.exists() )
      throw new IllegalArgumentException("localRepo directory \""+_repoDir.getAbsolutePath()+"\" should exist");

    _repo = repo;
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
  public RepositoryOperationResult<ArtifactMetadata> readMetadata( List<? extends ArtifactBasicMetadata> query )
      throws RepositoryException,
      IllegalArgumentException
  {
    if( query == null || query.size() < 1 )
      return null;

    RepositoryOperationResult<ArtifactMetadata> rr = new RepositoryOperationResult<ArtifactMetadata>();
    
    File gavDir = null;
    for( ArtifactBasicMetadata bmd : query )
    {
      gavDir = new File( _repoDir, bmd.getGroupId().replace( '.', '/' )+"/"+bmd.getArtifactId()+"/"+bmd.getVersion() );
      // TODO og: the rest
    }
    
    return rr;
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
    }
    
    return res;
  }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
}
