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

/**
 * classical depth comparator: shallower is better by default, but that could 
 * be changed by appropriate constructor
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class ClassicDepthComparator
implements Comparator<MetadataTreeNode>
{
  boolean _closerBetter = true;
  
  public ClassicDepthComparator()
  {
  }

  public ClassicDepthComparator( boolean closerBetter )
  {
    _closerBetter = closerBetter;
  }
  
  public int compare( MetadataTreeNode n1, MetadataTreeNode n2 )
  {
    return _closerBetter ? n2.getDepth() - n1.getDepth() : n1.getDepth() - n2.getDepth() ;
  }
}
