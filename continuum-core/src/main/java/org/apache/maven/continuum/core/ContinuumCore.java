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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.store.ContinuumStore;

import org.codehaus.plexus.taskqueue.TaskQueue;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public interface ContinuumCore
{
    String ROLE = ContinuumCore.class.getName();

    Collection addProjectsFromUrl( String url, String projectBuilderId )
        throws ContinuumException;

    Collection addProjectsFromUrl( URL url, String projectBuilderId )
        throws ContinuumException;

    String addProjectFromScm( ContinuumProject project )
        throws ContinuumException;

    void removeProject( String projectId )
        throws ContinuumException;

    void updateProjectFromScm( String projectId )
        throws ContinuumException;

    void updateProject( String projectId,
                        String name,
                        String scmUrl,
                        String nagEmailAddress,
                        String version,
                        String commandLineArguments )
        throws ContinuumException;

//    void updateProjectConfiguration( String projectId, Properties configuration )
//        throws ContinuumException;

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
    // ContinuumBuild
    // ----------------------------------------------------------------------

    ContinuumBuild getBuild( String buildId )
        throws ContinuumException;

    Collection getBuildsForProject( String projectId )
        throws ContinuumException;

    ContinuumBuildResult getBuildResultForBuild( String buildId )
        throws ContinuumException;

    Collection getChangedFilesForBuild( String buildId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // ContinuumBuildExecutor
    // ----------------------------------------------------------------------

    ContinuumBuildExecutor getBuildExecutor( String id )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Accessors for actions
    // ----------------------------------------------------------------------

    ContinuumStore getStore();

    ContinuumScm getScm();

    ContinuumProjectBuilderManager getProjectBuilderManager();

    BuildExecutorManager getBuildExecutorManager();

    TaskQueue getBuildQueue();

    TaskQueue getCheckOutQueue();

    String getWorkingDirectory();
}
