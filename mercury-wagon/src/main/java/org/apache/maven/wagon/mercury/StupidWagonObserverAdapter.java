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
package org.apache.maven.wagon.mercury;

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.wagon.events.TransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class StupidWagonObserverAdapter
implements StreamObserver
{
  private static final Logger _log = LoggerFactory.getLogger(StupidWagonObserverAdapter.class);
  byte [] buf = new byte[2048];

  MercuryWagon wagon;
  TransferEvent event;
  
  long length = -1L;
  String lastModified;
  
  public StupidWagonObserverAdapter( MercuryWagon wagon, TransferEvent event )
  {
    this.wagon = wagon;
    this.event = event;
    if( _log.isDebugEnabled() )
      _log.debug( "|=========-> adapter for "+event.getResource().getName()+" is created" );
  }

  public void byteReady( int b )
  throws StreamObserverException
  {
    bytesReady( new byte [] { (byte)b }, 0, 1 );
  }

  public void bytesReady( byte[] b, int off, int len )
  throws StreamObserverException
  {
    if( len > buf.length )
      buf = new byte[len];
    
    System.arraycopy( b, off, buf, 0, len );
    wagon.bytesReady( event, buf, len );
  }

  public long getLength()
  {
    return length;
  }

  public void setLength( long length )
  {
    this.length = length;
_log.info( "|=-> length is "+length );
  }

  public void setLastModified(String time)
  {
    lastModified = time;
  }

  public String getLastModified()
  {
    return lastModified;
  }

}
