/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.maven.mercury.repository.virtual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.api.ArtifactListProcessor;
import org.apache.maven.mercury.artifact.api.ArtifactListProcessorException;
import org.apache.maven.mercury.artifact.version.MetadataVersionComparator;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.artifact.version.VersionRangeFactory;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.event.EventGenerator;
import org.apache.maven.mercury.event.EventManager;
import org.apache.maven.mercury.event.EventTypeEnum;
import org.apache.maven.mercury.event.GenericEvent;
import org.apache.maven.mercury.event.MercuryEvent;
import org.apache.maven.mercury.event.MercuryEventListener;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.cache.fs.MetadataCacheFs;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * this helper class hides the necessity to talk to localRepo and a bunch of remoteRepos.
 * It also adds discrete convenience methods, hiding batch nature of RepositoryReader
 * 
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class VirtualRepositoryReader
implements MetadataReader, EventGenerator
{
  public static final String EVENT_READ_ARTIFACTS = "read.artifacts";
  public static final String EVENT_READ_ARTIFACTS_FROM_REPO = "read.artifacts.from.repo";
  public static final String EVENT_READ_ARTIFACTS_FROM_REPO_QUALIFIED = "read.artifacts.from.repo.qualified";
  public static final String EVENT_READ_ARTIFACTS_FROM_REPO_UNQUALIFIED = "read.artifacts.from.repo.unqualified";
  
  public static final String EVENT_READ_VERSIONS = "read.versions";
  public static final String EVENT_READ_VERSIONS_FROM_REPO = "read.versions.from.repo";
  
  public static final String EVENT_READ_DEPENDENCIES = "read.dependencies";
  public static final String EVENT_READ_DEPENDENCIES_FROM_REPO = "read.dependencies.from.repo";
  
  public static final String EVENT_READ_RAW = "vr.read.raw";
  public static final String EVENT_READ_RAW_FROM_REPO = "read.raw.from.repo";
  
  /** file system cache subfolder */
  public static final String METADATA_CACHE_DIR = ".cache";
  
  /** minimum # of queue elements to consider parallelization */
  private static int MIN_PARALLEL = 5;
  
  private static final Language _lang = new DefaultLanguage( VirtualRepositoryReader.class );
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( VirtualRepositoryReader.class ); 

  //----------------------------------------------------------------------------------------------------------------------------
  private List<Repository>       _repositories = new ArrayList<Repository>(8);

  private RepositoryReader[]     _repositoryReaders;

  private LocalRepository       _localRepository;
  
  private RepositoryWriter      _localRepositoryWriter;
  
  RepositoryMetadataCache         _mdCache;

  private Map<String,ArtifactListProcessor>   _processors;
  
  private boolean _initialized = false;
  
  private EventManager _eventManager;
  //----------------------------------------------------------------------------------------------------------------------------
  public VirtualRepositoryReader( Collection<Repository> repositories )
  throws RepositoryException
  {
    if( ! Util.isEmpty( repositories ) )
      this._repositories.addAll( repositories );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  private VirtualRepositoryReader(
                  LocalRepository        localRepository
                , Collection<RemoteRepository> remoteRepositories
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
  private VirtualRepositoryReader( Repository... repositories )
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
      if( ! r.isLocal() || ! r.isReadable() )
        continue;
      
      RepositoryReader rr = r.getReader();
      
      rr.setMetadataReader( this );
      
      _repositoryReaders[ i++ ] = rr;
      
      if( r.isWriteable() )
      {
        // we select the first writable repo in the list
        if( _localRepository != null )
          continue;
        
        _localRepository = (LocalRepository)r.getReader().getRepository();
        _localRepositoryWriter = _localRepository.getWriter();
        
        if( _mdCache == null )
        {
          try
          {
            _mdCache = getCache( _localRepository.getDirectory() );
            
            if( _eventManager != null )
              _mdCache.setEventManager( _eventManager );
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
      if( r.isLocal() || ! r.isReadable() )
        continue;
      
      RepositoryReader rr = r.getReader();
      
      if( _mdCache != null )
        rr.setMetadataCache( _mdCache );
      
      rr.setMetadataReader( this );

      _repositoryReaders[ i++ ] = rr;
    }
    _initialized = true;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public ArtifactBasicResults readVersions( Collection<ArtifactBasicMetadata> query )
  throws IllegalArgumentException, RepositoryException
  {
    if( query == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    init();
    
    GenericEvent event = null;
    
    try
    {
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_VERSIONS );
      
      ArtifactBasicResults res = null;
      ArtifactListProcessor tp = _processors == null ? null : _processors.get( ArtifactListProcessor.FUNCTION_TP );
      
      GenericEvent eventRead = null;
      
      List<ArtifactBasicMetadata> qList = new ArrayList<ArtifactBasicMetadata>( query.size() );
      qList.addAll( query );
  
      for( RepositoryReader rr : _repositoryReaders )
      try 
      {
        // all found
        if( qList.isEmpty() )
          break;

        if( _eventManager!= null )
          eventRead = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_VERSIONS_FROM_REPO, rr.getRepository().getId() );
          
        ArtifactBasicResults repoRes = rr.readVersions( qList );
        
        if( repoRes != null && repoRes.hasResults() )
        {
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
            
            if( Util.isEmpty( rorRes ) )
            {
              eventRead.setResult( "none found" );
              continue;
            }
            
            for( ArtifactBasicMetadata bmd : rorRes )
              bmd.setTracker(  rr );
            
            if( res == null )
              res = new ArtifactBasicResults( key, rorRes );
            else
              res.add( key, rorRes );
            
            String keyVersion = key.getVersion();
            VersionRange keyVersionRange = null;
            try
            {
              keyVersionRange = VersionRangeFactory.create( key.getVersion() );
            }
            catch( VersionException e )
            {
              throw new RepositoryException( _lang.getMessage( "query.element.bad.version", key.toString(), e.getMessage() ) );
            }
            
            if( keyVersionRange.isSingleton() )
            {
              Quality keyQuality = new Quality(keyVersion);
              if( keyQuality.compareTo( Quality.RELEASE_QUALITY ) == 0 )
                // fixed release is found - no more scanning
                qList.remove( key );
            }
          }
        }
        
        if( _eventManager!= null )
          eventRead.setResult( "repo done" );
      }
      finally
      {
        if( _eventManager != null )
        {
          eventRead.stop();
          _eventManager.fireEvent( eventRead );
        }
      }
      
      return res;
    }
    finally
    {
      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public ArtifactMetadata readDependencies( ArtifactBasicMetadata bmd )
  throws IllegalArgumentException, RepositoryException
  {
    if( bmd == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    GenericEvent event = null;
    
    try
    {
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_DEPENDENCIES, bmd.toString() );
      
      init();
      
      List<ArtifactBasicMetadata> query = new ArrayList<ArtifactBasicMetadata>(1);
      query.add( bmd );
      
      ArtifactMetadata md = new ArtifactMetadata( bmd );
      
      RepositoryReader [] repos = _repositoryReaders;
      
      Object tracker =  bmd.getTracker();
      
      // do we know where this metadata came from ?
      if( tracker != null && RepositoryReader.class.isAssignableFrom( tracker.getClass() ) )
      {
        repos = new RepositoryReader [] { (RepositoryReader)tracker };
      }
      
      GenericEvent eventRead = null;
      
      for( RepositoryReader rr : repos )
      try 
      {
        
        if( _eventManager != null )
          eventRead = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_DEPENDENCIES_FROM_REPO, rr.getRepository().getId() );
        
        ArtifactBasicResults res = rr.readDependencies( query );
        
        if( res != null && res.hasResults( bmd ) )
        {
          md.setDependencies( res.getResult( bmd ) );
          md.setTracker( rr );
          
          if( _eventManager != null )
            eventRead.setInfo( eventRead.getInfo()+", found: "+md.getDependencies() );
          
          return md;
        }
      }
      finally
      {
        if( _eventManager != null )
        {
          eventRead.stop();
          _eventManager.fireEvent( eventRead );
        }
      }
      
      if( _eventManager != null )
        event.setResult( "not found" );
      
      return md;
    }
    finally
    {
      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }
  //----------------------------------------------------------------------------------------------------------------------------
  /**
   * split query into repository buckets
   */
  private Map< RepositoryReader, List<ArtifactBasicMetadata> > sortByRepo( Collection<? extends ArtifactBasicMetadata> query )
  {
    HashMap< RepositoryReader, List<ArtifactBasicMetadata> > res = null;
    
    List<ArtifactBasicMetadata> rejects = null;

    for( ArtifactBasicMetadata bmd : query )
    {
      Object tracker =  bmd.getTracker();
      
      // do we know where this metadata came from ?
      if( tracker != null && RepositoryReader.class.isAssignableFrom( tracker.getClass() ) )
      {
        RepositoryReader rr = (RepositoryReader)tracker;
        
        if( res == null )
        {
          res = new HashMap< RepositoryReader, List<ArtifactBasicMetadata> >();
        }
        
        List<ArtifactBasicMetadata> rl = res.get( rr );
        
        if( rl == null )
        {
          rl = new ArrayList<ArtifactBasicMetadata>();
          res.put( rr, rl );
        }
        
        rl.add( bmd );
          
      }
      else
      {
        if( rejects == null )
          rejects = new ArrayList<ArtifactBasicMetadata>();
        
        rejects.add( bmd );
      }
    }

    if( rejects != null )
    {
      if( res == null )
        res = new HashMap< RepositoryReader, List<ArtifactBasicMetadata> >();

      res.put( RepositoryReader.NULL_READER, rejects );
    }
    
    return res;
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public ArtifactResults readArtifacts( Collection<? extends ArtifactBasicMetadata> query )
  throws RepositoryException
  {
    GenericEvent event = null;
    
    try
    {
      ArtifactResults res = null;

      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_ARTIFACTS, "" );
      
      if( Util.isEmpty( query ) )
        return res;
      
      Map< RepositoryReader, List<ArtifactBasicMetadata> > buckets = sortByRepo( query );
      
      List<ArtifactBasicMetadata> rejects = buckets == null ? null : buckets.get( RepositoryReader.NULL_READER );
  
      if( buckets == null )
        throw new RepositoryException( _lang.getMessage( "internal.error.sorting.query", query.toString() ) ); 
        
      init();
      
      // first read repository-qualified Artifacts
      for( RepositoryReader rr : buckets.keySet() )
      {
        if( RepositoryReader.NULL_READER.equals( rr ) )
          continue;
        
        String repoId = rr.getRepository().getId();
        
        GenericEvent eventRead = null;
        
        try
        {
          if( _eventManager != null )
            eventRead = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_ARTIFACTS_FROM_REPO_QUALIFIED, repoId );
          
          List<ArtifactBasicMetadata> rrQuery = buckets.get( rr );
          
          ArtifactResults rrRes = rr.readArtifacts( rrQuery );
          
          if( rrRes.hasExceptions() )
            throw new RepositoryException( _lang.getMessage( "error.reading.existing.artifact", rrRes.getExceptions().toString(), rr.getRepository().getId() ) );
          
          if( rrRes.hasResults() )
            for( ArtifactBasicMetadata bm : rrRes.getResults().keySet() )
            {
              List<Artifact> al = rrRes.getResults( bm ); 
              
              if( res == null )
                res = new ArtifactResults();
    
              res.addAll( bm, al );
              
              // don't write local artifacts back to the same repo
              if( _localRepository != null && repoId.equals( _localRepository.getId() ) )
                continue;
    
              if( _localRepositoryWriter != null )
                _localRepositoryWriter.writeArtifacts( al );
            }
        }
        finally
        {
          if( _eventManager != null )
          {
            eventRead.stop();
            _eventManager.fireEvent( eventRead );
          }
        }
      }
      
      // then search all repos for unqualified Artifacts
      if( !Util.isEmpty( rejects ) )
      {
        for( RepositoryReader rr : _repositoryReaders )
        {
          if( rejects.isEmpty() )
            break;
          
          String repoId = rr.getRepository().getId();
          
          GenericEvent eventRead = null;
          
          try
          {
            if( _eventManager != null )
              eventRead = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_ARTIFACTS_FROM_REPO_UNQUALIFIED, repoId );
            
            ArtifactResults rrRes = rr.readArtifacts( rejects );
            
            if( rrRes.hasResults() )
              for( ArtifactBasicMetadata bm : rrRes.getResults().keySet() )
              {
                List<Artifact> al = rrRes.getResults( bm ); 
    
                if( res == null )
                  res = new ArtifactResults();
                
                res.addAll( bm, al );
                
                rejects.remove( bm );
                
                // don't write local artifacts back to the same repo
                if( _localRepository != null && repoId.equals( _localRepository.getId() ) )
                  continue;
    
                if( _localRepositoryWriter != null )
                  _localRepositoryWriter.writeArtifacts( al );
  
            }
          }
          finally
          {
            if( _eventManager != null )
            {
              eventRead.stop();
              _eventManager.fireEvent( eventRead );
            }
          }
        }
      }
      
      return res;
    }
    finally
    {
      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }
  //----------------------------------------------------------------------------------------------------------------------------
  // MetadataReader implementation
  //----------------------------------------------------------------------------------------------------------------------------
  public byte[] readMetadata( ArtifactBasicMetadata bmd )
  throws MetadataReaderException
  {
    if( _log.isDebugEnabled() )
      _log.debug( "Asking for pom: "+bmd);

    return readRawData( bmd, "", "pom" );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  // MetadataReader implementation
  //----------------------------------------------------------------------------------------------------------------------------
  public byte[] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataReaderException
  {
    
    GenericEvent event = null;
    String eventTag = null; 
    
    if( _log.isDebugEnabled() )
      _log.debug( "request for "+bmd+", classifier="+classifier+", type="+type );
    
    if( bmd == null )
      throw new IllegalArgumentException("null bmd supplied");
    
    try
    {
      if( _eventManager!= null )
      {
        eventTag = bmd.toString()
                  + (Util.isEmpty( classifier )?"":", classifier="+classifier)
                  + (Util.isEmpty( type )?"":", type="+type)
                    ;
        event = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_RAW, eventTag );
      }
      
      ArtifactBasicMetadata bmdQuery = bmd;
      
      try
      {
        init();
      }
      catch( RepositoryException e )
      {
        throw new MetadataReaderException(e);
      }
      
      byte [] res = null;
      Quality vq = new Quality( bmd.getVersion() );
      
      if( _log.isDebugEnabled() )
        _log.debug( "quality calculated as "+vq.getQuality() == null ? "null" :vq.getQuality().name() );
      
      if( Quality.SNAPSHOT_QUALITY.equals( vq ) )
      {
        List<ArtifactBasicMetadata> query = new ArrayList<ArtifactBasicMetadata>(1);
        query.add( bmd );
        
        try
        {
          ArtifactBasicResults vRes = readVersions( query );
          if( Util.isEmpty( vRes ) )
          {
            if( _log.isDebugEnabled() )
              _log.debug( "no snapshots found - throw exception" );
            
            throw new MetadataReaderException( _lang.getMessage( "no.snapshots", bmd.toString(), classifier, type ) );
          }
            
            
          if( vRes.hasResults( bmd ) )
          {
            List<ArtifactBasicMetadata> versions = vRes.getResult( bmd );
            
            TreeSet<ArtifactBasicMetadata> snapshots = new TreeSet<ArtifactBasicMetadata>( new MetadataVersionComparator() );
            snapshots.addAll( versions );
            
            bmdQuery = snapshots.last();
          }
          else
          {
            if( _log.isDebugEnabled() )
              _log.debug( "no snapshots found - throw exception" );
            
            throw new MetadataReaderException( _lang.getMessage( "no.snapshots", bmd.toString(), classifier, type ) );
          }
        }
        catch( Exception e )
        {
          throw new MetadataReaderException(e);
        }
      }
      
      for( RepositoryReader rr : _repositoryReaders )
      {
        GenericEvent eventRead = null;
        
        try
        {
          if( _eventManager != null )
            eventRead = new GenericEvent( EventTypeEnum.virtualRepositoryReader, EVENT_READ_RAW_FROM_REPO, rr.getRepository().getId()+": "+eventTag );
          
          res = rr.readRawData( bmdQuery, classifier, type );
          if( res != null )
          {
            if( _log.isDebugEnabled() )
              _log.debug( bmdQuery+" found in "+rr.getRepository().getServer() );

            if( _eventManager != null )
              eventRead.setInfo( eventRead.getInfo() );
            
            return res;
          }
          
          if( _eventManager != null )
            eventRead.setResult( "not found" );
        }
        finally
        {
          if( _eventManager != null )
          {
            eventRead.stop();
            _eventManager.fireEvent( eventRead );
          }
        }
      }
      
      if( _log.isDebugEnabled() )
        _log.debug( "no data found, returning null" );
      
      return null;
    }
    finally
    {
      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }
  //----------------------------------------------------------------------------------------------------------------------------
  public void register( MercuryEventListener listener )
  {
    if( _eventManager == null )
      _eventManager = new EventManager();

    _eventManager.register( listener );
  }

  public void setEventManager( EventManager eventManager )
  {
    _eventManager = eventManager;
  }

  public void unRegister( MercuryEventListener listener )
  {
    if( _eventManager != null )
      _eventManager.unRegister( listener );
  }
  //----------------------------------------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------------------------------------
}
