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

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractMercuryLogger
{
  protected String _className;
  
  MercuryLoggingLevelEnum _threshold = MercuryLoggerManager.getThreshold();
  
  
  @SuppressWarnings("unchecked")
  public AbstractMercuryLogger( Class clazz )
  {
    _className = clazz.getName();
  }
  
  public boolean isDebugEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.debug.getId();
  }

  public boolean isErrorEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.error.getId();
  }

  public boolean isFatalEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.error.getId();
  }

  public boolean isInfoEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.info.getId();
  }

  public boolean isWarnEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.warn.getId();
  }
}
