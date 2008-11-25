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
package org.apache.maven.mercury.metadata.sat;

import java.util.Comparator;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.event.EventGenerator;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public interface SatSolver
extends EventGenerator
{
  public static final int DEFAULT_TREE_SIZE = 128; //nodes
  
  public static final String EVENT_SOLVE = "solve";
  public static final String EVENT_CREATE_SOLVER = "create.sat.solver";
  
  /**
   * 
   * @param sorts - policies expressed as sorted list of node sorters - from most important to the least
   * @throws SatException
   */
  public void applyPolicies( List< Comparator<MetadataTreeNode> > comparators )
  throws SatException;
  
  /**
   * 
   * @return list of ArtifactMetedata's in the solution
   * @throws SatException
   */
  public List<ArtifactMetadata> solve()
  throws SatException;
}
