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
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.ConfigurableJdoFactory;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;
import org.jpox.SchemaTool;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo I think this should have all the JDO stuff from the abstract test, and the abstract test should use a mock continuum store with the exception of the integration tests which should be running against a fully deployed plexus application instead
 * @todo review for ambiguities and ensure it is all encapsulated in the store, otherwise the code may make the same mistake about not deleting things, etc
 */
public class ContinuumStoreTest
    extends PlexusTestCase
{
    private ContinuumStore store;

    private ProjectGroup defaultProjectGroup;

    private ProjectGroup testProjectGroup2;

    private Project testProject1;

    private Project testProject2;

    private static final int INVALID_ID = 15000;

    private Schedule testSchedule1;

    private Schedule testSchedule2;

    private Schedule testSchedule3;

    private Profile testProfile1;

    private Profile testProfile2;

    private Profile testProfile3;

    private Installation testInstallationJava13;

    private Installation testInstallationJava14;

    private Installation testInstallationMaven20a3;

    private BuildResult testBuildResult1;

    private BuildResult testBuildResult2;

    private BuildResult testBuildResult3;

    private ScmResult testCheckoutResult1;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void setUp()
        throws Exception
    {
        super.setUp();

        store = createStore();

        // Setting up test data
        defaultProjectGroup = createTestProjectGroup( "Default Group", "The Default Group",
                                                      "org.apache.maven.test.default" );

        testProjectGroup2 = createTestProjectGroup( "test group 2", "test group 2 desc", "test group 2 groupId" );

        testProject1 = createTestProject( "artifactId1", 1, "description1", defaultProjectGroup.getGroupId(), "name1",
                                          "scmUrl1", 1, "url1", "version1", "workingDirectory1" );

        // state must be 1 unless we setup a build in the correct state
        testProject2 = createTestProject( "artifactId2", 2, "description2", defaultProjectGroup.getGroupId(), "name2",
                                          "scmUrl2", 1, "url2", "version2", "workingDirectory2" );

        testSchedule1 = createTestSchedule( "name1", "description1", 1, "cronExpression1", true );
        testSchedule2 = createTestSchedule( "name2", "description2", 2, "cronExpression2", true );
        testSchedule3 = createTestSchedule( "name3", "description3", 3, "cronExpression3", true );

        testInstallationJava13 = createTestInstallation( "JDK", "/usr/local/java-1.3", "1.3" );
        testInstallationJava14 = createTestInstallation( "JDK", "/usr/local/java-1.4", "1.4" );
        testInstallationMaven20a3 = createTestInstallation( "Maven", "/usr/local/maven-2.0-alpha-3", "2.0-alpha-3" );

        ProjectNotifier testGroupNotifier1 = createTestNotifier( 1, true, false, true, "type1" );
        ProjectNotifier testGroupNotifier2 = createTestNotifier( 2, false, true, false, "type2" );
        ProjectNotifier testGroupNotifier3 = createTestNotifier( 3, true, false, false, "type3" );

        ProjectNotifier testNotifier1 = createTestNotifier( 11, true, true, false, "type11" );
        ProjectNotifier testNotifier2 = createTestNotifier( 12, false, false, true, "type12" );
        ProjectNotifier testNotifier3 = createTestNotifier( 13, false, true, false, "type13" );

        ProjectDeveloper testDeveloper1 = createTestDeveloper( 1, "email1", "name1", "scmId1" );
        ProjectDeveloper testDeveloper2 = createTestDeveloper( 2, "email2", "name2", "scmId2" );
        ProjectDeveloper testDeveloper3 = createTestDeveloper( 3, "email3", "name3", "scmId3" );

        ProjectDependency testDependency1 = createTestDependency( "groupId1", "artifactId1", "version1" );
        ProjectDependency testDependency2 = createTestDependency( "groupId2", "artifactId2", "version2" );
        ProjectDependency testDependency3 = createTestDependency( "groupId3", "artifactId3", "version3" );

        // TODO: simplify by deep copying the relationships in createTest... ?
        long baseTime = System.currentTimeMillis();
        testBuildResult1 = createTestBuildResult( 1, true, 1, 1, "error1", 1, baseTime, baseTime + 1000 );
        BuildResult buildResult1 = createTestBuildResult( testBuildResult1 );
        ScmResult scmResult = createTestScmResult( "commandOutput1", "providerMessage1", true, "1" );
        buildResult1.setScmResult( scmResult );
        ScmResult testBuildResult1ScmResult = createTestScmResult( scmResult, "1" );
        testBuildResult1.setScmResult( testBuildResult1ScmResult );
        testCheckoutResult1 = createTestScmResult( "commandOutputCO1", "providerMessageCO1", false, "CO1" );
        ScmResult checkoutResult1 = createTestScmResult( testCheckoutResult1, "CO1" );
        testProject1.setCheckoutResult( checkoutResult1 );
        testProject1.addBuildResult( buildResult1 );

        testBuildResult2 = createTestBuildResult( 2, false, 2, 2, "error2", 2, baseTime + 2000, baseTime + 3000 );
        BuildResult buildResult2 = createTestBuildResult( testBuildResult2 );
        testProject1.addBuildResult( buildResult2 );

        testBuildResult3 = createTestBuildResult( 3, true, 3, 3, "error3", 3, baseTime + 4000, baseTime + 5000 );
        BuildResult buildResult3 = createTestBuildResult( testBuildResult3 );
        scmResult = createTestScmResult( "commandOutput3", "providerMessage3", true, "3" );
        buildResult3.setScmResult( scmResult );
        testBuildResult3.setScmResult( createTestScmResult( scmResult, "3" ) );
        testProject2.addBuildResult( buildResult3 );

        // TODO: better way? this assumes that some untested methods already work!
        Schedule schedule2 = createTestSchedule( testSchedule2 );
        schedule2 = store.addSchedule( schedule2 );
        testSchedule2.setId( schedule2.getId() );

        Schedule schedule1 = createTestSchedule( testSchedule1 );
        schedule1 = store.addSchedule( schedule1 );
        testSchedule1.setId( schedule1.getId() );

        Schedule schedule3 = createTestSchedule( testSchedule3 );
        schedule3 = store.addSchedule( schedule3 );
        testSchedule3.setId( schedule3.getId() );

        Installation installationJava14 = createTestInstallation( testInstallationJava14 );
        installationJava14 = store.addInstallation( installationJava14 );

        Installation installationMaven20a3 = createTestInstallation( testInstallationMaven20a3 );
        installationMaven20a3 = store.addInstallation( installationMaven20a3 );

        Installation installationJava13 = createTestInstallation( testInstallationJava13 );
        installationJava13 = store.addInstallation( installationJava13 );

        testProfile1 = createTestProfile( "name1", "description1", 1, true, true, installationJava13,
                                          installationMaven20a3 );
        testProfile2 = createTestProfile( "name2", "description2", 2, false, true, installationJava14,
                                          installationMaven20a3 );
        testProfile3 = createTestProfile( "name3", "description3", 3, true, false, installationJava14,
                                          installationMaven20a3 );

        Profile profile1 = createTestProfile( testProfile1 );
        profile1 = store.addProfile( profile1 );
        testProfile1.setId( profile1.getId() );

        Profile profile2 = createTestProfile( testProfile2 );
        profile2 = store.addProfile( profile2 );
        testProfile2.setId( profile2.getId() );

        Profile profile3 = createTestProfile( testProfile3 );
        profile3 = store.addProfile( profile3 );
        testProfile3.setId( profile3.getId() );

        BuildDefinition testGroupBuildDefinition1 = createTestBuildDefinition( "arguments1", "buildFile1", "goals1",
                                                                               profile1, schedule2 );
        BuildDefinition testGroupBuildDefinition2 = createTestBuildDefinition( "arguments2", "buildFile2", "goals2",
                                                                               profile1, schedule1 );
        BuildDefinition testGroupBuildDefinition3 = createTestBuildDefinition( "arguments3", "buildFile3", "goals3",
                                                                               profile2, schedule1 );

        BuildDefinition testBuildDefinition1 = createTestBuildDefinition( "arguments11", "buildFile11", "goals11",
                                                                          profile2, schedule1 );
        BuildDefinition testBuildDefinition2 = createTestBuildDefinition( "arguments12", "buildFile12", "goals12",
                                                                          profile2, schedule2 );
        BuildDefinition testBuildDefinition3 = createTestBuildDefinition( "arguments13", "buildFile13", "goals13",
                                                                          profile1, schedule2 );

        ProjectGroup group = createTestProjectGroup( defaultProjectGroup );

        Project project1 = createTestProject( testProject1 );
        project1.addBuildResult( buildResult1 );
        project1.addBuildResult( buildResult2 );
        project1.setCheckoutResult( checkoutResult1 );
        ProjectNotifier notifier1 = createTestNotifier( testNotifier1 );
        project1.addNotifier( notifier1 );
        testProject1.addNotifier( testNotifier1 );

        BuildDefinition buildDefinition1 = createTestBuildDefinition( testBuildDefinition1 );
        project1.addBuildDefinition( buildDefinition1 );
        testProject1.addBuildDefinition( testBuildDefinition1 );
        BuildDefinition buildDefinition2 = createTestBuildDefinition( testBuildDefinition2 );
        project1.addBuildDefinition( buildDefinition2 );
        testProject1.addBuildDefinition( testBuildDefinition2 );

        ProjectDeveloper projectDeveloper1 = createTestDeveloper( testDeveloper1 );
        project1.addDeveloper( projectDeveloper1 );
        testProject1.addDeveloper( testDeveloper1 );

        ProjectDependency projectDependency1 = createTestDependency( testDependency1 );
        project1.addDependency( projectDependency1 );
        testProject1.addDependency( testDependency1 );

        ProjectDependency projectDependency2 = createTestDependency( testDependency2 );
        project1.addDependency( projectDependency2 );
        testProject1.addDependency( testDependency2 );

        group.addProject( project1 );
        defaultProjectGroup.addProject( project1 );
        Project project2 = createTestProject( testProject2 );
        project2.addBuildResult( buildResult3 );
        ProjectNotifier notifier2 = createTestNotifier( testNotifier2 );
        project2.addNotifier( notifier2 );
        testProject2.addNotifier( testNotifier2 );
        ProjectNotifier notifier3 = createTestNotifier( testNotifier3 );
        project2.addNotifier( notifier3 );
        testProject2.addNotifier( testNotifier3 );

        BuildDefinition buildDefinition3 = createTestBuildDefinition( testBuildDefinition3 );
        project2.addBuildDefinition( buildDefinition3 );
        testProject2.addBuildDefinition( testBuildDefinition3 );

        ProjectDeveloper projectDeveloper2 = createTestDeveloper( testDeveloper2 );
        project2.addDeveloper( projectDeveloper2 );
        testProject2.addDeveloper( testDeveloper2 );

        ProjectDeveloper projectDeveloper3 = createTestDeveloper( testDeveloper3 );
        project2.addDeveloper( projectDeveloper3 );
        testProject2.addDeveloper( testDeveloper3 );

        ProjectDependency projectDependency3 = createTestDependency( testDependency3 );
        project2.addDependency( projectDependency3 );
        testProject2.addDependency( testDependency3 );

        group.addProject( project2 );
        defaultProjectGroup.addProject( project2 );

        ProjectNotifier groupNotifier1 = createTestNotifier( testGroupNotifier1 );
        group.addNotifier( groupNotifier1 );
        defaultProjectGroup.addNotifier( testGroupNotifier1 );
        ProjectNotifier groupNotifier2 = createTestNotifier( testGroupNotifier2 );
        group.addNotifier( groupNotifier2 );
        defaultProjectGroup.addNotifier( testGroupNotifier2 );

        BuildDefinition groupBuildDefinition1 = createTestBuildDefinition( testGroupBuildDefinition1 );
        group.addBuildDefinition( groupBuildDefinition1 );
        defaultProjectGroup.addBuildDefinition( testGroupBuildDefinition1 );

        store.addProjectGroup( group );
        defaultProjectGroup.setId( group.getId() );
        testProject1.setId( project1.getId() );
        testBuildResult1.setId( buildResult1.getId() );
        testBuildResult2.setId( buildResult2.getId() );
        testProject2.setId( project2.getId() );
        testBuildResult3.setId( buildResult3.getId() );

        group = createTestProjectGroup( testProjectGroup2 );

        ProjectNotifier groupNotifier3 = createTestNotifier( testGroupNotifier3 );
        group.addNotifier( groupNotifier3 );
        testProjectGroup2.addNotifier( testGroupNotifier3 );

        BuildDefinition groupBuildDefinition2 = createTestBuildDefinition( testGroupBuildDefinition2 );
        group.addBuildDefinition( groupBuildDefinition2 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition2 );

        BuildDefinition groupBuildDefinition3 = createTestBuildDefinition( testGroupBuildDefinition3 );
        group.addBuildDefinition( groupBuildDefinition3 );
        testProjectGroup2.addBuildDefinition( testGroupBuildDefinition3 );

        store.addProjectGroup( group );
        testProjectGroup2.setId( group.getId() );
    }

    // ----------------------------------------------------------------------
    //  TEST METHODS
    // ----------------------------------------------------------------------

    public void testAddProjectGroup()
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        String name = "testAddProjectGroup";
        String description = "testAddProjectGroup description";
        String groupId = "org.apache.maven.continuum.test";
        ProjectGroup group = createTestProjectGroup( name, description, groupId );

        ProjectGroup copy = createTestProjectGroup( group );
        store.addProjectGroup( group );
        copy.setId( group.getId() );

        ProjectGroup retrievedGroup = store.getProjectGroup( group.getId() );
        assertProjectGroupEquals( retrievedGroup, copy );
    }

    public void testGetProjectGroup()
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        ProjectGroup retrievedGroup = store.getProjectGroup( defaultProjectGroup.getId() );
        assertProjectGroupEquals( retrievedGroup, defaultProjectGroup );

        List projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );
        assertTrue( "Check existence of project 1", projects.contains( testProject1 ) );
        assertTrue( "Check existence of project 2", projects.contains( testProject2 ) );

        checkProjectGroupDefaultFetchGroup( retrievedGroup );

        Project project = (Project) projects.get( 0 );
        checkProjectDefaultFetchGroup( project );
        assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertProjectEquals( project, testProject1 );

        project = (Project) projects.get( 1 );
        checkProjectDefaultFetchGroup( project );
        assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertProjectEquals( project, testProject2 );
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
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        ProjectGroup newGroup = store.getProjectGroup( testProjectGroup2.getId() );

        String name = "testEditProjectGroup2";
        String description = "testEditProjectGroup updated description";
        String groupId = "org.apache.maven.continuum.test.new";
        newGroup.setName( name );
        newGroup.setDescription( description );
        newGroup.setGroupId( groupId );

        ProjectGroup copy = createTestProjectGroup( newGroup );
        copy.setId( newGroup.getId() );
        store.updateProjectGroup( newGroup );

        ProjectGroup retrievedGroup = store.getProjectGroup( testProjectGroup2.getId() );
        assertProjectGroupEquals( retrievedGroup, copy );

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
                assertProjectGroupEquals( group, testProjectGroup2 );
                assertTrue( "check no projects", projects.isEmpty() );
            }
            else if ( group.getId() == defaultProjectGroup.getId() )
            {
                assertProjectGroupEquals( group, defaultProjectGroup );
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
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        Project retrievedProject = store.getProject( testProject1.getId() );
        assertProjectEquals( retrievedProject, testProject1 );
        checkProjectDefaultFetchGroup( retrievedProject );
    }

    public void testGetProjectWithDetails()
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        Project retrievedProject = store.getProjectWithAllDetails( testProject1.getId() );
        assertProjectEquals( retrievedProject, testProject1 );
        checkProjectFetchGroup( retrievedProject, false, false, true, true );

        assertBuildDefinitionsEqual( retrievedProject.getBuildDefinitions(), testProject1.getBuildDefinitions() );
        assertNotifiersEqual( retrievedProject.getNotifiers(), testProject1.getNotifiers() );
        assertDevelopersEqual( retrievedProject.getDevelopers(), testProject1.getDevelopers() );
        assertDependenciesEqual( retrievedProject.getDependencies(), testProject1.getDependencies() );
    }

    public void testGetProjectWithCheckoutResult()
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        Project retrievedProject = store.getProjectWithCheckoutResult( testProject1.getId() );
        assertProjectEquals( retrievedProject, testProject1 );
        assertScmResultEquals( retrievedProject.getCheckoutResult(), testCheckoutResult1 );
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
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project newProject = store.getProject( testProject2.getId() );

        String name = "testEditProject2";
        String description = "testEditProject updated description";
        String groupId = "org.apache.maven.continuum.test.new";
        newProject.setName( name );
        newProject.setDescription( description );
        newProject.setGroupId( groupId );

        Project copy = createTestProject( newProject );
        copy.setId( newProject.getId() );
        store.updateProject( newProject );

        Project retrievedProject = store.getProject( testProject2.getId() );
        assertProjectEquals( retrievedProject, copy );

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
        throws ContinuumStoreException
    {
        List projects = store.getAllProjectsByName();
        assertEquals( "check items", Arrays.asList( new Project[]{testProject1, testProject2} ), projects );

        Project project = (Project) projects.get( 1 );
        assertProjectEquals( project, testProject2 );
        checkProjectDefaultFetchGroup( project );
        assertNotNull( "Check project group reference matches", project.getProjectGroup() );
    }

    public void testAddSchedule()
        throws ContinuumStoreException
    {
        Schedule newSchedule = createTestSchedule( "testAddSchedule", "testAddSchedule desc", 10, "cron test", false );
        Schedule copy = createTestSchedule( newSchedule );
        store.addSchedule( newSchedule );
        copy.setId( newSchedule.getId() );

        List schedules = store.getAllSchedulesByName();
        Schedule retrievedSchedule = (Schedule) schedules.get( schedules.size() - 1 );
        assertScheduleEquals( retrievedSchedule, copy );
    }

    public void testEditSchedule()
        throws ContinuumStoreException
    {
        Schedule newSchedule = (Schedule) store.getAllSchedulesByName().get( 0 );
        String name = "name1.1";
        String description = "testEditSchedule updated description";
        newSchedule.setName( name );
        newSchedule.setDescription( description );

        Schedule copy = createTestSchedule( newSchedule );
        copy.setId( newSchedule.getId() );
        store.updateSchedule( newSchedule );

        Schedule retrievedSchedule = (Schedule) store.getAllSchedulesByName().get( 0 );
        assertScheduleEquals( retrievedSchedule, copy );
    }

    public void testRemoveSchedule()
        throws ContinuumStoreException
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
        assertScheduleEquals( schedule, testSchedule1 );
        schedule = (Schedule) schedules.get( 1 );
        assertScheduleEquals( schedule, testSchedule2 );
        schedule = (Schedule) schedules.get( 2 );
        assertScheduleEquals( schedule, testSchedule3 );
    }

    public void testAddProfile()
        throws ContinuumStoreException
    {
        Installation installationJava14 = createTestInstallation( testInstallationJava14 );
        Installation installationMaven20a3 = createTestInstallation( testInstallationMaven20a3 );
        Profile newProfile = createTestProfile( "testAddProfile", "testAddProfile desc", 5, false, false,
                                                installationJava14, installationMaven20a3 );
        Profile copy = createTestProfile( newProfile );
        store.addProfile( newProfile );
        copy.setId( newProfile.getId() );

        List profiles = store.getAllProfilesByName();
        Profile retrievedProfile = (Profile) profiles.get( profiles.size() - 1 );
        assertProfileEquals( retrievedProfile, copy );
        assertInstallationEquals( retrievedProfile.getBuilder(), testInstallationMaven20a3 );
        assertInstallationEquals( retrievedProfile.getJdk(), testInstallationJava14 );
    }

    public void testEditProfile()
        throws ContinuumStoreException
    {
        Profile newProfile = (Profile) store.getAllProfilesByName().get( 0 );
        String name = "name1.1";
        String description = "testEditProfile updated description";
        newProfile.setName( name );
        newProfile.setDescription( description );

        Profile copy = createTestProfile( newProfile );
        store.updateProfile( newProfile );

        Profile retrievedProfile = (Profile) store.getAllProfilesByName().get( 0 );
        assertProfileEquals( retrievedProfile, copy );
        assertInstallationEquals( retrievedProfile.getBuilder(), copy.getBuilder() );
        assertInstallationEquals( retrievedProfile.getJdk(), copy.getJdk() );

    }

    public void testRemoveProfile()
        throws ContinuumStoreException
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
        assertProfileEquals( profile, testProfile1 );
        assertInstallationEquals( profile.getBuilder(), testProfile1.getBuilder() );
        assertInstallationEquals( profile.getJdk(), testProfile1.getJdk() );
        profile = (Profile) profiles.get( 1 );
        assertProfileEquals( profile, testProfile2 );
        assertInstallationEquals( profile.getBuilder(), testProfile2.getBuilder() );
        assertInstallationEquals( profile.getJdk(), testProfile2.getJdk() );
        profile = (Profile) profiles.get( 2 );
        assertProfileEquals( profile, testProfile3 );
        assertInstallationEquals( profile.getBuilder(), testProfile3.getBuilder() );
        assertInstallationEquals( profile.getJdk(), testProfile3.getJdk() );
    }

    public void testGetAllInstallations()
    {
        List installations = store.getAllInstallations();

        assertEquals( "check item count", 3, installations.size() );

        // check equality and order
        Installation installation = (Installation) installations.get( 0 );
        assertInstallationEquals( installation, testInstallationJava13 );
        installation = (Installation) installations.get( 1 );
        assertInstallationEquals( installation, testInstallationJava14 );
        installation = (Installation) installations.get( 2 );
        assertInstallationEquals( installation, testInstallationMaven20a3 );
    }

    public void testDeleteProject()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithBuilds( testProject1.getId() );

        store.removeProject( project );

        ProjectGroup projectGroup = store.getProjectGroup( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getProjects().size() );
        assertProjectEquals( (Project) projectGroup.getProjects().get( 0 ), testProject2 );

        confirmProjectDeletion( testProject1 );
    }

    public void testDeleteProjectGroup()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
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
        throws ContinuumStoreException, ContinuumObjectNotFoundException
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
        assertBuildResultEquals( (BuildResult) project.getBuildResults().get( 0 ), testBuildResult2 );

        List results = store.getAllBuildsForAProjectByDate( testProject1.getId() );
        assertEquals( "check item count", 1, results.size() );
        assertBuildResultEquals( (BuildResult) results.get( 0 ), testBuildResult2 );

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
        assertBuildResultEquals( buildResult, testBuildResult2 );
        assertProjectEquals( buildResult.getProject(), testProject1 );
        checkBuildResultDefaultFetchGroup( buildResult );
        buildResult = (BuildResult) results.get( 1 );
        assertBuildResultEquals( buildResult, testBuildResult1 );
        assertProjectEquals( buildResult.getProject(), testProject1 );
        checkBuildResultDefaultFetchGroup( buildResult );
    }

    public void testGetBuildResult()
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        BuildResult buildResult = store.getBuildResult( testBuildResult3.getId() );
        assertBuildResultEquals( buildResult, testBuildResult3 );
        assertScmResultEquals( buildResult.getScmResult(), testBuildResult3.getScmResult() );
        assertProjectEquals( buildResult.getProject(), testProject2 );
        // TODO: reports, artifacts, data
    }

    public void testGetProjectGroupWithDetails()
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        ProjectGroup retrievedGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertProjectGroupEquals( retrievedGroup, defaultProjectGroup );
        assertNotifiersEqual( retrievedGroup.getNotifiers(), defaultProjectGroup.getNotifiers() );
        assertBuildDefinitionsEqual( retrievedGroup.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );

        List projects = retrievedGroup.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = (Project) projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertProjectEquals( project, testProject1 );
        assertNotifiersEqual( project.getNotifiers(), testProject1.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = (Project) projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), retrievedGroup );
        assertProjectEquals( project, testProject2 );
        assertNotifiersEqual( project.getNotifiers(), testProject2.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );
    }

    public void testGetAllProjectsGroupWithDetails()
        throws ContinuumObjectNotFoundException
    {
        List projectGroups = store.getAllProjectGroupsWithBuildDetails();
        ProjectGroup group1 = (ProjectGroup) projectGroups.get( 0 );
        assertProjectGroupEquals( group1, defaultProjectGroup );
        assertNotifiersEqual( group1.getNotifiers(), defaultProjectGroup.getNotifiers() );
        assertBuildDefinitionsEqual( group1.getBuildDefinitions(), defaultProjectGroup.getBuildDefinitions() );
        ProjectGroup group2 = (ProjectGroup) projectGroups.get( 1 );
        assertProjectGroupEquals( group2, testProjectGroup2 );
        assertNotifiersEqual( group2.getNotifiers(), testProjectGroup2.getNotifiers() );
        assertBuildDefinitionsEqual( group2.getBuildDefinitions(), testProjectGroup2.getBuildDefinitions() );

        List projects = group1.getProjects();
        assertEquals( "Check number of projects", 2, projects.size() );

        Project project = (Project) projects.get( 0 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( project, testProject1 );
        assertNotifiersEqual( project.getNotifiers(), testProject1.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject1.getBuildDefinitions() );

        project = (Project) projects.get( 1 );
        checkProjectFetchGroup( project, false, false, true, false );
        assertSame( "Check project group reference matches", project.getProjectGroup(), group1 );
        assertProjectEquals( project, testProject2 );
        assertNotifiersEqual( project.getNotifiers(), testProject2.getNotifiers() );
        assertBuildDefinitionsEqual( project.getBuildDefinitions(), testProject2.getBuildDefinitions() );

        projects = group2.getProjects();
        assertEquals( "Check number of projects", 0, projects.size() );
    }

    public void testAddDeveloperToProject()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper developer = createTestDeveloper( 11, "email TADTP", "name TADTP", "scmId TADTP" );
        ProjectDeveloper copy = createTestDeveloper( developer );
        project.addDeveloper( developer );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 2, project.getDevelopers().size() );
        assertDeveloperEquals( (ProjectDeveloper) project.getDevelopers().get( 1 ), copy );
    }

    public void testEditDeveloper()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDeveloper newDeveloper = (ProjectDeveloper) project.getDevelopers().get( 0 );
        String name = "name1.1";
        String email = "email1.1";
        newDeveloper.setName( name );
        newDeveloper.setEmail( email );

        ProjectDeveloper copy = createTestDeveloper( newDeveloper );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # devs", 1, project.getDevelopers().size() );
        assertDeveloperEquals( (ProjectDeveloper) project.getDevelopers().get( 0 ), copy );
    }

    public void testDeleteDeveloper()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
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
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency dependency = createTestDependency( "TADTP groupId", "TADTP artifactId", "TADTP version" );
        ProjectDependency copy = createTestDependency( dependency );
        project.addDependency( dependency );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 3, project.getDependencies().size() );
        assertDependencyEquals( (ProjectDependency) project.getDependencies().get( 2 ), copy );
    }

    public void testEditDependency()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectDependency newDependency = (ProjectDependency) project.getDependencies().get( 0 );
        String groupId = "groupId1.1";
        String artifactId = "artifactId1.1";
        newDependency.setGroupId( groupId );
        newDependency.setArtifactId( artifactId );

        ProjectDependency copy = createTestDependency( newDependency );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # deps", 2, project.getDependencies().size() );
        assertDependencyEquals( (ProjectDependency) project.getDependencies().get( 0 ), copy );
    }

    public void testDeleteDependency()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );
        ProjectDependency dependency = (ProjectDependency) project.getDependencies().get( 1 );
        project.getDependencies().remove( 0 );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getDependencies().size() );
        assertDependencyEquals( (ProjectDependency) project.getDependencies().get( 0 ), dependency );

        // !! These actually aren't happening !!
        // TODO: test the dependency was physically deleted
    }

    public void testAddNotifierToProject()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier notifier = createTestNotifier( 13, true, false, true, "TADNTP type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        project.addNotifier( notifier );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 2, project.getNotifiers().size() );
        assertNotifierEquals( (ProjectNotifier) project.getNotifiers().get( 1 ), copy );
    }

    public void testEditNotifier()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        ProjectNotifier newNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );
        String type = "type1.1";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # notifiers", 1, project.getNotifiers().size() );
        assertNotifierEquals( (ProjectNotifier) project.getNotifiers().get( 0 ), copy );
    }

    public void testDeleteNotifier()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
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
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        Profile profile = store.getProfile( testProfile1.getId() );
        Schedule schedule = store.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition = createTestBuildDefinition( "TABDTP arguments", "TABDTP buildFile",
                                                                     "TABDTP goals", profile, schedule );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        project.addBuildDefinition( buildDefinition );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 3, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 2 );
        assertBuildDefinitionEquals( retrievedBuildDefinition, copy );
        assertScheduleEquals( retrievedBuildDefinition.getSchedule(), testSchedule1 );
        assertProfileEquals( retrievedBuildDefinition.getProfile(), testProfile1 );
    }

    public void testEditBuildDefinition()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );

        BuildDefinition newBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        String arguments = "arguments1.1";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check # build defs", 2, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( retrievedBuildDefinition, copy );
        assertScheduleEquals( retrievedBuildDefinition.getSchedule(), testSchedule1 );
        assertProfileEquals( retrievedBuildDefinition.getProfile(), testProfile2 );
    }

    public void testDeleteBuildDefinition()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        Project project = store.getProjectWithAllDetails( testProject1.getId() );
        BuildDefinition buildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 1 );
        project.getBuildDefinitions().remove( 0 );
        store.updateProject( project );

        project = store.getProjectWithAllDetails( testProject1.getId() );
        assertEquals( "check size is now 1", 1, project.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) project.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( retrievedBuildDefinition, buildDefinition );
        assertScheduleEquals( retrievedBuildDefinition.getSchedule(), testSchedule2 );
        assertProfileEquals( retrievedBuildDefinition.getProfile(), testProfile2 );

        // !! These actually aren't happening !!
        // TODO: test the def was physically deleted
        // TODO: test the schedule/profile was NOT physically deleted
    }

    public void testAddNotifierToProjectGroup()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        ProjectNotifier notifier = createTestNotifier( 14, true, false, true, "TADNTPG type" );
        ProjectNotifier copy = createTestNotifier( notifier );
        projectGroup.addNotifier( notifier );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 3, projectGroup.getNotifiers().size() );
        assertNotifierEquals( (ProjectNotifier) projectGroup.getNotifiers().get( 2 ), copy );
    }

    public void testEditGroupNotifier()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        ProjectNotifier newNotifier = (ProjectNotifier) projectGroup.getNotifiers().get( 0 );
        String type = "type1.1";
        newNotifier.setType( type );

        ProjectNotifier copy = createTestNotifier( newNotifier );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # notifiers", 2, projectGroup.getNotifiers().size() );
        assertNotifierEquals( (ProjectNotifier) projectGroup.getNotifiers().get( 0 ), copy );
    }

    public void testDeleteGroupNotifier()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        ProjectNotifier notifier = (ProjectNotifier) projectGroup.getNotifiers().get( 1 );
        projectGroup.getNotifiers().remove( 0 );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check size is now 1", 1, projectGroup.getNotifiers().size() );
        assertNotifierEquals( (ProjectNotifier) projectGroup.getNotifiers().get( 0 ), notifier );

        // !! These actually aren't happening !!
        // TODO: test the notifier was physically deleted
    }

    public void testAddBuildDefinitionToProjectGroup()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        Profile profile = store.getProfile( testProfile1.getId() );
        Schedule schedule = store.getSchedule( testSchedule1.getId() );
        BuildDefinition buildDefinition = createTestBuildDefinition( "TABDTPG arguments", "TABDTPG buildFile",
                                                                     "TABDTPG goals", profile, schedule );
        BuildDefinition copy = createTestBuildDefinition( buildDefinition );
        projectGroup.addBuildDefinition( buildDefinition );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 2, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 1 );
        assertBuildDefinitionEquals( retrievedBuildDefinition, copy );
        assertScheduleEquals( retrievedBuildDefinition.getSchedule(), testSchedule1 );
        assertProfileEquals( retrievedBuildDefinition.getProfile(), testProfile1 );
    }

    public void testEditGroupBuildDefinition()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );

        BuildDefinition newBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );
        String arguments = "arguments1.1";
        newBuildDefinition.setArguments( arguments );

        BuildDefinition copy = createTestBuildDefinition( newBuildDefinition );
        store.updateProjectGroup( projectGroup );

        projectGroup = store.getProjectGroupWithBuildDetails( defaultProjectGroup.getId() );
        assertEquals( "check # build defs", 1, projectGroup.getBuildDefinitions().size() );
        BuildDefinition retrievedBuildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );
        assertBuildDefinitionEquals( retrievedBuildDefinition, copy );
        assertScheduleEquals( retrievedBuildDefinition.getSchedule(), testSchedule2 );
        assertProfileEquals( retrievedBuildDefinition.getProfile(), testProfile1 );
    }

    public void testDeleteGroupBuildDefinition()
        throws ContinuumStoreException, ContinuumObjectNotFoundException
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

    private static BuildDefinition createTestBuildDefinition( BuildDefinition buildDefinition )
    {
        return createTestBuildDefinition( buildDefinition.getArguments(), buildDefinition.getBuildFile(),
                                          buildDefinition.getGoals(), buildDefinition.getProfile(),
                                          buildDefinition.getSchedule() );
    }

    private static BuildDefinition createTestBuildDefinition( String arguments, String buildFile, String goals,
                                                              Profile profile, Schedule schedule )
    {
        BuildDefinition definition = new BuildDefinition();
        definition.setArguments( arguments );
        definition.setBuildFile( buildFile );
        definition.setGoals( goals );
        definition.setProfile( profile );
        definition.setSchedule( schedule );
        return definition;
    }

    private static ProjectNotifier createTestNotifier( ProjectNotifier notifier )
    {
        return createTestNotifier( notifier.getRecipientType(), notifier.isSendOnError(), notifier.isSendOnFailure(),
                                   notifier.isSendOnSuccess(), notifier.getType() );
    }

    private static ProjectNotifier createTestNotifier( int recipientType, boolean sendOnError, boolean sendOnFailure,
                                                       boolean sendOnSuccess, String type )
    {
        Map configuration = new HashMap();
        configuration.put( "key1", "value1" );
        configuration.put( "key2", "value2" );

        ProjectNotifier notifier = new ProjectNotifier();
        notifier.setConfiguration( configuration );
        notifier.setRecipientType( recipientType );
        notifier.setSendOnError( sendOnError );
        notifier.setSendOnFailure( sendOnFailure );
        notifier.setSendOnSuccess( sendOnSuccess );
        notifier.setType( type );

        return notifier;
    }

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

    private static ScmResult createTestScmResult( ScmResult scmResult, String base )
    {
        return createTestScmResult( scmResult.getCommandOutput(), scmResult.getProviderMessage(), scmResult.isSuccess(),
                                    base );
    }

    private static ScmResult createTestScmResult( String commandOutput, String providerMessage, boolean success,
                                                  String base )
    {
        ScmResult scmResult = new ScmResult();
        scmResult.setCommandOutput( commandOutput );
        scmResult.setProviderMessage( providerMessage );
        scmResult.setSuccess( success );

        List changes = new ArrayList();
        changes.add( createTestChangeSet( "author" + base + ".1", "comment" + base + ".1", base + ".1" ) );
        changes.add( createTestChangeSet( "author" + base + ".2", "comment" + base + ".2", base + ".2" ) );
        scmResult.setChanges( changes );
        return scmResult;
    }

    private static ChangeSet createTestChangeSet( String author, String comment, String base )
    {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setAuthor( author );
        changeSet.setComment( comment );
        changeSet.setDate( System.currentTimeMillis() );
        List files = new ArrayList();
        files.add( createTestChangeFile( "name" + base + ".1", "rev" + base + ".1" ) );
        files.add( createTestChangeFile( "name" + base + ".2", "rev" + base + ".2" ) );
        files.add( createTestChangeFile( "name" + base + ".3", "rev" + base + ".3" ) );
        changeSet.setFiles( files );
        return changeSet;
    }

    private static ChangeFile createTestChangeFile( String name, String revision )
    {
        ChangeFile changeFile = new ChangeFile();
        changeFile.setName( name );
        changeFile.setRevision( revision );
        return changeFile;
    }

    private static BuildResult createTestBuildResult( BuildResult buildResult )
    {
        return createTestBuildResult( buildResult.getTrigger(), buildResult.isSuccess(), buildResult.getState(),
                                      buildResult.getExitCode(), buildResult.getError(), buildResult.getBuildNumber(),
                                      buildResult.getStartTime(), buildResult.getEndTime() );
    }

    private static BuildResult createTestBuildResult( int trigger, boolean success, int state, int exitCode,
                                                      String error, int buildNumber, long startTime, long endTime )
    {
        BuildResult result = new BuildResult();
        result.setBuildNumber( buildNumber );
        result.setStartTime( startTime );
        result.setEndTime( endTime );
        result.setError( error );
        result.setExitCode( exitCode );
        result.setState( state );
        result.setSuccess( success );
        result.setTrigger( trigger );
        return result;
    }

    private static Installation createTestInstallation( String name, String path, String version )
    {
        Installation installation = new Installation();
        installation.setName( name );
        installation.setPath( path );
        installation.setVersion( version );
        return installation;
    }

    private static Installation createTestInstallation( Installation installation )
    {
        return createTestInstallation( installation.getName(), installation.getPath(), installation.getVersion() );
    }

    private static Schedule createTestSchedule( Schedule schedule )
    {
        return createTestSchedule( schedule.getName(), schedule.getDescription(), schedule.getDelay(),
                                   schedule.getCronExpression(), schedule.isActive() );
    }

    private static Schedule createTestSchedule( String name, String description, int delay, String cronExpression,
                                                boolean active )
    {
        Schedule schedule = new Schedule();
        schedule.setActive( active );
        schedule.setCronExpression( cronExpression );
        schedule.setDelay( delay );
        schedule.setDescription( description );
        schedule.setName( name );
        return schedule;
    }

    private static Profile createTestProfile( Profile profile )
    {
        return createTestProfile( profile.getName(), profile.getDescription(), profile.getScmMode(),
                                  profile.isBuildWithoutChanges(), profile.isActive(), profile.getJdk(),
                                  profile.getBuilder() );
//                                  createTestInstallation( profile.getJdk() ),
//                                  createTestInstallation( profile.getBuilder() ) );
    }

    private static Profile createTestProfile( String name, String description, int scmMode, boolean buildWithoutChanges,
                                              boolean active, Installation jdk, Installation builder )
    {
        Profile profile = new Profile();
        profile.setActive( active );
        profile.setBuildWithoutChanges( buildWithoutChanges );
        profile.setScmMode( scmMode );
        profile.setDescription( description );
        profile.setName( name );
        profile.setBuilder( builder );
        profile.setJdk( jdk );
        return profile;
    }

    private static ProjectGroup createTestProjectGroup( ProjectGroup group )
    {
        return createTestProjectGroup( group.getName(), group.getDescription(), group.getGroupId() );
    }

    private static ProjectGroup createTestProjectGroup( String name, String description, String groupId )
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( name );
        group.setDescription( description );
        group.setGroupId( groupId );
        return group;
    }

    private static Project createTestProject( Project project )
    {
        return createTestProject( project.getArtifactId(), project.getBuildNumber(), project.getDescription(),
                                  project.getGroupId(), project.getName(), project.getScmUrl(), project.getState(),
                                  project.getUrl(), project.getVersion(), project.getWorkingDirectory() );
    }

    private static Project createTestProject( String artifactId, int buildNumber, String description, String groupId,
                                              String name, String scmUrl, int state, String url, String version,
                                              String workingDirectory )
    {
        Project project = new Project();
        project.setArtifactId( artifactId );
        project.setBuildNumber( buildNumber );
        project.setDescription( description );
        project.setGroupId( groupId );
        project.setName( name );
        project.setScmUrl( scmUrl );
        project.setState( state );
        project.setUrl( url );
        project.setVersion( version );
        project.setWorkingDirectory( workingDirectory );
        return project;
    }

    private static void assertProjectEquals( Project retrievedProject, Project project )
    {
        assertEquals( "compare projects", retrievedProject, project );
        assertNotSame( project, retrievedProject );
        // aggressive compare, as equals is using the identity
        assertEquals( "compare project - name", project.getName(), retrievedProject.getName() );
        assertEquals( "compare project - desc", project.getDescription(), retrievedProject.getDescription() );
        assertEquals( "compare project - groupId", project.getGroupId(), retrievedProject.getGroupId() );
        assertEquals( "compare project - artifactId", project.getArtifactId(), retrievedProject.getArtifactId() );
        assertEquals( "compare project - buildNumber", project.getBuildNumber(), retrievedProject.getBuildNumber() );
        assertEquals( "compare project - scmUrl", project.getScmUrl(), retrievedProject.getScmUrl() );
        assertEquals( "compare project - state", project.getState(), retrievedProject.getState() );
        assertEquals( "compare project - url", project.getUrl(), retrievedProject.getUrl() );
        assertEquals( "compare project - version", project.getVersion(), retrievedProject.getVersion() );
        assertEquals( "compare project - workingDirectory", project.getWorkingDirectory(),
                      retrievedProject.getWorkingDirectory() );
    }

    private static void assertProjectGroupEquals( ProjectGroup retrievedGroup, ProjectGroup group )
    {
        assertEquals( "compare project groups", retrievedGroup, group );
        assertNotSame( group, retrievedGroup );
        // aggressive compare, as equals is using the identity
        assertEquals( "compare project groups - name", group.getName(), retrievedGroup.getName() );
        assertEquals( "compare project groups - desc", group.getDescription(), retrievedGroup.getDescription() );
        assertEquals( "compare project groups - groupId", group.getGroupId(), retrievedGroup.getGroupId() );
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

    private static void assertScheduleEquals( Schedule retrievedSchedule, Schedule schedule )
    {
        assertNotSame( schedule, retrievedSchedule );
        assertEquals( "compare schedule - id", schedule.getId(), retrievedSchedule.getId() );
        assertEquals( "compare schedule - name", schedule.getName(), retrievedSchedule.getName() );
        assertEquals( "compare schedule - desc", schedule.getDescription(), retrievedSchedule.getDescription() );
        assertEquals( "compare schedule - delay", schedule.getDelay(), retrievedSchedule.getDelay() );
        assertEquals( "compare schedule - cron", schedule.getCronExpression(), retrievedSchedule.getCronExpression() );
        assertEquals( "compare schedule - active", schedule.isActive(), retrievedSchedule.isActive() );
    }

    private static void assertProfileEquals( Profile retrievedProfile, Profile profile )
    {
        assertNotSame( profile, retrievedProfile );
        assertEquals( "compare profile - name", profile.getName(), retrievedProfile.getName() );
        assertEquals( "compare profile - desc", profile.getDescription(), retrievedProfile.getDescription() );
        assertEquals( "compare profile - scmMode", profile.getScmMode(), retrievedProfile.getScmMode() );
        assertEquals( "compare profile - build w/o changes", profile.isBuildWithoutChanges(),
                      retrievedProfile.isBuildWithoutChanges() );
        assertEquals( "compare profile - active", profile.isActive(), retrievedProfile.isActive() );
    }

    private static void assertInstallationEquals( Installation retrievedInstallation, Installation installation )
    {
        assertEquals( "compare installation - name", installation.getName(), retrievedInstallation.getName() );
        assertEquals( "compare installation - path", installation.getPath(), retrievedInstallation.getPath() );
        assertEquals( "compare installation - version", installation.getVersion(), retrievedInstallation.getVersion() );
    }

    private static void assertBuildResultEquals( BuildResult retrievedBuildResult, BuildResult buildResult )
    {
        assertEquals( "compare build result - build #", buildResult.getBuildNumber(),
                      retrievedBuildResult.getBuildNumber() );
        assertEquals( "compare build result - end time", buildResult.getEndTime(), retrievedBuildResult.getEndTime() );
        assertEquals( "compare build result - error", buildResult.getError(), retrievedBuildResult.getError() );
        assertEquals( "compare build result - exit code", buildResult.getExitCode(),
                      retrievedBuildResult.getExitCode() );
        assertEquals( "compare build result - start time", buildResult.getStartTime(),
                      retrievedBuildResult.getStartTime() );
        assertEquals( "compare build result - state", buildResult.getState(), retrievedBuildResult.getState() );
        assertEquals( "compare build result - trigger", buildResult.getTrigger(), retrievedBuildResult.getTrigger() );
    }

    private static void assertScmResultEquals( ScmResult retrievedScmResult, ScmResult scmResult )
    {
        assertEquals( "compare SCM result - output", scmResult.getCommandOutput(),
                      retrievedScmResult.getCommandOutput() );
        assertEquals( "compare SCM result - message", scmResult.getProviderMessage(),
                      retrievedScmResult.getProviderMessage() );
        assertEquals( "compare SCM result - success", scmResult.isSuccess(), retrievedScmResult.isSuccess() );
        assertEquals( "compare SCM result - changes size", retrievedScmResult.getChanges().size(),
                      scmResult.getChanges().size() );
        for ( int i = 0; i < retrievedScmResult.getChanges().size(); i++ )
        {
            assertChangeSetEquals( (ChangeSet) retrievedScmResult.getChanges().get( i ),
                                   (ChangeSet) scmResult.getChanges().get( i ) );
        }
    }

    private static void assertChangeSetEquals( ChangeSet retrievedChangeSet, ChangeSet changeSet )
    {
        assertEquals( "compare change set result - author", changeSet.getAuthor(), retrievedChangeSet.getAuthor() );
        assertEquals( "compare change set result - comment", changeSet.getComment(), retrievedChangeSet.getComment() );
        //Remove this test, in some case we have a 1ms difference between two dates
        //assertEquals( "compare change set result - date", changeSet.getDate(), retrievedChangeSet.getDate() );
        assertEquals( "compare change set result - files size", retrievedChangeSet.getFiles().size(),
                      changeSet.getFiles().size() );
        for ( int i = 0; i < retrievedChangeSet.getFiles().size(); i++ )
        {
            assertChangeFileEquals( (ChangeFile) retrievedChangeSet.getFiles().get( i ),
                                    (ChangeFile) changeSet.getFiles().get( i ) );
        }
    }

    private static void assertChangeFileEquals( ChangeFile retrievedChangeFile, ChangeFile changeFile )
    {
        assertEquals( "compare change file result - name", retrievedChangeFile.getName(), changeFile.getName() );
        assertEquals( "compare change file result - revision", retrievedChangeFile.getRevision(),
                      changeFile.getRevision() );
    }

    private static void assertNotifiersEqual( List retrievedNotifiers, List notifiers )
    {
        for ( int i = 0; i < retrievedNotifiers.size(); i++ )
        {
            assertNotifierEquals( (ProjectNotifier) retrievedNotifiers.get( i ), (ProjectNotifier) notifiers.get( i ) );
        }
    }

    private static void assertNotifierEquals( ProjectNotifier retrievedNotifier, ProjectNotifier notifier )
    {
        assertEquals( "compare notifier - recipient type", notifier.getRecipientType(),
                      retrievedNotifier.getRecipientType() );
        assertEquals( "compare notifier - type", notifier.getType(), retrievedNotifier.getType() );
        assertEquals( "compare notifier - configuration", notifier.getConfiguration(),
                      retrievedNotifier.getConfiguration() );
        assertEquals( "compare notifier - send on success", notifier.isSendOnSuccess(),
                      retrievedNotifier.isSendOnSuccess() );
        assertEquals( "compare notifier - send on failure", notifier.isSendOnFailure(),
                      retrievedNotifier.isSendOnFailure() );
        assertEquals( "compare notifier - send on error", notifier.isSendOnError(), retrievedNotifier.isSendOnError() );
    }

    private static void assertBuildDefinitionsEqual( List retrievedBuildDefinitions, List buildDefinitions )
    {
        for ( int i = 0; i < retrievedBuildDefinitions.size(); i++ )
        {
            BuildDefinition retrievedBuildDefinition = (BuildDefinition) retrievedBuildDefinitions.get( i );
            BuildDefinition buildDefinition = (BuildDefinition) buildDefinitions.get( i );
            assertBuildDefinitionEquals( retrievedBuildDefinition, buildDefinition );
            assertScheduleEquals( retrievedBuildDefinition.getSchedule(), buildDefinition.getSchedule() );
            assertProfileEquals( retrievedBuildDefinition.getProfile(), buildDefinition.getProfile() );
        }
    }

    private static void assertBuildDefinitionEquals( BuildDefinition retrievedBuildDefinition,
                                                     BuildDefinition buildDefinition )
    {
        assertEquals( "compare build definition - arguments", buildDefinition.getArguments(),
                      retrievedBuildDefinition.getArguments() );
        assertEquals( "compare build definition - build file", buildDefinition.getBuildFile(),
                      retrievedBuildDefinition.getBuildFile() );
        assertEquals( "compare build definition - goals", buildDefinition.getGoals(),
                      retrievedBuildDefinition.getGoals() );
    }

    private static void assertDevelopersEqual( List retrievedDevelopers, List developers )
    {
        for ( int i = 0; i < retrievedDevelopers.size(); i++ )
        {
            assertDeveloperEquals( (ProjectDeveloper) retrievedDevelopers.get( i ),
                                   (ProjectDeveloper) developers.get( i ) );
        }
    }

    private static void assertDeveloperEquals( ProjectDeveloper retrievedDeveloper, ProjectDeveloper developer )
    {
        assertEquals( "compare developer - name", developer.getName(), retrievedDeveloper.getName() );
        assertEquals( "compare developer - email", developer.getEmail(), retrievedDeveloper.getEmail() );
        assertEquals( "compare developer - scmId", developer.getScmId(), retrievedDeveloper.getScmId() );
        assertEquals( "compare developer - continuumId", developer.getContinuumId(),
                      retrievedDeveloper.getContinuumId() );
    }

    private static void assertDependenciesEqual( List retrievedDependencies, List dependencies )
    {
        for ( int i = 0; i < retrievedDependencies.size(); i++ )
        {
            assertDependencyEquals( (ProjectDependency) retrievedDependencies.get( i ),
                                    (ProjectDependency) dependencies.get( i ) );
        }
    }

    private static void assertDependencyEquals( ProjectDependency retrievedDependency, ProjectDependency dependency )
    {
        assertEquals( "compare dependency - groupId", dependency.getGroupId(), retrievedDependency.getGroupId() );
        assertEquals( "compare dependency - artifactId", dependency.getArtifactId(),
                      retrievedDependency.getArtifactId() );
        assertEquals( "compare dependency - version", dependency.getVersion(), retrievedDependency.getVersion() );
    }

    private static ProjectDependency createTestDependency( ProjectDependency dependency )
    {
        return createTestDependency( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }

    private static ProjectDeveloper createTestDeveloper( ProjectDeveloper developer )
    {
        return createTestDeveloper( developer.getContinuumId(), developer.getEmail(), developer.getName(),
                                    developer.getScmId() );
    }

    private static ProjectDependency createTestDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency dependency = new ProjectDependency();
        dependency.setArtifactId( artifactId );
        dependency.setGroupId( groupId );
        dependency.setVersion( version );
        return dependency;
    }

    private static ProjectDeveloper createTestDeveloper( int continuumId, String email, String name, String scmId )
    {
        ProjectDeveloper developer = new ProjectDeveloper();
        developer.setContinuumId( continuumId );
        developer.setEmail( email );
        developer.setName( name );
        developer.setScmId( scmId );
        return developer;
    }

    /**
     * Setup JDO Factory
     *
     * @todo push down to a Jdo specific test
     */
    private ContinuumStore createStore()
        throws Exception
    {
        ConfigurableJdoFactory jdoFactory = (ConfigurableJdoFactory) lookup( JdoFactory.ROLE );
        assertEquals( DefaultConfigurableJdoFactory.class.getName(), jdoFactory.getClass().getName() );

        jdoFactory.setPersistenceManagerFactoryClass( "org.jpox.PersistenceManagerFactoryImpl" );

        // TODO: add ability to test with various
        jdoFactory.setDriverName( "org.hsqldb.jdbcDriver" );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:" + getName() );

        jdoFactory.setUserName( "sa" );

        jdoFactory.setPassword( "" );

        jdoFactory.setProperty( "org.jpox.transactionIsolation", "READ_UNCOMMITTED" );

        jdoFactory.setProperty( "org.jpox.poid.transactionIsolation", "READ_UNCOMMITTED" );

        jdoFactory.setProperty( "org.jpox.autoCreateSchema", "true" );

        Properties properties = jdoFactory.getProperties();

        for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }

        File file = getTestFile( "../continuum-model/target/classes/META-INF/package.jdo" );
        SchemaTool.createSchemaTables( new String[]{file.getAbsolutePath()}, false );

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        assertNotNull( pmf );

        PersistenceManager pm = pmf.getPersistenceManager();

        pm.close();

        return (ContinuumStore) lookup( ContinuumStore.ROLE );
    }
}
