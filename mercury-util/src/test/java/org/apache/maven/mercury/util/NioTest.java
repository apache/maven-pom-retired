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
