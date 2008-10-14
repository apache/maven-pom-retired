package org.apache.maven.mercury.repository.virtual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.api.ArtifactListProcessor;
import org.apache.maven.mercury.artifact.api.ArtifactListProcessorException;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.repository.api.AbstractRepOpResult;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.cache.fs.MetadataCacheFs;

/**
 * this helper class hides the necessity to talk to localRepo and a bunch of remoteRepos.
 * It also adds discrete convenience methods, hiding batch nature of RepositoryReader
 * 
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class VirtualRepositoryReader
implements MetadataReader
{
  public static final String METADATA_CACHE_DIR = ".cache";

  //----------------------------------------------------------------------------------------------------------------------------
  private List<Repository>       _repositories = new ArrayList<Repository>(8);

  private RepositoryReader[]     _repositoryReaders;

  private LocalRepository       _localRepository;
  
  private DependencyProcessor     _processor;
  
  RepositoryMetadataCache         _mdCache;

  private Map<String,ArtifactListProcessor>   _processors;
  
  private boolean _initialized = false;
  //----------------------------------------------------------------------------------------------------------------------------
  public VirtualRepositoryReader(
                  LocalRepository        localRepository
                , List<RemoteRepository> remoteRepositories
                , DependencyProcessor    processor
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
  public VirtualRepositoryReader( List<Repository> repositories, DependencyProcessor processor  )
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
  public static final RepositoryMetadataCache getCache( File localRepositoryRoot )
  throws IOException
  {
    // TODO: 2008-10-13 og: man - I miss plexus! Badly want an IOC container. This 
    // should be configured, not hardcoded
    return MetadataCacheFs.getCache( new File(localRepositoryRoot, METADATA_CACHE_DIR) );
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
  public void setProcessors( Map<String, ArtifactListProcessor> processors )
  {
    _processors = processors; 
  }
  //----------------------------------------------------------------------------------------------------------------------------
  /**
   * very important call - makes VRR sort out all the information it collected so far 
   */
  public void init()
  throws RepositoryException
  {
    if( _initialized )
      return;
    
    _repositoryReaders = new RepositoryReader[ _repositories.size() ];
    
    // move local repo's upfront - they are faster!
    int i = 0;
    for( Repository r : _repositories )
    {
      if( ! r.isLocal() )
        continue;
      
      _repositoryReaders[ i++ ] = r.getReader(_processor);
      if( ! r.isReadOnly() )
      {
        _localRepository = (LocalRepository)r.getReader(_processor).getRepository();
        if( _mdCache == null )
        {
          try
          {
            _mdCache = getCache( _localRepository.getDirectory() );
          }
          catch( IOException e )
          {
            throw new RepositoryException( e.getMessage() );
          }
        }
      }
    }
    
    // remote ones
    for( Repository r : _repositories )
    {
      if( r.isLocal() )
        continue;
      
      RepositoryReader rr = r.getReader(_processor);
      
      if( _mdCache != null )
        rr.setMetadataCache( _mdCache );

      _repositoryReaders[ i++ ] = rr;
    }
    _initialized = true;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public ArtifactBasicResults readVersions( List<ArtifactBasicMetadata> query )
  throws IllegalArgumentException, RepositoryException
  {
    if( query == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    init();

    ArtifactBasicResults res = null;
    ArtifactListProcessor tp = _processors == null ? null : _processors.get( ArtifactListProcessor.FUNCTION_TP );

    for( RepositoryReader rr : _repositoryReaders )
    {
      ArtifactBasicResults repoRes = rr.readVersions( query );
      
      if( repoRes != null && repoRes.hasResults() )
        for( ArtifactBasicMetadata key : repoRes.getResults().keySet() )
        {
          List<ArtifactBasicMetadata> rorRes = repoRes.getResult(key);
          
          if( tp != null )
          {
            try
            {
              tp.configure( key );
              rorRes = tp.process( rorRes );
            }
            catch( ArtifactListProcessorException e )
            {
              throw new RepositoryException(e);
            }
          }
          
          if( rorRes == null )
            continue;
          
          for( ArtifactBasicMetadata bmd : rorRes )
            bmd.setTracker(  rr );
          
          if( res == null )
            res = new ArtifactBasicResults( key, rorRes );
          else
            res.add( key, rorRes );
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
    
    ArtifactMetadata md = new ArtifactMetadata( bmd ); 
    for( RepositoryReader rr : _repositoryReaders )
    {
      ArtifactBasicResults res = rr.readDependencies( query );
      
      if( res != null && res.hasResults( bmd ) )
      {
        md.setDependencies( res.getResult( bmd ) );
        md.setTracker( rr );
        return md;
      }
    }
    
    return md;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.repository.api.MetadataReader#readMetadata(org.apache.maven.mercury.ArtifactBasicMetadata)
   */
  public byte[] readMetadata( ArtifactBasicMetadata bmd )
      throws MetadataReaderException
  {
    return readRawData( bmd, "", "pom" );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.repository.api.MetadataReader#readRawData(org.apache.maven.mercury.ArtifactBasicMetadata, java.lang.String)
   */
  public byte[] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataReaderException
  {
    if( bmd == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    try
    {
      init();
    }
    catch( RepositoryException e )
    {
      throw new MetadataReaderException(e);
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
