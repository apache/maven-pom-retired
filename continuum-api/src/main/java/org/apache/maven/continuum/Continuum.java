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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
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

    void removeProject( int projectId )
        throws ContinuumException;

    void checkoutProject( int projectId )
        throws ContinuumException;

    Project getProject( int projectId )
        throws ContinuumException;

    Collection getAllProjects( int start, int end )
        throws ContinuumException;

    Collection getProjects()
        throws ContinuumException;

    BuildResult getLatestBuildResultForProject( int projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    boolean isInBuildingQueue( int projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException;

    void buildProjects()
        throws ContinuumException;

    void buildProjects( int trigger )
        throws ContinuumException;

    void buildProjects( Schedule schedule )
        throws ContinuumException;

    void buildProject( int projectId )
        throws ContinuumException;

    void buildProject( int projectId, int trigger )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build information
    // ----------------------------------------------------------------------

    BuildResult getBuildResult( int buildId )
        throws ContinuumException;

    String getBuildOutput( int projectId, int buildId )
        throws ContinuumException;

    Collection getBuildResultsForProject( int projectId )
        throws ContinuumException;

    List getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    int addProject( Project project, String executorId )
        throws ContinuumException;

    ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException;

    ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException;

    void updateProject( Project project )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException;

    void updateNotifier( int projectId, int notifierId, Map configuration )
        throws ContinuumException;

    void addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException;

    void removeNotifier( int projectId, int notifierId )
        throws ContinuumException;

    Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException;

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumException;

    Project getProjectWithBuilds( int projectId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------

    BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException;

    void updateBuildDefinition( int projectId, int buildDefinitionId, Map configuration )
        throws ContinuumException;

    void addBuildDefinition( int projectId, Map configuration )
        throws ContinuumException;

    void removeBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Schedule
    // ----------------------------------------------------------------------

    Schedule getSchedule( int id )
        throws ContinuumException;

    Collection getSchedules()
        throws ContinuumException;

    void addSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( Schedule schedule )
        throws ContinuumException;

    void updateSchedule( int scheduleId, Map configuration )
        throws ContinuumException;

    void removeSchedule( int scheduleId )
        throws ContinuumException;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    ConfigurationService getConfiguration();

    void reloadConfiguration()
        throws ContinuumException;
}
