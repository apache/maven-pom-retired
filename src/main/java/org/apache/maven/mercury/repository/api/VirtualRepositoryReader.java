package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.ArtifactBasicMetadata;
import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeException;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;

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
  private LocalRepository        _localRepository;
  private List<RemoteRepository> _remoteRepositories;
  
  private int                    _repositoryCount = 1;
  private RepositoryReader[]     _repositoryReaders;
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

    this._remoteRepositories = remoteRepositories;
    
    if( _remoteRepositories != null && _remoteRepositories.size() > 0 )
      _repositoryCount += _remoteRepositories.size();
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public void init()
  {
    if( _repositoryReaders != null )
      return;
    
    _repositoryReaders = new RepositoryReader[ _repositoryCount ];
    _repositoryReaders[0] = _localRepository.getReader();
    
    if( _repositoryCount > 1 )
    {
      int i = 1;
      for( RemoteRepository rr : _remoteRepositories )
        _repositoryReaders[ i++ ] = rr.getReader();
    }
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> findMetadata( List<? extends ArtifactBasicMetadata> query )
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
    RepositoryOperationResult<ArtifactMetadata> res = reader.readMetadata( query );
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
