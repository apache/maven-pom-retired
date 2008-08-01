package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;

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
implements MetadataReader
{
  //----------------------------------------------------------------------------------------------------------------------------
  private List<Repository>       _repositories = new ArrayList<Repository>(8);
  private RepositoryReader[]     _repositoryReaders;

  private LocalRepository       _localRepository;
  
  private MetadataProcessor     _processor;
  
  private boolean _initialized = false;
  //----------------------------------------------------------------------------------------------------------------------------
  public VirtualRepositoryReader(
                  LocalRepository localRepository
                , List<RemoteRepository> remoteRepositories
                , MetadataProcessor processor
                          )
  throws RepositoryException
  {
    if( _localRepository == null )
      throw new RepositoryException( "null local repo" );
    
    if( processor == null )
      throw new RepositoryException( "null metadata processor" );
    this._processor = processor;
    
    this._localRepository = localRepository;

    this._repositories.add( localRepository );
    
    if( remoteRepositories != null && remoteRepositories.size() > 0 )
      this._repositories.addAll( remoteRepositories );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public VirtualRepositoryReader( List<Repository> repositories, MetadataProcessor processor  )
  throws RepositoryException
  {
    if( processor == null )
      throw new RepositoryException( "null metadata processor" );
    this._processor = processor;

    if( repositories != null && repositories.size() > 0 )
      this._repositories.addAll( repositories );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public VirtualRepositoryReader( Repository... repositories )
  throws RepositoryException
  {
    if( repositories != null && repositories.length > 0 )
      for( Repository r : repositories )
        this._repositories.add( r );
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
  throws RepositoryException
  {
    if( _initialized )
      return;
    
    int repositoryCount = _repositories.size();
    
    _repositoryReaders = new RepositoryReader[ repositoryCount ];
    
    // move local repo's upfront - they are faster!
    int i = 0;
    for( Repository r : _repositories )
    {
      if( ! r.isLocal() )
        continue;
      
      _repositoryReaders[ i++ ] = r.getReader(_processor);
      if( ! r.isReadOnly() )
        _localRepository = (LocalRepository)r.getReader(_processor).getRepository();
    }
    for( Repository r : _repositories )
    {
      if( r.isLocal() )
        continue;

      _repositoryReaders[ i++ ] = r.getReader(_processor);
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
              bmd.setTracker(  rr );
            
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
  public ArtifactMetadata readDependencies( ArtifactBasicMetadata bmd )
  throws IllegalArgumentException, RepositoryException
  {
    if( bmd == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    init();
    
    List<ArtifactBasicMetadata> query = new ArrayList<ArtifactBasicMetadata>(1);
    query.add( bmd );
    
    for( RepositoryReader rr : _repositoryReaders )
    {
      Map<ArtifactBasicMetadata, ArtifactMetadata> res = rr.readDependencies( query );
      if( res != null && ! res.isEmpty() )
      {
        ArtifactMetadata md = res.get( bmd );
        md.setTracker( rr );
        return md;
      }
    }
    
    return null;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.repository.api.MetadataReader#readMetadata(org.apache.maven.mercury.ArtifactBasicMetadata)
   */
  public byte[] readMetadata( ArtifactBasicMetadata bmd )
      throws MetadataProcessingException
  {
    return readRawData( bmd, "", "pom" );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.repository.api.MetadataReader#readRawData(org.apache.maven.mercury.ArtifactBasicMetadata, java.lang.String)
   */
  public byte[] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataProcessingException
  {
    if( bmd == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    try
    {
      init();
    }
    catch( RepositoryException e )
    {
      throw new MetadataProcessingException(e);
    }
    
    byte [] res = null;
    
    for( RepositoryReader rr : _repositoryReaders )
    {
      res = rr.readRawData( bmd, classifier, type );
      if( res != null )
        return res;
    }
    
    return null;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------------------------------------
}
