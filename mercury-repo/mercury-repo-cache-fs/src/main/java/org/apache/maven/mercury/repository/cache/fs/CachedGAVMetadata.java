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
package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.repository.api.MetadataCorruptionException;
import org.apache.maven.mercury.repository.api.RepositoryGAVMetadata;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class CachedGAVMetadata
extends RepositoryGAVMetadata
{
  public static final String ELEM_SNAPSHOTS = "snapshots";
  public static final String ATTR_SNAPSHOT = "snapshot";

  public static final String ELEM_CLASSIFIERS = "classifiers";
  public static final String ATTR_CLASSIFIER = "classifier";

  CachedMetadata cm;
  
  public CachedGAVMetadata( File mdFile )
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataCorruptionException
  {
    cm = new CachedMetadata( mdFile );
    fromXml();
  }
  
  public CachedGAVMetadata( RepositoryGAVMetadata gavm )
  throws MetadataException
  {
    super( gavm );
    
    cm = new CachedMetadata();
    
    toXml();
  }
  
  /**
   * fill GA with data from cm
   * 
   * @throws MetadataCorruptionException 
   */
  private void fromXml()
  throws MetadataCorruptionException
  {
    gav = new ArtifactCoordinates( 
                    cm.getAttribute( CachedGAMetadata.ELEM_COORDINATES, CachedGAMetadata.ATTR_GROUP_ID, true ) 
                  , cm.getAttribute( CachedGAMetadata.ELEM_COORDINATES, CachedGAMetadata.ATTR_ARTIFACT_ID, true ) 
                  , cm.getAttribute( CachedGAMetadata.ELEM_COORDINATES, CachedGAMetadata.ATTR_VERSION, true ) 
                                );
    
    List<String> snList = cm.findAttributes( ELEM_SNAPSHOTS, ATTR_SNAPSHOT );
    
    if( ! Util.isEmpty( snList ) )
      this.snapshots.addAll( snList );
    
    List<String> clList = cm.findAttributes( ELEM_CLASSIFIERS, ATTR_CLASSIFIER );
    
    if( ! Util.isEmpty( clList ) )
      this.classifiers.addAll( clList );

    String lChk = cm.getLastUpdate();

    lastCheck = Long.parseLong( lChk );
  }
  
  private void toXml()
  {
    cm.clean();
    
    cm.setAttribute( CachedGAMetadata.ELEM_COORDINATES, CachedGAMetadata.ATTR_GROUP_ID, gav.getGroupId() );
    cm.setAttribute( CachedGAMetadata.ELEM_COORDINATES, CachedGAMetadata.ATTR_ARTIFACT_ID, gav.getArtifactId() );
    cm.setAttribute( CachedGAMetadata.ELEM_COORDINATES, CachedGAMetadata.ATTR_VERSION, gav.getVersion() );
    
    if( !Util.isEmpty( snapshots ) )
      cm.setAttribute( ELEM_SNAPSHOTS, ATTR_SNAPSHOT, snapshots );
    
    cm.setLastUpdate( ""+lastCheck );
  }
  
}
