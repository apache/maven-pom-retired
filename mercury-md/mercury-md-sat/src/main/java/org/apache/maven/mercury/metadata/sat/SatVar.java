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

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
class SatVar
{
  MetadataTreeNode _node;
  int _literal;
  boolean _optional;
  //---------------------------------------------------------------------
  public SatVar( MetadataTreeNode n )
  throws SatException
  {
    if( n == null
        || n.getMd() == null
    )
      throw new SatException("Cannot create SatVar from a null MetadataTreeNode: "+n);
    
    ArtifactMetadata md = n.getMd();
    if(    
       md == null
      || md.getGroupId() == null
      || md.getArtifactId() == null
      || md.getVersion() == null
    )
      throw new SatException("Cannot create SatVar from a null Metadata: "+md);

    this._node = n;
    this._literal = n.getId();
  }
  //---------------------------------------------------------------------
  public ArtifactMetadata getMd()
  {
    return _node.getMd();
  }

  public int getLiteral()
  {
    return _literal;
  }
  //---------------------------------------------------------------------
  @Override
  public String toString()
  {
    return _node.toString()+" -> X"+_literal;
  }
  
  //---------------------------------------------------------------------
  //---------------------------------------------------------------------
}
