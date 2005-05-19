package org.apache.maven.continuum.core;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public interface ContinuumCore
{
    String ROLE = ContinuumCore.class.getName();

    //TODO: an URL converter in OGNL would be nice.

    Collection addProjectsFromUrl( String url, String projectBuilderId )
        throws ContinuumException;

    Collection addProjectsFromUrl( URL url, String projectBuilderId )
        throws ContinuumException;

    String addProjectFromScm( String scmUrl,
                              String executorId,
                              String projectName,
                              String nagEmailAddress,
                              String version,
                              Properties configuration )
        throws ContinuumException;

    void removeProject( String projectId )
        throws ContinuumException;

    void updateProjectFromScm( String projectId )
        throws ContinuumException;

    void updateProject( String projectId,
                        String name,
                        String scmUrl,
                        String nagEmailAddress,
                        String version )
        throws ContinuumException;

    void updateProjectConfiguration( String projectId, Properties configuration )
        throws ContinuumException;

    ContinuumProject getProject( String projectId )
        throws ContinuumException;

    Collection getAllProjects( int start, int end )
        throws ContinuumException;

    CheckOutScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumException;

    void buildProject( String projectId, boolean force )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    Collection getProjects()
        throws ContinuumException;

    ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    ContinuumBuild getBuild( String buildId )
        throws ContinuumException;

    Collection getBuildsForProject( String projectId )
        throws ContinuumException;

    ContinuumBuildResult getBuildResultForBuild( String buildId )
        throws ContinuumException;

    Collection getChangedFilesForBuild( String buildId )
        throws ContinuumException;
}
