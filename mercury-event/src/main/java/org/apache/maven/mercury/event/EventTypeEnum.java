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
package org.apache.maven.mercury.event;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public enum EventTypeEnum
{
      dependencyBuilder(0)
    , satSolver(1)
    
    , virtualRepositoryReader(2)
    
    , localRepository(3)
    , localRepositoryReader(4)
    , localRepositoryWriter(5)
    
    , remoteRepository(6)
    , remoteRepositoryReader(7)
    , remoteRepositoryWriter(8)
    
    , cache(9)
    , fsCache(10)
    ;
    
    int bitNo;
    
    EventTypeEnum( int bitNo )
    {
      this.bitNo = bitNo;
    }
  }
