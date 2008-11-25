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

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class TransferEventDebugger
implements TransferListener
{
  public static final String SYSTEM_PARAMETER_DEBUG_TRANSFER_BYTES = "maven.mercury.wagon.debug.transfer.bytes";
  private boolean debugTransferBytes = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_DEBUG_TRANSFER_BYTES, "false" ) );

  private static final Logger _log = LoggerFactory.getLogger(TransferEventDebugger.class);

  public void debug( String message )
  {
  }

  public void transferCompleted(
      TransferEvent transferEvent )
  {
    _log.info("|=============>   completed: "+transferEvent.getResource().getName() );
  }

  public void transferError( TransferEvent transferEvent )
  {
    _log.info("|=============>   error: "+transferEvent.getResource().getName() );
  }

  public void transferInitiated( TransferEvent transferEvent )
  {
    _log.info("|=============>   initialized: "+transferEvent.getResource().getName() );
  }

  public void transferProgress(
      TransferEvent transferEvent,
      byte[] buffer,
      int length )
  {
    if( debugTransferBytes )
      _log.info("|=============>   ready "+length+" bytes : "+transferEvent.getResource().getName() );
  }

  public void transferStarted(
      TransferEvent transferEvent )
  {
    _log.info("|=============>   started: "+transferEvent.getResource().getName() );
  }

}
