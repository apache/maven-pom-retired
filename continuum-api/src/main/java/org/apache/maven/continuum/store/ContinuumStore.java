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
import org.apache.maven.continuum.project.ContinuumProject;

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

    ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException;

    String getBuildOutput( int buildId, String projectId )
        throws ContinuumStoreException;

    File getBuildOutputFile( int buildId, String projectId )
        throws ContinuumStoreException;

    void removeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException;

    ProjectNotifier storeNotifier( ProjectNotifier notifier )
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

    ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException;

    BuildResult getLatestBuildResultForProject( String projectId )
        throws ContinuumStoreException;

    BuildResult addBuildResult( ContinuumProject project, BuildResult build )
        throws ContinuumStoreException;

    void updateBuildResult( BuildResult build )
        throws ContinuumStoreException;
}
