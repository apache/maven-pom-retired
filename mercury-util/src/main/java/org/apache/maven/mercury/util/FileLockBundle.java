package org.apache.maven.mercury.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class FileLockBundle
{
  String dir;
  FileChannel channel;
  FileLock lock;
  
  boolean fileLock = false;
  
  /**
   * @param dir
   * @param channel
   * @param lock
   */
  public FileLockBundle(
                  String dir,
                  FileChannel channel,
                  FileLock lock
                      )
  {
    this.dir = dir;
    this.channel = channel;
    this.lock = lock;
  }

  /**
   * @param dir
   * @param channel
   * @param lock
   */
  public FileLockBundle( String dir )
  {
    this.dir = dir;
    this.fileLock = true;
  }
  
  public void release()
  {
    if( lock == null )
    {
      if( fileLock  )
        FileUtil.unlockDir( dir );

      return;
    }
    
    try
    {
      lock.release();
      channel.close();
    }
    catch( IOException any ){}
  }
}
