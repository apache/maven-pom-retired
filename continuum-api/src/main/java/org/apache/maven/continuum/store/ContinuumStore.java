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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;

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

    Project getProjectByName( String name )
        throws ContinuumStoreException;

    void removeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    ProjectGroup addProjectGroup( ProjectGroup group );

    ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException;

    Collection getAllProjectGroupsWithProjects();

    List getAllProjectsByName();

    List getAllSchedulesByName();

    Schedule addSchedule( Schedule schedule );

    Schedule getScheduleByName( String name )
        throws ContinuumStoreException;

    Schedule storeSchedule( Schedule schedule )
        throws ContinuumStoreException;

    List getAllProfilesByName();

    Profile addProfile( Profile profile );

    Installation addInstallation( Installation installation );

    List getAllInstallations();

    List getAllBuildsForAProjectByDate( int projectId );

    Project getProject( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateProject( Project project )
        throws ContinuumStoreException;

    void updateProfile( Profile profile )
        throws ContinuumStoreException;

    void updateSchedule( Schedule schedule )
        throws ContinuumStoreException;

    Project getProjectWithBuilds( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void removeProfile( Profile profile );

    void removeSchedule( Schedule schedule );

    Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    void removeProject( Project project );

    void removeProjectGroup( ProjectGroup projectGroup );

    ProjectGroup getProjectGroupWithBuildDetails( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    List getAllProjectGroupsWithBuildDetails();

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildResult getLatestBuildResultForProject( int projectId )
        throws ContinuumStoreException;

    void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;

    Project getProjectWithBuildDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    ProjectGroup getDefaultProjectGroup()
        throws ContinuumStoreException;

    SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf );

    void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException;

    SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException;
}
