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
package org.apache.maven.mercury.repository.remote.m2;

import java.io.File;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RemoteRepositoryUtilM2Test
    extends TestCase
{
  public void testFlipFolder()
  {
    File repo = new File("./target/test-classes/localTestRepo");
    
    RepositoryUtilM2.flipLocalFolderToRemoteRepository( repo );
    
    File ga = new File( repo, "org/apache/maven/mercury/mercury-pom/maven-metadata.xml");
    
    assertTrue( ga.exists() );
    
    File gav = new File( repo, "org/apache/maven/mercury/mercury-pom/1.0.0-alpha-2-SNAPSHOT/maven-metadata.xml");
    
  }
}
