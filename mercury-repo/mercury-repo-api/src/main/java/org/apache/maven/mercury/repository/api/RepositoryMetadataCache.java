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
package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.event.EventGenerator;

/**
 * this object abstracts the existence of multiple repositories and repository
 * policies. Given a metadata request, implementor of this interface will
 * either return a local copy if one exists, or will go out and read from a remote repo
 * if either local copy does not exist or remote repository policy allows a read 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryMetadataCache
extends EventGenerator
{
  /**
   * check if GA level metadata exists in this cache for the given repo. Read from repo, if does not exists
   * 
   * @param repoGuid repository GUID
   * @param up repository update policy 
   * @param coord bare GA coordinates of the requisted metadata
   * @return  metadata object or null, if data does not exist or has been expired
   */
  public RepositoryGAMetadata findGA( String repoGuid, RepositoryUpdatePolicy up, ArtifactCoordinates coord )
  throws MetadataCorruptionException;
  
  /**
   * check if GAV level metadata exists in this cache for the given repo. Read from repo, if does not exists
   * 
   * @param repoGuid repository GUID
   * @param up repository update policy 
   * @param coord bare GAV coordinates of the requisted metadata
   * @return 
   */
  public RepositoryGAVMetadata findGAV( String repoGuid, RepositoryUpdatePolicy up, ArtifactCoordinates coord )
  throws MetadataCorruptionException;

  /**
   * update stored GA metadata with a fresh copy
   * 
   * @param repoGuid
   * @param gam
   * @throws MetadataCacheException
   */
  public void updateGA( String repoGuid, RepositoryGAMetadata gam )
  throws MetadataCacheException;

  /**
   * update stored GA metadata with a fresh copy
   * 
   * @param repoGuid
   * @param gavm fresh metadata
   * @throws MetadataCacheException
   */
  public void updateGAV( String repoGuid, RepositoryGAVMetadata gavm )
  throws MetadataCacheException;

  /**
   * find cached raw data. Raw data is different from metadata in a sense
   * that it does not expire, so it's either found or not.
   * This call is crafted for caching POMs and thus ignores classifier
   * 
   * @param bmd
   * @return found bytes or null, if no data was cached for this coordinates before
   * @throws MetadataCacheException
   */
  public byte [] findRaw( ArtifactBasicMetadata bmd )
  throws MetadataCacheException;

  /**
   * cache raw data. Raw data is different from metadata in a sense
   * that it does not expire, so it's either found or not
   * This call is crafted for caching POMs and thus ignores classifier
   * 
   * @param bmd
   * @param rawBytes - bytes to cache
   * @throws MetadataCacheException
   */
  public void saveRaw( ArtifactBasicMetadata bmd, byte [] rawBytes )
  throws MetadataCacheException;
}
