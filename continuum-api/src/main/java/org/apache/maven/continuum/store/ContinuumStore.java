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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumSchedule;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo remove old stuff
 */
public interface ContinuumStore
{
    String ROLE = ContinuumStore.class.getName();

    ContinuumProject addProject( ContinuumProject project )
        throws ContinuumStoreException;

    void removeProject( String projectId )
        throws ContinuumStoreException;

    ContinuumProject updateProject( ContinuumProject project )
        throws ContinuumStoreException;

    Collection getAllProjects()
        throws ContinuumStoreException;

    ContinuumProject getProjectByName( String name )
        throws ContinuumStoreException;

    ContinuumProject getProject( String projectId )
        throws ContinuumStoreException;

    ContinuumProject getProjectForBuild( String buildId )
        throws ContinuumStoreException;

    ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException;

    ContinuumBuild addBuild( String projectId, ContinuumBuild build )
        throws ContinuumStoreException;

    ContinuumBuild updateBuild( ContinuumBuild build )
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

    File getBuildOutputFile( String buildId )
        throws ContinuumStoreException;

    void removeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    ContinuumSchedule addSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException;

    ContinuumSchedule updateSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException;

    Collection getSchedules()
        throws ContinuumStoreException;

    ContinuumSchedule getSchedule( String scheduleId )
        throws ContinuumStoreException;

    ContinuumProjectGroup addProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException;

    ContinuumProjectGroup updateProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException;

    void removeProjectGroup( String projectGroupId )
        throws ContinuumStoreException;

    ContinuumProjectGroup getProjectGroup( String projectGroupId )
        throws ContinuumStoreException;

    ContinuumProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException;

    Collection getProjectGroups()
        throws ContinuumStoreException;

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

    ProjectGroup addProjectGroup( ProjectGroup group );

    ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumObjectNotFoundException;

    void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException;

    Collection getAllProjectGroupsWithProjects();

    List getAllProjectsByName();

    List getAllSchedulesByName();

    Schedule addSchedule( Schedule schedule );

    List getAllProfilesByName();

    Profile addProfile( Profile profile );

    Installation addInstallation( Installation installation );

    List getAllInstallations();

    List getAllBuildsForAProjectByDate( int projectId );

    Project getProject( int projectId )
        throws ContinuumObjectNotFoundException;

    void updateProject( Project project )
        throws ContinuumStoreException;

    void updateProfile( Profile profile )
        throws ContinuumStoreException;

    void updateSchedule( Schedule schedule )
        throws ContinuumStoreException;

    Project getProjectWithBuilds( int projectId )
        throws ContinuumObjectNotFoundException;

    void removeProfile( Profile profile );

    void removeSchedule( Schedule schedule );

    Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumObjectNotFoundException;

    BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException;

    void removeProject( Project project );

    void removeProjectGroup( ProjectGroup projectGroup );

    ProjectGroup getProjectGroupWithBuildDetails( int projectGroupId )
        throws ContinuumObjectNotFoundException;

    List getAllProjectGroupsWithBuildDetails();

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumObjectNotFoundException;

    Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException;

    Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException;
}
