package org.apache.maven.continuum;

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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface Continuum
{
    String ROLE = Continuum.class.getName();

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    void removeProject( String projectId )
        throws ContinuumException;

    void checkoutProject( String id )
        throws ContinuumException;

    ContinuumProject getProject( String projectId )
        throws ContinuumException;

    Collection getAllProjects( int start, int end )
        throws ContinuumException;

    ScmResult getScmResultForProject( String projectId )
        throws ContinuumException;

    Collection getProjects()
        throws ContinuumException;

    BuildResult getLatestBuildResultForProject( String projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    boolean isInBuildingQueue( String id )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException;

    void buildProjects()
        throws ContinuumException;

    void buildProjects( boolean force )
        throws ContinuumException;

    void buildProject( String projectId )
        throws ContinuumException;

    void buildProject( String projectId, boolean force )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    BuildResult getBuildResult( int buildId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Maven 2.x projects.
    // ----------------------------------------------------------------------

    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    String addProject( ContinuumProject project, String executorId )
        throws ContinuumException;

    void updateProject( ContinuumProject project )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    ProjectNotifier getNotifier( String projectId, String notifierType )
        throws ContinuumException;

    void updateNotifier( String projectId, String notifierType, Map configuration )
        throws ContinuumException;

    void addNotifier( String projectId, String notifierType, Map configuration )
        throws ContinuumException;

    void removeNotifier( String projectId, String notifierType )
        throws ContinuumException;

    Collection getBuildResultsForProject( String projectId )
        throws ContinuumException;
}
