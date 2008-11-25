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
package org.apache.maven.mercury.artifact;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.version.VersionException;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DependencyFilterTest
    extends TestCase
{
  ArtifactBasicMetadata a1;
  ArtifactBasicMetadata a2;
  ArtifactBasicMetadata a3;
  ArtifactBasicMetadata a4;

  ArrayList<ArtifactBasicMetadata> inc;
  ArrayList<ArtifactBasicMetadata> exc;
  
  @Override
  protected void setUp()
  throws Exception
  {
    a1 = new ArtifactBasicMetadata("a:a:1.1");
    a2 = new ArtifactBasicMetadata("a:a:2.1");
    a3 = new ArtifactBasicMetadata("a:a:3.1");
    a4 = new ArtifactBasicMetadata("a:a:4.1");
    
    inc = new ArrayList<ArtifactBasicMetadata>();
    inc.add(  new ArtifactBasicMetadata("a:a") );
    inc.add(  new ArtifactBasicMetadata("b:b:2.0.0") );
    
    exc = new ArrayList<ArtifactBasicMetadata>();
    exc.add(  new ArtifactBasicMetadata("c:c") );
    exc.add(  new ArtifactBasicMetadata("b:b:2.0.1") );
    
    a2.setInclusions( inc );
    
    a3.setExclusions( exc );
    
    a4.setInclusions( inc );
    a4.setExclusions( exc );
  }
  
  public void testNoFilter()
  throws VersionException
  {
    assertTrue( a1.allowDependency( new ArtifactBasicMetadata("a:a:2.0.0") ) );
    assertTrue( a1.allowDependency( new ArtifactBasicMetadata("b:b:1.0.0") ) );
    assertTrue( a1.allowDependency( new ArtifactBasicMetadata("c:c:1.0.0") ) );
  }
  
  public void testInclusionsFilter()
  throws VersionException
  {
    assertTrue( a2.allowDependency( new ArtifactBasicMetadata("a:a:2.0.0") ) );
    assertFalse( a2.allowDependency( new ArtifactBasicMetadata("b:b:1.0.0") ) );
    assertTrue( a2.allowDependency( new ArtifactBasicMetadata("b:b:2.0.0") ) );
    assertFalse( a2.allowDependency( new ArtifactBasicMetadata("b:b:2.0.1") ) );
    assertFalse( a2.allowDependency( new ArtifactBasicMetadata("c:c:1.0.0") ) );
  }
  
  public void testExclusionsFilter()
  throws VersionException
  {
    assertTrue( a3.allowDependency( new ArtifactBasicMetadata("a:a:2.0.0") ) );
    assertTrue( a3.allowDependency( new ArtifactBasicMetadata("b:b:1.0.0") ) );
    assertFalse( a3.allowDependency( new ArtifactBasicMetadata("b:b:2.0.1") ) );
    assertFalse( a3.allowDependency( new ArtifactBasicMetadata("c:c:1.0.0") ) );
  }
  
  public void testInclusionsExclusionsFilter()
  throws VersionException
  {
    assertTrue( a4.allowDependency( new ArtifactBasicMetadata("a:a:2.0.0") ) );
    assertFalse( a4.allowDependency( new ArtifactBasicMetadata("b:b:1.0.0") ) );
    assertTrue( a4.allowDependency( new ArtifactBasicMetadata("b:b:2.0.0") ) );
    assertFalse( a4.allowDependency( new ArtifactBasicMetadata("b:b:2.0.1") ) );
    assertFalse( a4.allowDependency( new ArtifactBasicMetadata("b:b:3.0.1") ) );
    assertFalse( a4.allowDependency( new ArtifactBasicMetadata("c:c:1.0.0") ) );
  }

}
