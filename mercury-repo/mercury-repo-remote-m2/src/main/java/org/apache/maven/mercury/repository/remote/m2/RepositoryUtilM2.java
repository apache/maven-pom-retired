package org.apache.maven.mercury.repository.remote.m2;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryUtilM2
{
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( RepositoryUtilM2.class ); 
  private static final Language _lang = new DefaultLanguage( RepositoryUtilM2.class );
  //----------------------------------------------------------------------------------
  public static void flip( LocalRepositoryM2 repo, File dest )
  {
    if( repo == null )
      throw new IllegalArgumentException( _lang.getMessage( "lrepo.null" ) );

    File lDir = repo.getDirectory();
    
    if( lDir == null )
      throw new IllegalArgumentException( _lang.getMessage( "lrepo.no.dir" ) );
    
    if( !lDir.exists() || !lDir.isDirectory() )
      throw new IllegalArgumentException( _lang.getMessage( "lrepo.dir.not.exists", lDir.toString() ) );
    
    if( dest.exists() && !dest.isFile() )
      throw new IllegalArgumentException( _lang.getMessage( "dest.is.file", dest.toString() ) );
    
    
  }
  //----------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------
}
//=====================================================================================
class copyGa
implements Runnable
{
  ConcurrentLinkedQueue<ArtifactCoordinates> _q;
  File _root;
  File _dest;
  
  public copyGa( ConcurrentLinkedQueue<ArtifactCoordinates> q, File from, File to )
  {
    this._q = q;
    this._root = from;
    this._dest = to;
  }

  // copy GAV to it's final dest
  public void run()
  {
    try
    {
      for(;;)
      {
        ArtifactCoordinates ga = _q.remove();
        
//        File gaDir = new File( _root );
//        File gaDest = new File( _dest );
//        
//        gaDest.mkdirs();
//        
//        Metadata gaMd = new Metadata();
//        gaMd.setGroupId( groupId )
//        
//        File [] versions = gaDir.listFiles(); 
//        
//        for( File v : versions )
//        {
//          if( v.isFile() )
//            continue;
//        }
        
      }
    }
    catch( NoSuchElementException e )
    {
    }
  }
  //-------------------------------------
}
//=====================================================================================

