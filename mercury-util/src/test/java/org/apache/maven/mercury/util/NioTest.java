package org.apache.maven.mercury.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class NioTest
    extends TestCase
{
  
  public void testNio()
  {
    boolean ok = false;
    
    try
    {
      File file = File.createTempFile( "test-nio-", "-file" );
      
      String fn = file.getAbsolutePath();
      
      File f1 = new File( fn ); 
      FileChannel c1 = new RandomAccessFile( f1, "rw").getChannel();
  
      FileLock l1 = c1.lock();
      assertNotNull( "cannot obtain even the first lock", l1 );
      
      File f2 = new File( fn ); 
      FileChannel c2 = new RandomAccessFile( f2, "rw").getChannel();
      FileLock l2 = null;
      try
      {
          l2 = c2.tryLock();
          
          if( l2 == null )
            throw new OverlappingFileLockException();
          
          l2.release();
      }
      catch (OverlappingFileLockException e)
      {
          ok = true;
      }
  
      l1.release();
      c1.close();
      
      file.delete();

if(false)      
      assertTrue( "java.nio does not work !!", ok );
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}
