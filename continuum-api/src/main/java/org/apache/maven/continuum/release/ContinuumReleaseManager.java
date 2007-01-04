package org.apache.maven.continuum.release;

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

import org.apache.maven.continuum.model.project.Project;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * The Continuum Release Manager is responsible for performing releases based on a release descriptor
 * that has been received by the Maven Release Plugin.
 *
 * @author Jason van Zyl
 */
public interface ContinuumReleaseManager
{
    String ROLE = ContinuumReleaseManager.class.getName();

    /**
     * Prepare a project for release
     *
     * @param project      project / project group to be released
     * @param releaseProperties
     * @throws ContinuumReleaseException
     */
    String prepare( Project project, Properties releaseProperties, Map releaseVersions,
                    Map developmentVersions, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException;

    /**
     * Perform a release based on a given releaseId
     *
     * @param releaseId
     * @param buildDirectory
     * @param goals
     * @param useReleaseProfile
     * @throws ContinuumReleaseException
     */
    void perform( String releaseId, File buildDirectory,
                  String goals, boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException;

    /**
     * Perform a release based on a release descriptor received by the Maven Release Plugin.
     *
     * @param descriptorFile
     * @throws ContinuumReleaseException
     */
    void perform( String releaseId, File descriptorFile, File buildDirectory,
                  String goals, boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException;

    Map getPreparedReleases();

    Map getReleaseResults();

    Map getListeners();
}
