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
import java.util.Collection;
import java.util.List;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class ArtifactMetadataList
{
  List<ArtifactBasicMetadata> _artifacts = new ArrayList<ArtifactBasicMetadata>(8);

  public ArtifactMetadataList( ArtifactBasicMetadata... md )
  {
    for( ArtifactBasicMetadata m : md )
      add( m );
  }

  public ArtifactMetadataList( Collection<ArtifactBasicMetadata> md )
  {
    add( md );
  }

  public ArtifactMetadataList( String... mds )
  {
    for( String m : mds )
      add( new ArtifactBasicMetadata(m) );
  }
  
  public void add( ArtifactBasicMetadata md )
  {
    _artifacts.add( md );
  }
  
  public void add( Collection<ArtifactBasicMetadata> md )
  {
    _artifacts.addAll( md );
  }
  
  public void addGav( String md )
  {
    _artifacts.add( new ArtifactBasicMetadata(md) );
  }
  
  public void addByGav( Collection<String> mds )
  {
    for( String m : mds )
      _artifacts.add( new ArtifactBasicMetadata(m) );
  }
  
  public List<ArtifactBasicMetadata> getMetadataList()
  {
    return _artifacts;
  }
  
  public int size()
  {
    return _artifacts.size();
  }
  
  public boolean isEmpty()
  {
    return _artifacts.isEmpty();
  }
  
  public void clear()
  {
    _artifacts.clear();
  }
}
