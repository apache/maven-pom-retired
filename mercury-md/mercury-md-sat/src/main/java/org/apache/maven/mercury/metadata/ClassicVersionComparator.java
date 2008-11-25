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

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;

/**
 * classical version comparator: newer is better by default, but that could 
 * be changed by appropriate constructor
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class ClassicVersionComparator
implements Comparator<MetadataTreeNode>
{
  boolean _newerBetter = true;
  
  public ClassicVersionComparator()
  {
  }

  public ClassicVersionComparator( boolean newerBetter )
  {
    _newerBetter = newerBetter;
  }
  
  public int compare( MetadataTreeNode n1, MetadataTreeNode n2 )
  {
    ArtifactMetadata md1 = n1.getMd();
    DefaultArtifactVersion v1 = new DefaultArtifactVersion( md1.getVersion() );
    
    ArtifactMetadata md2 = n2.getMd();
    DefaultArtifactVersion v2 = new DefaultArtifactVersion( md2.getVersion() );
    
    return _newerBetter ? v1.compareTo(v2) : v2.compareTo(v1) ;
  }
  
}
