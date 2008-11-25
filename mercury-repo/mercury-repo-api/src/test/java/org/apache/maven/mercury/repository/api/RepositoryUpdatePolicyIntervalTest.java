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
package org.apache.maven.mercury.repository.api;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryUpdatePolicyIntervalTest
    extends TestCase
{
  RepositoryUpdateIntervalPolicy in;
  long now;
  
  @Override
  protected void setUp()
      throws Exception
  {
    in = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_NAME_INTERVAL+"1" );
    now = System.currentTimeMillis();
  }

  public void testInterval()
  {
    assertFalse( in.timestampExpired( now ) );
    assertTrue( in.timestampExpired( now-80000L ) );
  }

  public void testDayly()
  {
    in = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_NAME_DAILY );

    assertFalse( in.timestampExpired( now ) );
    assertFalse( in.timestampExpired( now-80000L ) );
    assertTrue( in.timestampExpired( now - 24L*3600L*1000L - 80000L ) );
  }
}
