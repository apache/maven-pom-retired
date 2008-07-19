package org.apache.maven.mercury.metadata;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;

/**
 * Provides some metadata operations, like querying the remote repository for a list of versions available for an
 * artifact.
 *
 * @author Jason van Zyl
 * @author Oleg Gusakov
 * 
 * @version $Id$
 */
public interface MetadataSource
{
    String ROLE = MetadataSource.class.getName();

    /**
     * You would only ever need the remote repositories if the representation you were parsing contained repositories that
     * you wanted added to the search list of remote repositories.
     * 
     * This entry is called by tree builder to get the recorded dependencies, i.e. queries, which will later be expanded 
     * into actual "existing" ArtifactMetadata objects
     * 
     * @param metadata
     * @param localRepository
     * @param remoteRepositories
     * @return
     * @throws MetadataRetrievalException
     */
    MetadataResolution retrieve( 
                         ArtifactMetadata metadata
                       , LocalRepository localRepository
                       , List<RemoteRepository> remoteRepositories
                               )
    throws MetadataRetrievalException
    ;

    /**
     * returns all existing versions of the supplied artifact, treating the provided metadata as a query. This is used 
     * by the tree builder to construct the "dirty" tree
     * 
     * @param metadataQuery metadata to look for: ranges, etc.
     * @param localRepository
     * @param remoteRepositories
     * @return
     * @throws MetadataRetrievalException
     */
    Collection<ArtifactMetadata> expand( 
                                   ArtifactMetadata metadataQuery
                                 , LocalRepository localRepository
                                 , List<RemoteRepository> remoteRepositories 
                                       )
    throws MetadataRetrievalException
    ;
}