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
import org.codehaus.plexus.logging.Logger;

/**
 * Mercury adaptor for plexus logger
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryPlexusLogger
implements IMercuryLogger
{
  Logger _logger;
  
  public MercuryPlexusLogger( Logger logger )
  {
    this._logger = logger;
  }

  public void debug( String message )
  {
    _logger.debug( message );
  }

  public void debug( String message, Throwable throwable )
  {
    _logger.debug( message, throwable );
  }

  public void error( String message )
  {
    _logger.error( message );
  }

  public void error( String message, Throwable throwable )
  {
    _logger.error( message, throwable );
  }

  public void fatal( String message )
  {
    _logger.fatalError( message );
  }

  public void fatal( String message, Throwable throwable )
  {
    _logger.fatalError( message, throwable );
  }

  public void info( String message )
  {
    _logger.info( message );
  }

  public void info( String message, Throwable throwable )
  {
    _logger.info( message, throwable );
  }

  public void warn( String message )
  {
    _logger.warn( message );
  }

  public void warn( String message, Throwable throwable )
  {
    _logger.warn( message, throwable );
  }

  public boolean isDebugEnabled()
  {
    return _logger.isDebugEnabled();
  }

  public boolean isErrorEnabled()
  {
    return _logger.isErrorEnabled();
  }

  public boolean isFatalEnabled()
  {
    return _logger.isFatalErrorEnabled();
  }

  public boolean isInfoEnabled()
  {
    return _logger.isInfoEnabled();
  }

  public boolean isWarnEnabled()
  {
    return _logger.isWarnEnabled();
  }
}
