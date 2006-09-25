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
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.model.system.UserGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    Map getDefaultBuildDefinitions();

    /**
     * returns the default build definition of the project, if the project doesn't have on declared the default
     * of the project group will be returned
     *
     * this should be the most common usage of the default build definition accessing methods
     *
     * @param projectId
     * @return
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * returns the default build definition for the project without consulting the project group
     *
     * @param projectId
     * @return
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    BuildDefinition getDefaultBuildDefinitionForProject( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    /**
     * returns the default build definition for the project group and there should always be one declared
     * 
     * @param projectGroupId
     * @return
     * @throws ContinuumStoreException
     * @throws ContinuumObjectNotFoundException
     */
    BuildDefinition getDefaultBuildDefinitionForProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException;

    ProjectGroup addProjectGroup( ProjectGroup group );

    ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    public ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumObjectNotFoundException;

    void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException;

    Collection getAllProjectGroupsWithProjects();

    List getAllProjectsByName();

    List getAllProjectsByNameWithDependencies();

    public List getProjectsWithDependenciesByGroupId( int projectGroupId );

    List getAllProjectsByNameWithBuildDetails();

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

    Map getProjectIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

    Map getProjectGroupIdsAndBuildDefinitionsIdsBySchedule( int scheduleId )
        throws ContinuumStoreException;

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

    ProjectGroup getProjectGroupWithProjects( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    List getAllProjectGroupsWithBuildDetails();

    List getAllProjectsWithAllDetails();

    Project getProjectWithAllDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    ProjectGroup getProjectGroupByGroupIdWithProjects( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    BuildResult getLatestBuildResultForProject( int projectId );

    List getBuildResultsForProject( int projectId, long fromDate );

    Map getLatestBuildResults();

    List getBuildResultByBuildNumber( int projectId, int buildNumber );

    Map getBuildResultsInSuccess();

    void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;

    Project getProjectWithBuildDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf );

    void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException;

    SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException;

    ContinuumUser addUser( ContinuumUser user );

    void updateUser( ContinuumUser user )
        throws ContinuumStoreException;

    ContinuumUser getUser( int userId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    ContinuumUser getGuestUser()
        throws ContinuumStoreException;

    List getUsers()
        throws ContinuumStoreException;

    ContinuumUser getUserByUsername( String username )
        throws ContinuumStoreException;

    void removeUser( ContinuumUser user );

    List getPermissions()
        throws ContinuumStoreException;

    Permission getPermission( String name )
        throws ContinuumStoreException;

    Permission addPermission( Permission perm );

    UserGroup addUserGroup( UserGroup group );

    void updateUserGroup( UserGroup group )
        throws ContinuumStoreException;

    List getUserGroups()
        throws ContinuumStoreException;

    UserGroup getUserGroup( int userGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException;

    UserGroup getUserGroup( String name );

    void removeUserGroup( UserGroup group );

    void closeStore();
}
