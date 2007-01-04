package org.apache.maven.continuum.execution.maven.m2;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public interface MavenBuilderHelper
{
    String ROLE = MavenBuilderHelper.class.getName();

    void mapMetadataToProject( ContinuumProjectBuildingResult result, File metadata, Project project );

    MavenProject getMavenProject( ContinuumProjectBuildingResult result, File file );

    /**
     * @param result
     * @param mavenProject
     * @param continuumProject
     * @param groupPom         map this project as if it is being used to initialize a project group
     */
    void mapMavenProjectToContinuumProject( ContinuumProjectBuildingResult result, MavenProject mavenProject,
                                            Project continuumProject, boolean groupPom );

    ArtifactRepository getLocalRepository()
        throws SettingsConfigurationException;
}
