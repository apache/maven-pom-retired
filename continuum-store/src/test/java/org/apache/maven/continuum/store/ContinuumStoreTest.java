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
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;

import javax.jdo.JDODetachedFieldAccessException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo I think this should have all the JDO stuff from the abstract test, and the abstract test should use a mock continuum store with the exception of the integration tests which should be running against a fully deployed plexus application instead
 * @todo review for ambiguities and ensure it is all encapsulated in the store, otherwise the code may make the same mistake about not deleting things, etc
 */
public class ContinuumStoreTest
    extends AbstractContinuumStoreTestCase
{

    private static final int INVALID_ID = 15000;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    //  TEST METHODS
    // ----------------------------------------------------------------------

    public void testAddProjectGroup()
        throws ContinuumStoreException
    {
        String name = "testAddProjectGroup";
        String description = "testAddProjectGroup description";
        String groupId = "org.apache.maven.continuum.test";
        ProjectGroup group = createTestProjectGroup( name, description, groupId );

        ProjectGroup copy = createTestProjectGroup( group );
        store.addProjectGroup( group );
        copy.setId( group.getId() );

        ProjectGroup retrievedGroup = store.getProjectGroup( group.getId() );
        assertProjectGroupEquals( copy, retrievedGroup );
    }

    public void testGetProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup retrievedGroup = store.getProjectGroupWithProjects( defaultProjectGroup.getId() );
        assertProjectGroupEquals( defaultProjectGroup, retrievedGroup );

        List projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );
        assertTrue( "Check existence of project 1", projects.contains( testProject1 ) );
        assertTrue( "Check existence of project 2", projects.contains( testProject2 ) );

        checkProjectGroupDefaultFetchGroup( retrievedGroup );

        Project project = (Project) projects.get( 0 );
        checkProjectDefaultFetchGroup( project );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject1, project );

        project = (Project) projects.get( 1 );
        checkProjectDefaultFetchGroup( project );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject2, project );
    }

    public void testGetInvalidProjectGroup()
        throws ContinuumStoreException
    {
        try
        {
            store.getProjectGroup( INVALID_ID );
            fail( "Should not find group with invalid ID" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }
    }

    public void testEditProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup newGroup = store.getProjectGroup( testProjectGroup2.getId() );

        newGroup.setName( "testEditProjectGroup2" );
        newGroup.setDescription( "testEditProjectGroup updated description" );
        newGroup.setGroupId( "org.apache.maven.continuum.test.new" );

        ProjectGroup copy = createTestProjectGroup( newGroup );
        copy.setId( newGroup.getId() );
        store.updateProjectGroup( newGroup );

        ProjectGroup retrievedGroup = store.getProjectGroup( testProjectGroup2.getId() );
        assertProjectGroupEquals( copy, retrievedGroup );

    }

    public void testUpdateUndetachedGroup()
    {
        ProjectGroup newGroup = new ProjectGroup();
        newGroup.setId( testProjectGroup2.getId() );
        newGroup.setName( "testUpdateUndetachedGroup2" );
        newGroup.setDescription( "testUpdateUndetachedGroup updated description" );
        newGroup.setGroupId( "org.apache.maven.continuum.test.new" );

        try
        {
            store.updateProjectGroup( newGroup );
            fail( "Should not have succeeded" );
        }
        catch ( ContinuumStoreException expected )
        {
            // good!
            assertTrue( true );
        }
    }

    public void testGetAllProjectGroups()
    {
        Collection groups = store.getAllProjectGroupsWithProjects();

        assertEquals( "check size", 2, groups.size() );
        assertTrue( groups.contains( defaultProjectGroup ) );
        assertTrue( groups.contains( testProjectGroup2 ) );

        for ( Iterator i = groups.iterator(); i.hasNext(); )
        {
            ProjectGroup group = (ProjectGroup) i.next();
            List projects = group.getProjects();
            if ( group.getId() == testProjectGroup2.getId() )
            {
                assertProjectGroupEquals( testProjectGroup2, group );
                assertTrue( "check no projects", projects.isEmpty() );
            }
            else if ( group.getId() == defaultProjectGroup.getId() )
            {
                assertProjectGroupEquals( defaultProjectGroup, group );
                assertEquals( "Check number of projects", 2, projects.size() );
                assertTrue( "Check existence of project 1", projects.contains( testProject1 ) );
                assertTrue( "Check existence of project 2", projects.contains( testProject2 ) );

                checkProjectGroupDefaultFetchGroup( group );

                Project p = (Project) projects.get( 0 );
                checkProjectDefaultFetchGroup( p );
                assertSame( "Check project group reference matches", p.getProjectGroup(), group );
            }
        }
    }

    public void testGetProject()
        throws ContinuumStoreException
    {
        Project retrievedProject = store.getProject( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        checkProjectDefaultFetchGroup( retrievedProject );
    }

    public void testGetProjectWithDetails()
        throws ContinuumStoreException
    {
        Project retrievedProject = store.getProjectWithAllDetails( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        checkProjectFetchGroup( retrievedProject, false, false, true, true );

        assertBuildDefinitionsEqual( retrievedProject.getBuildDefinitions(), testProject1.getBuildDefinitions() );
        assertNotifiersEqual( testProject1.getNotifiers(), retrievedProject.getNotifiers() );
        assertDevelopersEqual( testProject1.getDevelopers(), retrievedProject.getDevelopers() );
        assertDependenciesEqual( testProject1.getDependencies(), retrievedProject.getDependencies() );
    }

    public void testGetProjectWithCheckoutResult()
        throws ContinuumStoreException
    {
        Project retrievedProject = store.getProjectWithCheckoutResult( testProject1.getId() );
        assertProjectEquals( testProject1, retrievedProject );
        assertScmResultEquals( testCheckoutResult1, retrievedProject.getCheckoutResult()  );
        checkProjectFetchGroup( retrievedProject, true, false, false, false );
    }

    public void testGetInvalidProject()
        throws ContinuumStoreException
    {
        try
        {
            store.getProject( INVALID_ID );
            fail( "Should not find project with invalid ID" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }
    }

    public void testEditProject()
        throws ContinuumStoreException
    {
        Project newProject = store.getProject( testProject2.getId() );

        newProject.setName( "testEditProject2" );
        newProject.setDescription( "testEditProject updated description" );
        newProject.setGroupId( "org.apache.maven.continuum.test.new" );

        Project copy = createTestProject( newProject );
        copy.setId( newProject.getId() );
        store.updateProject( newProject );

        Project retrievedProject = store.getProject( testProject2.getId() );
        assertProjectEquals( copy, retrievedProject );

    }

    public void testUpdateUndetachedProject()
    {
        Project newProject = new Project();
        newProject.setId( testProject2.getId() );
        newProject.setName( "testUpdateUndetached2" );
        newProject.setDescription( "testUpdateUndetached updated description" );
        newProject.setGroupId( "org.apache.maven.continuum.test.new" );

        try
        {
            store.updateProject( newProject );
            fail( "Should not have succeeded" );
        }
        catch ( ContinuumStoreException expected )
        {
            // good!
            assertTrue( true );
        }
    }

    public void testGetAllProjects()
    {
        List projects = store.getAllProjectsByName();
        assertEquals( "check items", Arrays.asList( new Project[]{testProject1, testProject2} ), projects );

        Project project = (Project) projects.get( 1 );
        assertProjectEquals( testProject2, project );
        checkProjectDefaultFetchGroup( project );
        assertNotNull( "Check project group reference matches", project.getProjectGroup() );
    }

    public void testAddSchedule()
    {
        Schedule newSchedule = createTestSchedule( "testAddSchedule", "testAddSchedule desc", 10, "cron test", false );
        Schedule copy = createTestSchedule( newSchedule );
        store.addSchedule( newSchedule );
        copy.setId( newSchedule.getId() );

        List schedules = store.getAllSchedulesByName();
        Schedule retrievedSchedule = (Schedule) schedules.get( schedules.size() - 1 );
        assertScheduleEquals( copy, retrievedSchedule );
    }

    public void testEditSchedule()
        throws ContinuumStoreException
    {
        Schedule newSchedule = (Schedule) store.getAllSchedulesByName().get( 0 );
        newSchedule.setName( "name1.1" );
        newSchedule.setDescription( "testEditSchedule updated description" );

        Schedule copy = createTestSchedule( newSchedule );
        copy.setId( newSchedule.getId() );
        store.updateSchedule( newSchedule );

        Schedule retrievedSchedule = (Schedule) store.getAllSchedulesByName().get( 0 );
        assertScheduleEquals( copy, retrievedSchedule );
    }

    public void testRemoveSchedule()
    {
        Schedule schedule = (Schedule) store.getAllSchedulesByName().get( 2 );

        // TODO: test if it has any attachments

        store.removeSchedule( schedule );

        List schedules = store.getAllSchedulesByName();
        assertEquals( "check size", 2, schedules.size() );
        assertFalse( "check not there", schedules.contains( schedule ) );
    }

    public void testGetAllSchedules()
    {
        List schedules = store.getAllSchedulesByName();

        assertEquals( "check item count", 3, schedules.size() );

        // check equality and order
        Schedule schedule = (Schedule) schedules.get( 0 );
        assertScheduleEquals( testSchedule1, schedule );
        schedule = (Schedule) schedules.get( 1 );
        assertScheduleEquals( testSchedule2, schedule );
        schedule = (Schedule) schedules.get( 2 );
        assertScheduleEquals( testSchedule3, schedule );
    }

    public void testAddProfile()
    {
        List installations = store.getAllInstallations();
        Profile newProfile = createTestProfile( "testAddProfile", "testAddProfile desc", 5, false, false,
                                                (Installation) installations.get( 1 ),
                                                (Installation) installations.get( 2 ) );
        Profile copy = createTestProfile( newProfile );
        store.addProfile( newProfile );
        copy.setId( newProfile.getId() );

        List profiles = store.getAllProfilesByName();
        Profile retrievedProfile = (Profile) profiles.get( profiles.size() - 1 );
        assertProfileEquals( copy, retrievedProfile );
        assertInstallationEquals( testInstallationMaven20a3, retrievedProfile.getBuilder() );
        assertInstallationEquals( testInstallationJava14, retrievedProfile.getJdk() );
    }

    public void testEditProfile()
        throws ContinuumStoreException
    {
        Profile newProfile = (Profile) store.getAllProfilesByName().get( 0 );
        newProfile.setName( "name1.1" );
        newProfile.setDescription( "testEditProfile updated description" );

        Profile copy = createTestProfile( newProfile );
        store.updateProfile( newProfile );

        Profile retrievedProfile = (Profile) store.getAllProfilesByName().get( 0 );
        assertProfileEquals( copy, retrievedProfile );
        assertInstallationEquals( copy.getBuilder(), retrievedProfile.getBuilder() );
        assertInstallationEquals( copy.getJdk(), retrievedProfile.getJdk() );

    }

    public void testRemoveProfile()
    {
        Profile profile = (Profile) store.getAllProfilesByName().get( 2 );

        // TODO: test if it has any attachments

        store.removeProfile( profile );

        List profiles = store.getAllProfilesByName();
        assertEquals( "check size", 2, profiles.size() );
        assertFalse( "check not there", profiles.contains( profile ) );
    }

    public void testGetAllProfiles()
    {
        List profiles = store.getAllProfilesByName();

        assertEquals( "check item count", 3, profiles.size() );

        // check equality and order
        Profile profile = (Profile) profiles.get( 0 );
        assertProfileEquals( testProfile1, profile );
        assertInstallationEquals( testProfile1.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile1.getJdk(), profile.getJdk() );
        profile = (Profile) profiles.get( 1 );
        assertProfileEquals( testProfile2, profile );
        assertInstallationEquals( testProfile2.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile2.getJdk(), profile.getJdk() );
        profile = (Profile) profiles.get( 2 );
        assertProfileEquals( testProfile3, profile );
        assertInstallationEquals( testProfile3.getBuilder(), profile.getBuilder() );
        assertInstallationEquals( testProfile3.getJdk(), profile.getJdk() );
    }

    public void testGetAllInstallations()
    {
        List installations = store.getAllInstallations();

        assertEquals( "check item count", 3, installations.size() );

        // check equality and order
        Installation installation = (Installation) installations.get( 0 );
        assertInstallationEquals( testInstallationJava13, installation );
        installation = (Installation) installations.get( 1 );
        assertInstallationEquals( testInstallationJava14, installation );
        installation = (Installation) installations.get( 2 );
        assertInstallationEquals( testInstallationMaven20a3, installation );
    }

    public void testDeleteProject()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithBuilds( testProject1.getId() );

        store.removeProject( project );

        ProjectGroup projectGroup = store.getProjectGroupWithProjects( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getProjects().size() );
        assertProjectEquals( testProject2, (Project) projectGroup.getProjects().get( 0 ) );

        confirmProjectDeletion( testProject1 );
    }

    public void testDeleteProjectGroup()
        throws ContinuumStoreException
    {
        store.removeProjectGroup( store.getProjectGroup( defaultProjectGroup.getId() ) );

        try
        {
            store.getProjectGroup( defaultProjectGroup.getId() );
            fail( "Project group was not deleted" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }

        confirmProjectDeletion( testProject1 );
        confirmProjectDeletion( testProject2 );
        // TODO: test the project group's notifiers are physically deleted
        // TODO: test the project group's build definitions are physically deleted
    }

    public void testDeleteBuildResult()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithBuilds( testProject1.getId() );

        for ( Iterator i = project.getBuildResults().iterator(); i.hasNext(); )
        {
            BuildResult result = (BuildResult) i.next();
            if ( result.getId() == testBuildResult1.getId() )
            {
                i.remove();
            }
        }
        store.updateProject( project );

        project = store.getProjectWithBuilds( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildResults().size() );
        assertBuildResultEquals( testBuildResult2, (BuildResult) project.getBuildResults().get( 0 ) );

        List results = store.getAllBuildsForAProjectByDate( testProject1.getId() );
        assertEquals( "check item count", 1, results.size() );
        assertBuildResultEquals( testBuildResult2, (BuildResult) results.get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the build result was physically deleted
        // TODO: test the build result's SCM result was physically deleted
        // TODO: test the build result's SCM result's change sets and change files were physically deleted
    }

    public void testGetInvalidBuildResult()
        throws ContinuumStoreException
    {
        try
        {
            store.getBuildResult( INVALID_ID );
            fail( "Should not find build result with invalid ID" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }
    }

    public void testGetAllBuildsForAProject()
    {
        List results = store.getAllBuildsForAProjectByDate( testProject1.getId() );

        assertEquals( "check item count", 2, results.size() );

        // check equality and order
        BuildResult buildResult = (BuildResult) results.get( 0 );
        assertBuildResultEquals( testBuildResult2, buildResult );
        assertProjectEquals( testProject1, buildResult.getProject() );
        checkBuildResultDefaultFetchGroup( buildResult );
        buildResult = (BuildResult) results.get( 1 );
        assertBuildResultEquals( testBuildResult1, buildResult );
        assertProjectEquals( testProject1, buildResult.getProject() );
        checkBuildResultDefaultFetchGroup( buildResult );
    }

    public void testGetBuildResult()
        throws ContinuumStoreException
    {
        BuildResult buildResult = store.getBuildResult( testBuildResult3.getId() );
        assertBuildResultEquals( testBuildResult3, buildResult );
        assertScmResultEquals( testBuildResult3.getScmResult(), buildResult.getScmResult() );
        assertProjectEquals( testProject2, buildResult.getProject() );
        // TODO: reports, artifacts, data
    }

    public void testGetProjectGroupWithDetails()
        throws ContinuumStoreException
    {
        ProjectGroup retrievedGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertProjectGroupEquals( defaultProjectGroup, retrievedGroup );
        assertNotifiersEqual( defaultProjectGroup.getNotifiers(), retrievedGroup.getNotifiers() );
        assertBuildDefinitionsEqual( retrievedGroup.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );

        List projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = (Project) projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject1, project );
        assertNotifiersEqual( testProject1.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = (Project) projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        //assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertEquals( project.getProjectGroup().getId(), retrievedGroup.getId() );
        assertProjectEquals( testProject2, project );
        assertNotifiersEqual( testProject2.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );
    }

    public void testGetAllProjectsGroupWithDetails()
    {
        List projectGroups = store.getAllProjectGroupsWithBuildDetails();
        ProjectGroup group1 = (ProjectGroup) projectGroups.get( 0 );
        assertProjectGroupEquals( defaultProjectGroup, group1 );
        assertNotifiersEqual( defaultProjectGroup.getNotifiers(), group1.getNotifiers() );
        assertBuildDefinitionsEqual( group1.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );
        ProjectGroup group2 = (ProjectGroup) projectGroups.get( 1 );
        assertProjectGroupEquals( testProjectGroup2, group2 );
        assertNotifiersEqual( testProjectGroup2.getNotifiers(), group2.getNotifiers() );
        assertBuildDefinitionsEqual( group2.getBuildDefinitions(), testProjectGroup2.getBuildDefinitions() );

        List projects = group1.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = (Project) projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( testProject1, project );
        assertNotifiersEqual( testProject1.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = (Project) projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( testProject2, project );
        assertNotifiersEqual( testProject2.getNotifiers(), project.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );

        projects = group2.getProjects();
        assertEquals( "Check number of projects", 0, projects.size() );
    }

    public void testAddDeveloperToProject()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper developer = createTestDeveloper( 11, "email TADTP", "name TADTP", "scmId TADTP" );
        ProjectDeveloper copy = createTestDeveloper( developer );
        project.addDeveloper( developer );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 2, project.getDevelopers().size() );
        assertDeveloperEquals( copy, (ProjectDeveloper) project.getDevelopers().get( 1 ) );
    }

    public void testEditDeveloper()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper newDeveloper = (ProjectDeveloper) project.getDevelopers().get( 0 );
        newDeveloper.setName( "name1.1" );
        newDeveloper.setEmail( "email1.1" );

        ProjectDeveloper copy = createTestDeveloper( newDeveloper );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 1, project.getDevelopers().size() );
        assertDeveloperEquals( copy, (ProjectDeveloper) project.getDevelopers().get( 0 ) );
    }

    public void testDeleteDeveloper()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );
        project.getDevelopers().remove( 0 );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 0", 0, project.getDevelopers().size() );

        // !! These actually aren't happening !!
        // TODO: test the developer was physically deleted
    }

    public void testAddDependencyToProject()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency dependency = createTestDependency( "TADTP groupId", "TADTP artifactId", "TADTP version" );
        ProjectDependency copy = createTestDependency( dependency );
        project.addDependency( dependency );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 3, project.getDependencies().size() );
        assertDependencyEquals( copy, (ProjectDependency) project.getDependencies().get( 2 ) );
    }

    public void testEditDependency()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency newDependency = (ProjectDependency) project.getDependencies().get( 0 );
        newDependency.setGroupId( "groupId1.1" );
        newDependency.setArtifactId( "artifactId1.1" );

        ProjectDependency copy = createTestDependency( newDependency );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 2, project.getDependencies().size() );
        assertDependencyEquals( copy, (ProjectDependency) project.getDependencies().get( 0 ) );
    }

    public void testDeleteDependency()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );
        ProjectDependency dependency = (ProjectDependency) project.getDependencies().get( 1 );
        project.getDependencies().remove( 0 );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getDependencies().size() );
        assertDependencyEquals( dependency, (ProjectDependency) project.getDependencies().get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the dependency was physically deleted
    }

    public void testAddNotifierToProject()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier notifier = createTestNotifier( 13, true, false, true, "TADNTP type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        project.addNotifier( notifier );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 2, project.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) project.getNotifiers().get( 1 ) );
    }

    public void testEditNotifier()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier newNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );
        // If we use "type1.1", jpox-rc2 store "type11", weird
        String type = "type11";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 1, project.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) project.getNotifiers().get( 0 ) );
    }

    public void testDeleteNotifier()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );
        project.getNotifiers().remove( 0 );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 0", 0, project.getNotifiers().size() );

        // !! These actually aren't happening !!
        // TODO: test the notifier was physically deleted
    }

    public void testAddBuildDefinitionToProject()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        Profile profile = store.getProfile( testProfile1.getId() );
        Schedule schedule = store.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition =
            createTestBuildDefinition( "TABDTP arguments", "TABDTP buildFile", "TABDTP goals", profile, schedule );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        project.addBuildDefinition( buildDefinition );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 3, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 2 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    public void testEditBuildDefinition()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        BuildDefinition newBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        // If we use "arguments1.1", jpox-rc2 store "arguments11", weird
        String arguments = "arguments11";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 2, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile2, retrievedBuildDefinition.getProfile() );
    }

    public void testDeleteBuildDefinition()
        throws ContinuumStoreException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );
        BuildDefinition buildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 1 );
        project.getBuildDefinitions().remove( 0 );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( buildDefinition, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule2, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile2, retrievedBuildDefinition.getProfile() );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    public void testAddNotifierToProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        ProjectNotifier notifier = createTestNotifier( 14, true, false, true, "TADNTPG type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        projectGroup.addNotifier( notifier );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 3, projectGroup.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) projectGroup.getNotifiers().get( 2 ) );
    }

    public void testEditGroupNotifier()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        ProjectNotifier newNotifier = (ProjectNotifier) projectGroup.getNotifiers().get( 0 );
        // If we use "type1.1", jpox-rc2 store "type1", weird
        String type = "type1";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 2, projectGroup.getNotifiers().size() );
        assertNotifierEquals( copy, (ProjectNotifier) projectGroup.getNotifiers().get( 0 ) );
    }

    public void testDeleteGroupNotifier()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        ProjectNotifier notifier = (ProjectNotifier) projectGroup.getNotifiers().get( 1 );
        projectGroup.getNotifiers().remove( 0 );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getNotifiers().size() );
        assertNotifierEquals( notifier, (ProjectNotifier) projectGroup.getNotifiers().get( 0 ) );

        // !! These actually aren't happening !!
        // TODO: test the notifier was physically deleted
    }

    public void testAddBuildDefinitionToProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        Profile profile = store.getProfile( testProfile1.getId() );
        Schedule schedule = store.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition =
            createTestBuildDefinition( "TABDTPG arguments", "TABDTPG buildFile", "TABDTPG goals", profile, schedule );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        projectGroup.addBuildDefinition( buildDefinition );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 2, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 1 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule1, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    public void testEditGroupBuildDefinition()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        BuildDefinition newBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );
        // If we use "arguments1.1", jpox-rc2 store "arguments11", weird
        String arguments = "arguments1";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 1, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( copy, retrievedBuildDefinition );
        assertScheduleEquals( testSchedule2, retrievedBuildDefinition.getSchedule() );
        assertProfileEquals( testProfile1, retrievedBuildDefinition.getProfile() );
    }

    public void testDeleteGroupBuildDefinition()
        throws ContinuumStoreException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        projectGroup.getBuildDefinitions().remove( 0 );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check size is now 0", 0, projectGroup.getBuildDefinitions().size() );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    // ----------------------------------------------------------------------
    //  HELPER METHODS
    // ----------------------------------------------------------------------

    private void confirmProjectDeletion( Project project )
        throws ContinuumStoreException
    {
        try
        {
            store.getProject( project.getId() );
            fail( "Project should no longer exist" );
        }
        catch ( ContinuumObjectNotFoundException expected )
        {
            assertTrue( true );
        }

        // !! These actually aren't happening !!
        // TODO: test the project's checkout SCM result was physically deleted
        // TODO: test the project's checkout SCM result's change sets and change files were physically deleted
        // TODO: test the project's dependencies are physically deleted
        // TODO: test the project's developers are physically deleted
        // TODO: test the project's builds are physically deleted
        // TODO: test the project's notifiers are physically deleted
        // TODO: test the project's build definitions are physically deleted
    }

    private static void checkProjectGroupDefaultFetchGroup( ProjectGroup retrievedGroup )
    {
        try
        {
            retrievedGroup.getBuildDefinitions();
            fail( "buildDefinitions should not be in the default fetch group" );
        }
        catch ( JDODetachedFieldAccessException expected )
        {
            assertTrue( true );
        }

        try
        {
            retrievedGroup.getNotifiers();
            fail( "notifiers should not be in the default fetch group" );
        }
        catch ( JDODetachedFieldAccessException expected )
        {
            assertTrue( true );
        }
    }

    private static void checkProjectDefaultFetchGroup( Project project )
    {
        checkProjectFetchGroup( project, false, false, false, false );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        createBuildDatabase();
    }

    private static void checkProjectFetchGroup( Project project, boolean checkoutFetchGroup,
                                                boolean buildResultsFetchGroup, boolean detailsFetchGroup,
                                                boolean fineDetailsFetchGroup )
    {
        if ( !fineDetailsFetchGroup )
        {
            try
            {
                project.getDevelopers();

                fail( "developers should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }

            try
            {
                project.getDependencies();

                fail( "dependencies should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }

        if ( !detailsFetchGroup )
        {
            try
            {
                project.getNotifiers();

                fail( "notifiers should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }

            try
            {
                project.getBuildDefinitions();

                fail( "buildDefinitions should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }

        if ( !checkoutFetchGroup )
        {
            try
            {
                project.getCheckoutResult();

                fail( "checkoutResult should not be in the fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }

        if ( !buildResultsFetchGroup )
        {
            try
            {
                project.getBuildResults();

                fail( "buildResults should not be in the default fetch group" );
            }
            catch ( JDODetachedFieldAccessException expected )
            {
                assertTrue( true );
            }
        }
    }

    private static void checkBuildResultDefaultFetchGroup( BuildResult buildResult )
    {
        try
        {
            buildResult.getScmResult();

            fail( "scmResult should not be in the default fetch group" );
        }
        catch ( JDODetachedFieldAccessException expected )
        {
            assertTrue( true );
        }
        // TODO: artifacts
        // TODO: report
        // TODO: long error data
    }

}
