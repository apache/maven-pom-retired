package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.ArtifactBasicMetadata;
import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;

/**
 * this helper class hides the necessity to talk to localRepo and a bunch of remoteRepos.
 * It also adds discrete convenience methods, hiding batch nature of RepositoryReader
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class VirtualRepositoryReader
{
  //----------------------------------------------------------------------------------------------------------------------------
  private List<Repository>       _repositories = new ArrayList<Repository>(8);
  private RepositoryReader[]     _repositoryReaders;

  private LocalRepository       _localRepository;
  
  private boolean _initialized = false;
  //----------------------------------------------------------------------------------------------------------------------------
  public VirtualRepositoryReader(
                  LocalRepository localRepository
                , List<RemoteRepository> remoteRepositories
                          )
  throws RepositoryException
  {
    if( _localRepository == null )
      throw new RepositoryException( "null local repo" );
    
    this._localRepository = localRepository;

    this._repositories.add( localRepository );
    
    if( remoteRepositories != null && remoteRepositories.size() > 0 )
      this._repositories.addAll( remoteRepositories );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public void addRepository( Repository repo )
  throws RepositoryException
  {
    if( _initialized )
      throw new RepositoryException("cannot add repositories after VirtualReader has been initialized");
    
    _repositories.add( repo );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public void init()
  {
    if( _initialized )
      return;
    
    int repositoryCount = _repositories.size();
    
    _repositoryReaders = new RepositoryReader[ repositoryCount ];
    
    int i = 0;
    for( Repository r : _repositories )
    {
      _repositoryReaders[ i++ ] = r.getReader();
      if( r.isLocal() )
        _localRepository = (LocalRepository)r.getReader().getRepository();
    }
    _initialized = true;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> readVersions( List<? extends ArtifactBasicMetadata> query )
  throws IllegalArgumentException, RepositoryException
  {
    if( query == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    init();
        
    Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> res = null;

    for( RepositoryReader rr : _repositoryReaders )
    {
      Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> repoRes = rr.readVersions( query );
      
      if( repoRes != null )
        for( ArtifactBasicMetadata key : repoRes.keySet() )
        {
          RepositoryOperationResult<ArtifactBasicMetadata> ror = repoRes.get( key );
          if( ror != null && !ror.hasExceptions() && ror.hasResults() )
          {
            List<ArtifactBasicMetadata> rorRes = ror.getResults();
            for( ArtifactBasicMetadata bmd : rorRes )
              bmd.setReader(  rr );
            
            if( res == null )
              res = new HashMap<ArtifactBasicMetadata, List<ArtifactBasicMetadata>>( query.size() );
            
            if( res.containsKey( key ) )
              res.get( key ).addAll( rorRes );
            else
              res.put( key, rorRes );
          }
        }
    }
    
    return res;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public ArtifactMetadata readMetadata( ArtifactBasicMetadata bmd )
  throws IllegalArgumentException, RepositoryException
  {
    if( bmd == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    init();
    
    List<ArtifactBasicMetadata> query = new ArrayList<ArtifactBasicMetadata>(1);
    query.add( bmd );
    
    List<ArtifactMetadata> resList = null;
    for( RepositoryReader rr : _repositoryReaders )
    {
      resList = readMetadataFromRepository( query, rr );
      if( resList != null )
        return resList.get( 0 );
    }
    
    return null;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  private List<ArtifactMetadata> readMetadataFromRepository( List<ArtifactBasicMetadata> query, RepositoryReader reader )
  throws RepositoryException
  {
    RepositoryOperationResult<ArtifactMetadata> res = reader.readDependencies( query );
    if( res != null && !res.hasExceptions() && res.hasResults() )
    {
      List<ArtifactMetadata> resList = res.getResults();
      for( ArtifactBasicMetadata bmd : resList )
        bmd.setReader( reader );
      
      return resList;
    }
    
    return null;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------------------------------------
}
