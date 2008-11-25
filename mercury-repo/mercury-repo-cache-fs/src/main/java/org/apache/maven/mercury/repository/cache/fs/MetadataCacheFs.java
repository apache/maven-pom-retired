package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.repository.api.MetadataCacheException;
import org.apache.maven.mercury.repository.api.MetadataCorruptionException;
import org.apache.maven.mercury.repository.api.RepositoryGAMetadata;
import org.apache.maven.mercury.repository.api.RepositoryGAVMetadata;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.repository.api.RepositoryUpdatePolicy;
import org.apache.maven.mercury.util.FileLockBundle;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.event.EventManager;
import org.apache.maven.mercury.util.event.EventTypeEnum;
import org.apache.maven.mercury.util.event.GenericEvent;
import org.apache.maven.mercury.util.event.MercuryEvent;
import org.apache.maven.mercury.util.event.MercuryEventListener;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataCacheFs
implements RepositoryMetadataCache
{
  public static final String EVENT_FIND_GA = "find.ga";
  public static final String EVENT_FIND_GAV = "find.gav";
  public static final String EVENT_FIND_RAW = "find.raw";

  public static final String EVENT_UPDATE_GA = "update.ga";
  public static final String EVENT_UPDATE_GAV = "update.gav";
  public static final String EVENT_SAVE_RAW = "save.raw";

  private static final Language _lang = new DefaultLanguage( RepositoryGAVMetadata.class );
  
  static volatile Map<String, MetadataCacheFs> fsCaches = Collections.synchronizedMap( new HashMap<String, MetadataCacheFs>(2) ); 
  
  // store resolved cached data in memory
  private volatile Map<String, RepositoryGAMetadata> gaCache
        = (Map<String, RepositoryGAMetadata>)Collections.synchronizedMap( new HashMap<String, RepositoryGAMetadata>(512) );
  
  private volatile Map<String, RepositoryGAVMetadata> gavCache
        = (Map<String, RepositoryGAVMetadata>)Collections.synchronizedMap( new HashMap<String, RepositoryGAVMetadata>(1024) );
  
  private volatile Map<String, byte []> rawCache
        = (Map<String, byte []>)Collections.synchronizedMap( new HashMap<String, byte []>(1024) );
  
  private File root;
  
  private EventManager _eventManager;
  
  /**
   * access to all known FS caches
   * 
   * @param root
   * @return
   * @throws IOException 
   */
  public static MetadataCacheFs getCache( File root )
  throws IOException
  {
    if( root == null 
        || ( root.exists() && root.isFile() )
    )
      throw new IllegalArgumentException( _lang.getMessage( "bad.root.file", root == null ? "null" : root.getAbsolutePath() ) );
    
    String key = root.getCanonicalPath();
    
    MetadataCacheFs fsc = fsCaches.get(key);
    
    if( fsc == null )
    {
      fsc = new MetadataCacheFs( root );
      fsCaches.put( key, fsc );
    }
    
    return fsc;
  }

  /**
   * private as it should be obtained via a call to <code>getCache()</code>
   */
  private MetadataCacheFs( File root )
  {
    this.root = root;
  }

  public RepositoryGAMetadata findGA( String repoGuid, RepositoryUpdatePolicy up, ArtifactCoordinates coord )
  throws MetadataCorruptionException
  {
    GenericEvent event = null;
    
    try
    {
      String gaKey = getGAKey(coord);
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.fsCache, EVENT_FIND_GA, gaKey );
      
      RepositoryGAMetadata inMem = gaCache.get( gaKey );
      
      if( inMem != null )
      {
        long lastCheckMillis = inMem.getLastCheckMillis();
        
        if( up.timestampExpired( lastCheckMillis ) )
        {
          inMem.setExpired( true );
          gaCache.put( gaKey, inMem );
        }
        
        if( _eventManager != null )
          event.setResult( "found in memory, expired is "+inMem.isExpired() );
        
        return inMem;
      }
      
      File gaDir = getGADir(coord);
      
      File gamF = getGAFile( gaDir, repoGuid );
      
      CachedGAMetadata md = null;
      
      if( gamF.exists() )
      {
          md = new CachedGAMetadata( gamF );
          
          long lastCheckMillis = md.getLastCheckMillis();
          
          if( up != null && up.timestampExpired( lastCheckMillis ) )
            md.setExpired( true );

          gaCache.put( gaKey, md );
          
          if( _eventManager != null )
            event.setResult( "found on disk, expired is "+md.isExpired() );
      }
      else
      {
        if( _eventManager != null )
          event.setResult( "not found" );
      }
      
      return md;
    }
    catch( Exception e )
    {
      throw new MetadataCorruptionException( e.getMessage() );
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

  public RepositoryGAVMetadata findGAV( String repoGuid, RepositoryUpdatePolicy up, ArtifactCoordinates coord )
  throws MetadataCorruptionException
  {
    FileLockBundle lock = null;
    GenericEvent event = null;
    
    try
    {
      String gavKey = getGAVKey(coord);
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.fsCache, EVENT_FIND_GAV, gavKey );
      
      RepositoryGAVMetadata inMem = gavCache.get( gavKey );
      
      if( inMem != null )
      {
        long lastCheckMillis = inMem.getLastCheckMillis();
        
        if( up.timestampExpired( lastCheckMillis ) )
        {
          inMem.setExpired( true );
          gavCache.put( gavKey, inMem );
        }
        
        if( _eventManager != null )
          event.setResult( "found in memory, expired is "+inMem.isExpired() );
        
        return inMem;
      }
      
      File gavDir = getGAVDir( coord );

      lock = FileUtil.lockDir( gavDir.getCanonicalPath(), 500L, 5L );
      
      File gavmF = getGAVFile( gavDir, repoGuid );
      
      CachedGAVMetadata md = null;
      
      if( gavmF.exists() )
      {
          md = new CachedGAVMetadata( gavmF );
          
          if( up != null && up.timestampExpired( md.getLastCheck() ) )
            md.setExpired( true );

          if( _eventManager != null )
            event.setResult( "found on disk, expired is "+inMem.isExpired() );
          
          gavCache.put(  gavKey, md );
      }
      else
        if( _eventManager != null )
          event.setResult( "not found" );
      
      return md;
    }
    catch( Exception e )
    {
      throw new MetadataCorruptionException( e.getMessage() );
    }
    finally
    {
      if( lock != null )
        lock.release();

      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }
  
  public void updateGA( String repoGuid, RepositoryGAMetadata gam )
  throws MetadataCacheException
  {
    FileLockBundle lock = null;

    GenericEvent event = null;
    
    try
    {
      String gaKey = getGAKey( gam.getGA() );
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.fsCache, EVENT_UPDATE_GA, gaKey );
      
      File gaDir = getGADir( gam.getGA() );
      
      lock = FileUtil.lockDir( gaDir.getCanonicalPath(), 500L, 5L );
      
      File gamF = getGAFile( gaDir, repoGuid );
      
      CachedGAMetadata md = new CachedGAMetadata( gam );
      
      md.cm.save( gamF );
      
      gaCache.put( gaKey, md );
    }
    catch( Exception e )
    {
      throw new MetadataCacheException( e.getMessage() );
    }
    finally
    {
      if( lock != null ) lock.release();

      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }

  public void updateGAV( String repoGuid, RepositoryGAVMetadata gavm )
  throws MetadataCacheException
  {
    FileLockBundle lock = null;

    GenericEvent event = null;
    
    try
    {
      String gavKey = getGAKey( gavm.getGAV() );

      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.fsCache, EVENT_UPDATE_GA, gavKey );
      
      
      File gavDir = getGAVDir( gavm.getGAV() );
      
      lock = FileUtil.lockDir( gavDir.getCanonicalPath(), 500L, 5L );

      File gavmF = getGAVFile( gavDir, repoGuid );
      
      CachedGAVMetadata md = new CachedGAVMetadata( gavm );
      
      md.cm.save( gavmF );
      
      gavCache.put( gavKey, md );
    }
    catch( Exception e )
    {
      throw new MetadataCacheException( e.getMessage() );
    }
    finally
    {
      if( lock != null ) lock.release();

      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }

  public byte[] findRaw( ArtifactBasicMetadata bmd )
  throws MetadataCacheException
  {
    GenericEvent event = null;
    
    try
    {
      String rawKey = bmd.getGAV();
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.fsCache, EVENT_FIND_RAW, rawKey );
      
      byte [] res = rawCache.get( rawKey );
      
      if( res != null )
      {
        if( _eventManager != null )
          event.setResult( "found in memory" );
        
        return res;
      }
      
      // locking is provided by underlying OS, don't waste the effort
      File f = new File( getGAVDir( bmd.getEffectiveCoordinates() )
          , bmd.getArtifactId()+FileUtil.DASH+bmd.getVersion()+"."+bmd.getType()
          );
      
      if( ! f.exists() )
        return null;
      
      res = FileUtil.readRawData( f );
      
      rawCache.put( rawKey, res );
      
      if( _eventManager != null )
        event.setResult( "found on disk" );
      
      return res; 
    }
    catch( IOException e )
    {
      throw new MetadataCacheException( e.getMessage() );
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

  public void saveRaw( ArtifactBasicMetadata bmd, byte[] rawBytes )
  throws MetadataCacheException
  {
    GenericEvent event = null;
    
    // locking is provided by underlying OS, don't waste the effort
    try
    {
      String rawKey = bmd.getGAV();
      
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.fsCache, EVENT_SAVE_RAW, rawKey );

      rawCache.put( rawKey, rawBytes );
      
      File f = new File( getGAVDir( bmd.getEffectiveCoordinates() )
          , bmd.getArtifactId()+FileUtil.DASH+bmd.getVersion()+"."+bmd.getType()
          );
      
      FileUtil.writeRawData( f, rawBytes );
    }
    catch( IOException e )
    {
      throw new MetadataCacheException( e.getMessage() );
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
  //---------------------------------------------------------------------------------------
  private String getGAKey( ArtifactCoordinates coord )
  {
    return coord.getGroupId()+":"+coord.getArtifactId();
  }

  private String getGAVKey( ArtifactCoordinates coord )
  {
    return coord.getGroupId()+":"+coord.getArtifactId()+":"+coord.getVersion();
  }
  
  private File getGADir( ArtifactCoordinates coord )
  {
    File dir = new File( root, coord.getGroupId()+FileUtil.SEP+coord.getArtifactId() );
    
    if( ! dir.exists() )
      dir.mkdirs();
    
    return dir;
  }
  
  private File getGAFile( File gaDir, String repoGuid )
  {
    return new File( gaDir, "meta-ga-"+repoGuid+".xml" );
  }

  private File getGAVDir( ArtifactCoordinates coord )
  {
    String version = coord.getVersion();
    
    Quality q = new Quality( version );
    
    if( q.compareTo( Quality.SNAPSHOT_TS_QUALITY ) == 0 )
    {
      DefaultArtifactVersion dav = new DefaultArtifactVersion(version);
      version = dav.getBase()+"-"+ Artifact.SNAPSHOT_VERSION;
    }
    
    File dir = new File( getGADir( coord ), coord.getArtifactId()+FileUtil.DASH+version );
    
    if( ! dir.exists() )
      dir.mkdirs();
    
    return dir;
  }
  
  private File getGAVFile( File gavDir, String repoGuid )
  {
    return new File( gavDir, "meta-gav-"+repoGuid+".xml" );
  }


  public void register( MercuryEventListener listener )
  {
    if( _eventManager == null )
      _eventManager = new EventManager();
      
    _eventManager.register( listener );
  }

  public void unRegister( MercuryEventListener listener )
  {
    if( _eventManager != null )
      _eventManager.unRegister( listener );
  }
  
  public void setEventManager( EventManager eventManager )
  {
    if( _eventManager == null )
      _eventManager = eventManager;
    else
      _eventManager.getListeners().addAll( eventManager.getListeners() );
      
  }
}
