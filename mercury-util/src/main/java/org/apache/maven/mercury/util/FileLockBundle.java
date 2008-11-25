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
