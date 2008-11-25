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
package org.apache.maven.mercury.logging.console;

import org.apache.maven.mercury.logging.AbstractMercuryLogger;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggingLevelEnum;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryConsoleLogger
extends AbstractMercuryLogger
implements IMercuryLogger
{
  
  @SuppressWarnings("unchecked")
  public MercuryConsoleLogger( Class clazz )
  {
    super( clazz );
  }
  
  private static final void say( MercuryLoggingLevelEnum level, String message, Throwable throwable )
  {
    System.out.print( "["+level.name()+"] " );
    System.out.println( message );
    if( throwable != null )
    {
      throwable.printStackTrace( System.out );
    }
  }

  public void debug( String message )
  {
    if( isDebugEnabled() )
      say( MercuryLoggingLevelEnum.debug, message, null );
  }

  public void debug( String message, Throwable throwable )
  {
    if( isDebugEnabled() )
      say( MercuryLoggingLevelEnum.debug, message, throwable );
  }

  public void error( String message )
  {
  }

  /* (non-Javadoc)
   * @see org.apache.maven.mercury.logging.MercuryLogger#error(java.lang.String, java.lang.Throwable)
   */
  public void error(
      String message,
      Throwable throwable )
  {
    // TODO Auto-generated method stub

  }

  public void fatal( String message )
  {
  }

  public void fatal( String message, Throwable throwable )
  {
  }

  public int getThreshold()
  {
    return 0;
  }

  public void info( String message )
  {
  }

  public void info(
      String message,
      Throwable throwable )
  {
    // TODO Auto-generated method stub

  }

  public void warn( String message )
  {
    // TODO Auto-generated method stub

  }

  public void warn( String message, Throwable throwable )
  {
    // TODO Auto-generated method stub

  }

}
