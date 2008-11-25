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
package org.apache.maven.mercury.logging;

import org.apache.maven.mercury.logging.console.MercuryConsoleLoggerFactory;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryLoggerManager
{
  public static final String SYSTEM_PROPERTY_MERCURY_LOG_FACTORY = "maven.mercury.log.factory";
  public static final String _loggerFactoryClassName = System.getProperty( SYSTEM_PROPERTY_MERCURY_LOG_FACTORY, MercuryConsoleLoggerFactory.class.getName() );
  
  public static final String SYSTEM_PROPERTY_MERCURY_LOG_THRESHOLD = "maven.mercury.log.threshold";
  public static final String _loggerThresholdName = System.getProperty( SYSTEM_PROPERTY_MERCURY_LOG_THRESHOLD, MercuryLoggingLevelEnum.error.name() );
  
  static MercuryLoggingLevelEnum _threshold = MercuryLoggingLevelEnum.valueOf( _loggerThresholdName );

  static IMercuryLoggerFactory _loggerFactory;
  
  @SuppressWarnings("unchecked")
  public static final IMercuryLogger getLogger( Class clazz )
  {
    if( _loggerFactory == null )
    {
      try
      {
        _loggerFactory = (IMercuryLoggerFactory)Class.forName( _loggerFactoryClassName ).newInstance();
      }
      catch( Exception e )
      {
        _loggerFactory = new MercuryConsoleLoggerFactory();
        _loggerFactory.getLogger( MercuryLoggerManager.class ).error( "cannot load logger for "+_loggerFactoryClassName, e );
      }
    }
    
    return _loggerFactory.getLogger( clazz );
  }
  
  public static MercuryLoggingLevelEnum getThreshold()
  {
    return _threshold;
  }
}
