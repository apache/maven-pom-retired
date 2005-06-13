package org.apache.maven.continuum.store;

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

import java.util.Collection;
import java.util.List;

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.UpdateScmResult;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ContinuumStore
{
    String ROLE = ContinuumStore.class.getName();

    // ----------------------------------------------------------------------
    // ContinuumProject
    // ----------------------------------------------------------------------

    String addProject( ContinuumProject project )
        throws ContinuumStoreException;

    void removeProject( String projectId )
        throws ContinuumStoreException;

    void setWorkingDirectory( String projectId, String workingDirectory )
        throws ContinuumStoreException;

    void updateProject( String projectId,
                        String name,
                        String scmUrl,
                        String nagEmailAddress,
                        String version,
                        String commandLineArguments )
        throws ContinuumStoreException;

    Collection getAllProjects()
        throws ContinuumStoreException;

    ContinuumProject getProjectByName( String nameSearchPattern )
        throws ContinuumStoreException;

    ContinuumProject getProjectByScmUrl( String scmUrl )
        throws ContinuumStoreException;

    ContinuumProject getProject( String projectId )
        throws ContinuumStoreException;

    CheckOutScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    /**
     * @deprecated
     */
    String createBuild( String projectId, boolean forced )
        throws ContinuumStoreException;

    /**
     * @deprecated
     */
    void setBuildResult( String buildId,
                         int state,
                         ContinuumBuildResult result,
                         UpdateScmResult scmResult,
                         Throwable error )
        throws ContinuumStoreException;

    ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException;

    ContinuumBuild getLatestBuildForProject( String projectId )
        throws ContinuumStoreException;

    Collection getBuildsForProject( String projectId, int start, int end )
        throws ContinuumStoreException;

    ContinuumBuildResult getBuildResultForBuild( String buildId )
        throws ContinuumStoreException;

    List getChangedFilesForBuild( String buildId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Project Transitions
    // ----------------------------------------------------------------------

    void setCheckoutDone( String projectId,
                          CheckOutScmResult scmResult,
                          String errorMessage,
                          Throwable exception )
        throws ContinuumStoreException;

    void setIsUpdating( String projectId )
        throws ContinuumStoreException;

    void setUpdateDone( String projectId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Build Transitions
    // ----------------------------------------------------------------------

    String buildingProject( String projectId,
                            boolean forced,
                            UpdateScmResult scmResult )
        throws ContinuumStoreException;

    void setBuildNotExecuted( String projectId )
        throws ContinuumStoreException;

    void setBuildComplete( String buildId,
                           UpdateScmResult scmResult,
                           ContinuumBuildResult result )
        throws ContinuumStoreException;

    void setBuildError( String buildId,
                        UpdateScmResult scmResult,
                        Throwable throwable )
        throws ContinuumStoreException;
}
