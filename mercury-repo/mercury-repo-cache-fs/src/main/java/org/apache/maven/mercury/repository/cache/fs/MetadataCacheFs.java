package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryGAMetadata;
import org.apache.maven.mercury.repository.api.RepositoryGAVMetadata;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.util.FileUtil;
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
   * 
   */
  private MetadataCacheFs( File root )
  {
    this.root = root;
  }

  public RepositoryGAMetadata findGA( RemoteRepository repo, ArtifactBasicMetadata bmd )
  {
    File gam = getGADir( bmd );
    
    return null;
  }

  public RepositoryGAMetadata findGAV( RemoteRepository repo, ArtifactBasicMetadata bmd )
  {
    return null;
  }
  
  private File getGADir( ArtifactBasicMetadata bmd )
  {
    File dir = new File( root, bmd.getGroupId()+FileUtil.SEP+bmd.getArtifactId() );
    
    if( ! dir.exists() )
      dir.mkdirs();
    
    return dir;
  }
  
  private String getGAFileName( RemoteRepository repo )
  {
    return "";
  }

  private File getGAVDir( ArtifactBasicMetadata bmd )
  {
    String version = bmd.getVersion();
    
    Quality q = new Quality( version );
    
    if( q.compareTo( Quality.SNAPSHOT_TS_QUALITY ) == 0 )
    {
      version = Artifact.SNAPSHOT_VERSION;
    }
    
    File dir = new File( getGADir( bmd ), bmd.getArtifactId()+FileUtil.DASH+version );
    
    if( ! dir.exists() )
      dir.mkdirs();
    
    return dir;
  }
  
}
