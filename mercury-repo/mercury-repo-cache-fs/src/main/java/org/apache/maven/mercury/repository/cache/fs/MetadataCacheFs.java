package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryGAMetadata;
import org.apache.maven.mercury.repository.api.RepositoryGAVMetadata;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.repository.api.RepositoryUpdatePolicy;
import org.apache.maven.mercury.util.FileLockBundle;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

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
  private static final Language _lang = new DefaultLanguage( RepositoryGAVMetadata.class );
  
  static volatile Map<String, MetadataCacheFs> fsCaches = Collections.synchronizedMap( new HashMap<String, MetadataCacheFs>(2) ); 
  
  volatile HashMap<String, RepositoryGAMetadata> gaCache;
  volatile HashMap<String, RepositoryGAVMetadata> gavCache;
  
  private File root;
  
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
    try
    {
      File gaDir = getGADir(coord);
      
      File gamF = getGAFile( gaDir, repoGuid );
      
      CachedGAMetadata md = null;
      
      if( gamF.exists() )
      {
          md = new CachedGAMetadata( gamF );
          
          if( up != null && up.timestampExpired( md.getLastCheck() ) )
            md.setExpired( true );
      }
      
      return md;
    }
    catch( Exception e )
    {
      throw new MetadataCorruptionException( e.getMessage() );
    }
  }

  public RepositoryGAVMetadata findGAV( String repoGuid, RepositoryUpdatePolicy up, ArtifactCoordinates coord )
  throws MetadataCorruptionException
  {
    FileLockBundle lock = null;
    try
    {
      File gavDir = getGAVDir( coord );

      lock = FileUtil.lockDir( gavDir.getCanonicalPath(), 500L, 5L );
      
      File gavmF = getGAVFile( gavDir, repoGuid );
      
      CachedGAVMetadata md = null;
      
      if( gavmF.exists() )
      {
          md = new CachedGAVMetadata( gavmF );
          
          if( up != null && up.timestampExpired( md.getLastCheck() ) )
            md.setExpired( true );
      }
      
      return md;
    }
    catch( Exception e )
    {
      throw new MetadataCorruptionException( e.getMessage() );
    }
    finally
    {
      if( lock != null ) lock.release();
    }
  }
  
  public void updateGA( String repoGuid, RepositoryGAMetadata gam )
  throws MetadataCacheException
  {
    FileLockBundle lock = null;
    try
    {
      File gaDir = getGADir( gam.getGA() );
      
      lock = FileUtil.lockDir( gaDir.getCanonicalPath(), 500L, 5L );
      
      File gamF = getGAFile( gaDir, repoGuid );
      
      CachedGAMetadata md = new CachedGAMetadata( gam );
      
      md.cm.save( gamF );
    }
    catch( Exception e )
    {
      throw new MetadataCacheException( e.getMessage() );
    }
    finally
    {
      if( lock != null ) lock.release();
    }
  }

  /* (non-Javadoc)
   * @see org.apache.maven.mercury.repository.api.RepositoryMetadataCache#updateGAV(java.lang.String, org.apache.maven.mercury.repository.api.RepositoryGAVMetadata)
   */
  public void updateGAV( String repoGuid, RepositoryGAVMetadata gavm )
  throws MetadataCacheException
  {
    try
    {
      File gavDir = getGAVDir( gavm.getGAV() );
      
      File gavmF = getGAVFile( gavDir, repoGuid );
      
      CachedGAVMetadata md = new CachedGAVMetadata( gavm );
      
      md.cm.save( gavmF );
    }
    catch( Exception e )
    {
      throw new MetadataCacheException( e.getMessage() );
    }
  }

  public byte[] findRaw( ArtifactBasicMetadata bmd )
  throws MetadataCacheException
  {
    try
    {
      // locking is provided by underlying OS, don't waste the effort
      File f = new File( getGAVDir( bmd.getEffectiveCoordinates() )
          , bmd.getArtifactId()+FileUtil.DASH+bmd.getVersion()+"."+bmd.getType()
          );
      
      if( ! f.exists() )
        return null;
      
      return FileUtil.readRawData( f );
    }
    catch( IOException e )
    {
      throw new MetadataCacheException( e.getMessage() );
    }
  }

  public void saveRaw( ArtifactBasicMetadata bmd, byte[] rawBytes )
  throws MetadataCacheException
  {
    // locking is provided by underlying OS, don't waste the effort
    try
    {
      File f = new File( getGAVDir( bmd.getEffectiveCoordinates() )
          , bmd.getArtifactId()+FileUtil.DASH+bmd.getVersion()+"."+bmd.getType()
          );
      
      FileUtil.writeRawData( f, rawBytes );
    }
    catch( IOException e )
    {
      throw new MetadataCacheException( e.getMessage() );
    }
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

}
