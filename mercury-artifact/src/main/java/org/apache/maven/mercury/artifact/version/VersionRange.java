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
package org.apache.maven.mercury.artifact.version;

import org.apache.maven.mercury.artifact.api.Configurable;

/**
 * interface to the version range processor. To be implemented for various syntaxes/interpreters
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface VersionRange
extends Configurable
{
  /**
   * returns true if the supplied version fits into the range
   * 
   * @param version to test
   * @return 
   */
  public boolean includes( String version );
  /**
   * @return true if the range is good old single version, not a true range
   */
  public boolean isSingleton();
}
