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
package org.apache.maven.mercury.metadata;

import java.util.Comparator;

public class ClassicDepthComparatorTest
extends AbstractSimpleTreeTest
{
  
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();
  }

  public void testNearestBest()
  {
    Comparator<MetadataTreeNode> comparator = new ClassicDepthComparator();
    
    int res = comparator.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be closer'n cc1bb1",  res > 0 );
    
    res = comparator.compare( bb1, cc1 );
    assertTrue( "bb1 should be the same as cc1",  res == 0 );
    
    res = comparator.compare( cc1, aa1 );
    assertTrue( "cc1 should be deeper'n aa11",  res < 0 );
  }

  public void testFarestBest()
  {
    Comparator<MetadataTreeNode> comparator = new ClassicDepthComparator(false);
    
    int res = comparator.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be closer'n cc1bb1",  res < 0 );
    
    res = comparator.compare( bb1, cc1 );
    assertTrue( "bb1 should be the same as cc1",  res == 0 );
    
    res = comparator.compare( cc1, aa1 );
    assertTrue( "cc1 should be deeper'n aa11",  res > 0 );
  }
}
