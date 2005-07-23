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
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumBuildGroup;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.scm.ScmResult;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ContinuumStore
{
    String ROLE = ContinuumStore.class.getName();

    // ----------------------------------------------------------------------
    // ContinuumProject Mutators
    // ----------------------------------------------------------------------

    ContinuumProject addProject( ContinuumProject project )
        throws ContinuumStoreException;

    void removeProject( String projectId )
        throws ContinuumStoreException;

    ContinuumProject updateProject( ContinuumProject project )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // ContinuumProject Queries
    // ----------------------------------------------------------------------

    Collection getAllProjects()
        throws ContinuumStoreException;

    ContinuumProject getProjectByName( String name )
        throws ContinuumStoreException;

    ContinuumProject getProjectByScmUrl( String scmUrl )
        throws ContinuumStoreException;

    ContinuumProject getProject( String projectId )
        throws ContinuumStoreException;

    ContinuumProject getProjectForBuild( String buildId )
        throws ContinuumStoreException;

    ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    ContinuumBuild addBuild( String projectId, ContinuumBuild build )
        throws ContinuumStoreException;

    ContinuumBuild updateBuild( ContinuumBuild build )
        throws ContinuumStoreException;

    void setBuildOutput( String buildId, String output )
        throws ContinuumStoreException;

    ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException;

    String getBuildOutput( String buildId )
        throws ContinuumStoreException;

    ContinuumBuild getLatestBuildForProject( String projectId )
        throws ContinuumStoreException;

    Collection getBuildsForProject( String projectId, int start, int end )
        throws ContinuumStoreException;

    List getChangedFilesForBuild( String buildId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Notifiers
    // ----------------------------------------------------------------------

    void removeNotifier( ContinuumNotifier notifier )
        throws ContinuumStoreException;

    ContinuumNotifier storeNotifier( ContinuumNotifier notifier )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    ContinuumSchedule addSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException;

    ContinuumSchedule updateSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException;

    void removeSchedule( String scheduleId )
        throws ContinuumStoreException;

    Collection getSchedules()
        throws ContinuumStoreException;

    ContinuumSchedule getSchedule( String scheduleId )
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Project Groups
    // ----------------------------------------------------------------------

    ContinuumProjectGroup addProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException;

    ContinuumProjectGroup updateProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException;

    void removeProjectGroup( String projectGroupId )
        throws ContinuumStoreException;

    ContinuumProjectGroup getProjectGroup( String projectGroupId )
        throws ContinuumStoreException;

    ContinuumProjectGroup getProjectGroupByName( String name )
        throws ContinuumStoreException;

    ContinuumProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException;

    Collection getProjectGroups()
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Build Group
    // ----------------------------------------------------------------------

    ContinuumBuildGroup addBuildGroup( ContinuumBuildGroup buildGroup )
        throws ContinuumStoreException;

    ContinuumBuildGroup updateBuildGroup( ContinuumBuildGroup buildGroup )
        throws ContinuumStoreException;

    void removeBuildGroup( String buildGroupId )
        throws ContinuumStoreException;

    ContinuumBuildGroup getBuildGroup( String buildGroupId )
        throws ContinuumStoreException;

    Collection getBuildGroups()
        throws ContinuumStoreException;

    // ----------------------------------------------------------------------
    // Build settings
    // ----------------------------------------------------------------------

    ContinuumBuildSettings addBuildSettings( ContinuumBuildSettings buildSettings )
        throws ContinuumStoreException;

    ContinuumBuildSettings updateBuildSettings( ContinuumBuildSettings buildSettings )
        throws ContinuumStoreException;

    void removeBuildSettings( String buildSettingsId )
        throws ContinuumStoreException;

    ContinuumBuildSettings getBuildSettings( String buildSettingsId )
        throws ContinuumStoreException;

    Collection getBuildSettings()
        throws ContinuumStoreException;
}
