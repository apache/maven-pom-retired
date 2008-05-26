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

import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;

/**
 * Provides some metadata operations, like querying the remote repository for a list of versions available for an
 * artifact.
 *
 * @author Jason van Zyl
 * @version $Id$
 */
public interface MetadataSource
{
    String ROLE = MetadataSource.class.getName();

    MetadataResolution retrieve( ArtifactMetadata artifact,
                                 LocalRepository localRepository,
                                 Set<RemoteRepository> remoteRepositories )
        throws MetadataRetrievalException;
}