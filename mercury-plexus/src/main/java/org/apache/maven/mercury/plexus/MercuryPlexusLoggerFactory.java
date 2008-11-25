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
package org.apache.maven.mercury.plexus;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.IMercuryLoggerFactory;
import org.apache.maven.mercury.logging.MercuryLoggingLevelEnum;
import org.codehaus.plexus.logging.LoggerManager;

/**
 * mercury adaptor for plesux logger factory (manager)
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryPlexusLoggerFactory
implements IMercuryLoggerFactory
{
  LoggerManager _loggerManager;
  
  public MercuryPlexusLoggerFactory( LoggerManager loggerManager )
  {
    setLoggerFactory( loggerManager );
  }

  public IMercuryLogger getLogger( Class clazz )
  {
    return new MercuryPlexusLogger( _loggerManager.getLoggerForComponent( clazz.getName() ) );
  }

  public void setLoggerFactory( LoggerManager loggerManager )
  {
    this._loggerManager = loggerManager;
  }

  public void setThreshold( MercuryLoggingLevelEnum threshold )
  {
    // TODO Auto-generated method stub
    
  }

}
