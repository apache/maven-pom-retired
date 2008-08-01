/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file                                                                                            
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.maven.mercury.transport.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Binding <p/> A Binding represents a remote uri whose contents are to be
 * downloaded and stored in a locally, or a local resource whose contents are to
 * be uploaded to the remote uri.
 */
public class Binding
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Binding.class );
  
  protected URL                 remoteResource;
  protected File                localFile;
  /** 
   * inbound in-memory binding for reading remote content.
   * It is created by the constructor
   */
  protected ByteArrayOutputStream localOS;
  /**
   * this is outbound in-memory binding. IS is passed by the client
   */
  protected InputStream         localIS;

  protected Exception error;

  public Binding()
  {
  }

  public Binding(
      URL remoteUrl,
      File localFile)
  {
    this.remoteResource = remoteUrl;
    this.localFile = localFile;

  }

  /** 
   * this is in-memory binding for writing remote content into localOS
   * 
   * @param remoteUrl
   * @param lenientChecksum
   */
  public Binding( URL remoteUrl )
  {
    this.remoteResource = remoteUrl;
    // let's assume 4k on average
    this.localOS = new ByteArrayOutputStream( 4*1024 );
  }

  /**
   * outbound constructor - send contents of the stream to remoteUrl
   * 
   * @param remoteUrl
   * @param is
   */
  public Binding( URL remoteUrl, InputStream is )
  {
    this.remoteResource = remoteUrl;
    this.localIS = is;
  }

  public URL getRemoteResource()
  {
    return remoteResource;
  }

  public void setRemoteResource( URL remoteResource )
  {
    this.remoteResource = remoteResource;
  }

  public Exception getError()
  {
    return error;
  }

  public void setError( Exception error )
  {
    this.error = error;
  }
  
  public boolean isInMemory()
  {
    return (!isFile() && (localIS != null || localOS != null));
  }
  
  public boolean isFile()
  {
    return localFile != null;
  }
  
  public byte [] getInboundContent()
  {
    if( localOS != null )
      return localOS.toByteArray();
    
    return null;
  }
  
  public OutputStream getLocalOutputStream()
  {
      return localOS;
  }
  
  public InputStream getLocalInputStream()
  {
      return localIS;
  }
  
  public File getLocalFile ()
  {
      return localFile;
  }

}
