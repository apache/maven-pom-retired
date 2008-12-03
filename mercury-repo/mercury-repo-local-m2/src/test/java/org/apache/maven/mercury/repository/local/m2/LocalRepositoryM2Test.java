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
package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.repository.api.ArtifactResults;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class LocalRepositoryM2Test
    extends TestCase
{
  File dir;
  LocalRepositoryM2 repo;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
      throws Exception
  {
    dir = new File("./target/test-classes/repo");
    
    repo = new LocalRepositoryM2( "test", dir, new MetadataProcessorMock() );
    
  }
  
  
  /**
   * this does not fully test this use case, full test belongs to ITs. See http://jira.codehaus.org/browse/MERCURY-47
   * 
   * @throws Exception
   */
  public void testReadTwice()
  throws Exception
  {
    String artifactId = "a:a:4";
    
    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata(artifactId);
    
    ArrayList<ArtifactBasicMetadata> q = new ArrayList<ArtifactBasicMetadata>();
    
    q.add( bmd );
    
    ArtifactResults res = repo.getReader().readArtifacts( q );
    
    assertNotNull( res );
    
   assertFalse( res.hasExceptions() );
   
   assertTrue( res.hasResults() );
   
   List<Artifact> arts = res.getResults( bmd );
   
   assertNotNull( arts );
   
   assertEquals( 1, arts.size() );
   
   Artifact a = arts.get( 0 );
   
   assertNotNull( a );
   
   File f = a.getFile();
   
   assertNotNull( f );
   
   assertTrue( f.exists() );
   
   assertEquals( 14800, f.length() );
   
   // second time
   
   res = repo.getReader().readArtifacts( q );
   
   assertNotNull( res );
   
  assertFalse( res.hasExceptions() );
  
  assertTrue( res.hasResults() );
  
  arts = res.getResults( bmd );
  
  assertNotNull( arts );
  
  assertEquals( 1, arts.size() );
  
  a = arts.get( 0 );
  
  assertNotNull( a );
  
  f = a.getFile();
  
  assertNotNull( f );
  
  assertTrue( f.exists() );
  
  assertEquals( 14800, f.length() );
  
  }

}
