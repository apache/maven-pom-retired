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
package org.apache.maven.mercury.artifact.version;

import java.util.Comparator;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * version comparator used elsewhere to keep version collections sorted
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataVersionComparator
implements Comparator<ArtifactBasicMetadata>
{
  private static final Language _lang = new DefaultLanguage( MetadataVersionComparator.class );
  
  public int compare( ArtifactBasicMetadata v1, ArtifactBasicMetadata v2 )
  {
    if( v1 == null || v2 == null )
      throw new IllegalArgumentException( _lang.getMessage( "null.version.to.compare", v1 == null ? "null" : v1.toString(), v2 == null ? "null" : v2.toString() )  );
    
    DefaultArtifactVersion av1 = new DefaultArtifactVersion( v1.getVersion() );
    DefaultArtifactVersion av2 = new DefaultArtifactVersion( v2.getVersion() );
    
    return av1.compareTo( av2 );
  }

}
