package org.apache.maven.continuum.store;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for tests using the continuum store.
 */
public abstract class AbstractContinuumStoreTestCase
    extends PlexusTestCase
{
    protected ContinuumStore store;

    protected ProjectGroup defaultProjectGroup;

    protected ProjectGroup testProjectGroup2;

    protected Project testProject1;

    protected Project testProject2;

    protected Schedule testSchedule1;

    protected Schedule testSchedule2;

    protected Schedule testSchedule3;

    protected Profile testProfile1;

    protected Profile testProfile2;

    protected Profile testProfile3;

    protected Installation testInstallationJava13;

    protected Installation testInstallationJava14;

    protected Installation testInstallationMaven20a3;

    protected BuildResult testBuildResult1;

    protected BuildResult testBuildResult2;

    protected BuildResult testBuildResult3;

    protected ScmResult testCheckoutResult1;

    private ProjectNotifier testGroupNotifier1;

    private ProjectNotifier testGroupNotifier2;

    private ProjectNotifier testGroupNotifier3;

    private ProjectNotifier testNotifier1;

    private ProjectNotifier testNotifier2;

    private ProjectNotifier testNotifier3;

    private ProjectDeveloper testDeveloper1;

    private ProjectDeveloper testDeveloper2;

    private ProjectDeveloper testDeveloper3;

    private ProjectDependency testDependency1;

    private ProjectDependency testDependency2;

    private ProjectDependency testDependency3;

    private SystemConfiguration systemConfiguration;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        store = createStore();
    }

    protected void createBuildDatabase()
    {
        createBuildDatabase( true );
    }

    protected void createBuildDatabase( boolean addToStore )
    {
        // Setting up test data
        defaultProjectGroup =
            createTestProjectGroup( "Default Group", "The Default Group", "org.apache.maven.test.default" );

        testProjectGroup2 = createTestProjectGroup( "test group 2", "test group 2 desc", "test group 2 groupId" );

        testProject1 = createTestProject( "artifactId1", 1, "description1", defaultProjectGroup.getGroupId(), "name1",
                                          "scmUrl1", 1, "url1", "version1", "workingDirectory1" );

        // state must be 1 unless we setup a build in the correct state
        testProject2 = createTestProject( "artifactId2", 2, "description2", defaultProjectGroup.getGroupId(), "name2",
                                          "scmUrl2", 1, "url2", "version2", "workingDirectory2" );

        testSchedule1 = createTestSchedule( "name1", "description1", 1, "cronExpression1", true );
        testSchedule2 = createTestSchedule( "name2", "description2", 2, "cronExpression2", true );
        testSchedule3 = createTestSchedule( "name3", "description3", 3, "cronExpression3", true );

        testInstallationJava13 = createTestInstallation( "JDK 1.3", "/usr/local/java-1.3", "1.3" );
        testInstallationJava14 = createTestInstallation( "JDK 1.4", "/usr/local/java-1.4", "1.4" );
        testInstallationMaven20a3 =
            createTestInstallation( "Maven 2.0 alpha 3", "/usr/local/maven-2.0-alpha-3", "2.0-alpha-3" );

        testGroupNotifier1 = createTestNotifier( 1, true, false, true, "type1" );
        testGroupNotifier2 = createTestNotifier( 2, false, true, false, "type2" );
        testGroupNotifier3 = createTestNotifier( 3, true, false, false, "type3" );

        testNotifier1 = createTestNotifier( 11, true, true, false, "type11" );
        testNotifier2 = createTestNotifier( 12, false, false, true, "type12" );
        testNotifier3 = createTestNotifier( 13, false, true, false, "type13" );

        testDeveloper1 = createTestDeveloper( 1, "email1", "name1", "scmId1" );
        testDeveloper2 = createTestDeveloper( 2, "email2", "name2", "scmId2" );
        testDeveloper3 = createTestDeveloper( 3, "email3", "name3", "scmId3" );

        testDependency1 = createTestDependency( "groupId1", "artifactId1", "version1" );
        testDependency2 = createTestDependency( "groupId2", "artifactId2", "version2" );
        testDependency3 = createTestDependency( "groupId3", "artifactId3", "version3" );

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
        if ( addToStore )
        {
            schedule2 = store.addSchedule( schedule2 );
            testSchedule2.setId( schedule2.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            testSchedule2.setId( 1 );
        }

        Schedule schedule1 = createTestSchedule( testSchedule1 );
        if ( addToStore )
        {
            schedule1 = store.addSchedule( schedule1 );
            testSchedule1.setId( schedule1.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            testSchedule1.setId( 2 );
        }

        Schedule schedule3 = createTestSchedule( testSchedule3 );
        if ( addToStore )
        {
            schedule3 = store.addSchedule( schedule3 );
            testSchedule3.setId( schedule3.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            testSchedule3.setId( 3 );
        }

        Installation installationJava14 = createTestInstallation( testInstallationJava14 );
        if ( addToStore )
        {
            installationJava14 = store.addInstallation( installationJava14 );
        }

        Installation installationMaven20a3 = createTestInstallation( testInstallationMaven20a3 );
        if ( addToStore )
        {
            installationMaven20a3 = store.addInstallation( installationMaven20a3 );
        }

        Installation installationJava13 = createTestInstallation( testInstallationJava13 );
        if ( addToStore )
        {
            installationJava13 = store.addInstallation( installationJava13 );
        }

        testProfile1 =
            createTestProfile( "name1", "description1", 1, true, true, installationJava13, installationMaven20a3 );
        testProfile2 =
            createTestProfile( "name2", "description2", 2, false, true, installationJava14, installationMaven20a3 );
        testProfile3 =
            createTestProfile( "name3", "description3", 3, true, false, installationJava14, installationMaven20a3 );

        Profile profile1 = createTestProfile( testProfile1 );
        if ( addToStore )
        {
            profile1 = store.addProfile( profile1 );
        }
        testProfile1.setId( profile1.getId() );

        Profile profile2 = createTestProfile( testProfile2 );
        if ( addToStore )
        {
            profile2 = store.addProfile( profile2 );
        }
        testProfile2.setId( profile2.getId() );

        Profile profile3 = createTestProfile( testProfile3 );
        if ( addToStore )
        {
            profile3 = store.addProfile( profile3 );
        }
        testProfile3.setId( profile3.getId() );

        BuildDefinition testGroupBuildDefinition1 =
            createTestBuildDefinition( "arguments1", "buildFile1", "goals1", profile1, schedule2 );
        BuildDefinition testGroupBuildDefinition2 =
            createTestBuildDefinition( "arguments2", "buildFile2", "goals2", profile1, schedule1 );
        BuildDefinition testGroupBuildDefinition3 =
            createTestBuildDefinition( "arguments3", "buildFile3", "goals3", profile2, schedule1 );

        BuildDefinition testBuildDefinition1 =
            createTestBuildDefinition( "arguments11", "buildFile11", "goals11", profile2, schedule1 );
        BuildDefinition testBuildDefinition2 =
            createTestBuildDefinition( "arguments12", "buildFile12", "goals12", profile2, schedule2 );
        BuildDefinition testBuildDefinition3 =
            createTestBuildDefinition( "arguments13", "buildFile13", "goals13", profile1, schedule2 );

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

        if ( addToStore )
        {
            store.addProjectGroup( group );
            defaultProjectGroup.setId( group.getId() );
            testProject1.setId( project1.getId() );
            testProject2.setId( project2.getId() );
            testBuildResult1.setId( buildResult1.getId() );
            testBuildResult2.setId( buildResult2.getId() );
            testBuildResult3.setId( buildResult3.getId() );
        }
        else
        {
            // from expected.xml, continuum-data-management
            defaultProjectGroup.setId( 1 );
            testProject1.setId( 1 );
            testProject2.setId( 2 );
        }

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

        if ( addToStore )
        {
            store.addProjectGroup( group );
            testProjectGroup2.setId( group.getId() );
        }
        else
        {
            testProjectGroup2.setId( 2 ); // from expected.xml, continuum-data-management
        }

        systemConfiguration = new SystemConfiguration();
        systemConfiguration.setBaseUrl( "baseUrl" );
        systemConfiguration.setBuildOutputDirectory( "buildOutputDirectory" );
        systemConfiguration.setDefaultScheduleCronExpression( "* * * * *" );
        systemConfiguration.setDefaultScheduleDescription( "Description" );
        systemConfiguration.setDeploymentRepositoryDirectory( "deployment" );
        systemConfiguration.setGuestAccountEnabled( false );
        systemConfiguration.setInitialized( true );
        systemConfiguration.setWorkingDirectory( "workingDirectory" );

        if ( addToStore )
        {
            systemConfiguration = store.addSystemConfiguration( systemConfiguration );
        }
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        store.closeStore();
    }

    protected void assertBuildDatabase()
        throws ContinuumStoreException
    {
        assertProjectGroupEquals( defaultProjectGroup, store.getProjectGroup( defaultProjectGroup.getId() ) );
        assertProjectGroupEquals( testProjectGroup2, store.getProjectGroup( testProjectGroup2.getId() ) );

        assertProjectEquals( testProject1, store.getProject( testProject1.getId() ) );
        assertProjectEquals( testProject2, store.getProject( testProject2.getId() ) );

        assertScheduleEquals( testSchedule1, store.getSchedule( testSchedule1.getId() ) );
        assertScheduleEquals( testSchedule2, store.getSchedule( testSchedule2.getId() ) );
        assertScheduleEquals( testSchedule3, store.getSchedule( testSchedule3.getId() ) );

        Iterator iterator = store.getAllInstallations().iterator();
        assertInstallationEquals( testInstallationJava13, (Installation) iterator.next() );
        assertInstallationEquals( testInstallationJava14, (Installation) iterator.next() );
        assertInstallationEquals( testInstallationMaven20a3, (Installation) iterator.next() );

/*
        // TODO!!! -- definitely need to test the changeset stuff since it uses modello.refid
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

        BuildDefinition testGroupBuildDefinition1 =
            createTestBuildDefinition( "arguments1", "buildFile1", "goals1", profile1, schedule2 );
        BuildDefinition testGroupBuildDefinition2 =
            createTestBuildDefinition( "arguments2", "buildFile2", "goals2", profile1, schedule1 );
        BuildDefinition testGroupBuildDefinition3 =
            createTestBuildDefinition( "arguments3", "buildFile3", "goals3", profile2, schedule1 );

        BuildDefinition testBuildDefinition1 =
            createTestBuildDefinition( "arguments11", "buildFile11", "goals11", profile2, schedule1 );
        BuildDefinition testBuildDefinition2 =
            createTestBuildDefinition( "arguments12", "buildFile12", "goals12", profile2, schedule2 );
        BuildDefinition testBuildDefinition3 =
            createTestBuildDefinition( "arguments13", "buildFile13", "goals13", profile1, schedule2 );

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
*/

        assertSystemConfiguration( systemConfiguration, store.getSystemConfiguration() );
    }

    private void assertSystemConfiguration( SystemConfiguration expected, SystemConfiguration actual )
    {
        assertNotSame( expected, actual );
        assertEquals( expected.getBaseUrl(), actual.getBaseUrl() );
        assertEquals( expected.getBuildOutputDirectory(), actual.getBuildOutputDirectory() );
        assertEquals( expected.getDefaultScheduleCronExpression(), actual.getDefaultScheduleCronExpression() );
        assertEquals( expected.getDefaultScheduleDescription(), actual.getDefaultScheduleDescription() );
        assertEquals( expected.getDeploymentRepositoryDirectory(), actual.getDeploymentRepositoryDirectory() );
        assertEquals( expected.isGuestAccountEnabled(), actual.isGuestAccountEnabled() );
        assertEquals( expected.isInitialized(), actual.isInitialized() );
        assertEquals( expected.getWorkingDirectory(), actual.getWorkingDirectory() );
    }

    protected void assertEmpty()
        throws ContinuumStoreException
    {
        assertEquals( 0, store.getAllInstallations().size() );
        assertEquals( 0, store.getAllProfilesByName().size() );
        assertEquals( 0, store.getAllProjectGroups().size() );
        assertEquals( 0, store.getAllProjectsByName().size() );
        assertNull( store.getSystemConfiguration() );
    }

    protected static BuildDefinition createTestBuildDefinition( BuildDefinition buildDefinition )
    {
        return createTestBuildDefinition( buildDefinition.getArguments(), buildDefinition.getBuildFile(),
                                          buildDefinition.getGoals(), buildDefinition.getProfile(),
                                          buildDefinition.getSchedule() );
    }

    protected static BuildDefinition createTestBuildDefinition( String arguments, String buildFile, String goals,
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

    protected static ProjectNotifier createTestNotifier( ProjectNotifier notifier )
    {
        return createTestNotifier( notifier.getRecipientType(), notifier.isSendOnError(), notifier.isSendOnFailure(),
                                   notifier.isSendOnSuccess(), notifier.getType() );
    }

    protected static ProjectNotifier createTestNotifier( int recipientType, boolean sendOnError, boolean sendOnFailure,
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

    protected static Installation createTestInstallation( Installation installation )
    {
        return createTestInstallation( installation.getName(), installation.getPath(), installation.getVersion() );
    }

    protected static Schedule createTestSchedule( Schedule schedule )
    {
        return createTestSchedule( schedule.getName(), schedule.getDescription(), schedule.getDelay(),
                                   schedule.getCronExpression(), schedule.isActive() );
    }

    protected static Schedule createTestSchedule( String name, String description, int delay, String cronExpression,
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

    protected static Profile createTestProfile( Profile profile )
    {
        return createTestProfile( profile.getName(), profile.getDescription(), profile.getScmMode(),
                                  profile.isBuildWithoutChanges(), profile.isActive(), profile.getJdk(),
                                  profile.getBuilder() );
//                                  createTestInstallation( profile.getJdk() ),
//                                  createTestInstallation( profile.getBuilder() ) );
    }

    protected static Profile createTestProfile( String name, String description, int scmMode,
                                                boolean buildWithoutChanges, boolean active, Installation jdk,
                                                Installation builder )
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

    protected static ProjectGroup createTestProjectGroup( ProjectGroup group )
    {
        return createTestProjectGroup( group.getName(), group.getDescription(), group.getGroupId() );
    }

    protected static ProjectGroup createTestProjectGroup( String name, String description, String groupId )
    {
        ProjectGroup group = new ProjectGroup();
        group.setName( name );
        group.setDescription( description );
        group.setGroupId( groupId );
        return group;
    }

    protected static Project createTestProject( Project project )
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

    protected static void assertProjectEquals( Project expectedProject, Project project )
    {
        assertEquals( "compare projects", expectedProject, project );
        assertNotSame( expectedProject, project );
        // aggressive compare, as equals is using the identity
        assertEquals( "compare expectedProject - name", expectedProject.getName(), project.getName() );
        assertEquals( "compare expectedProject - desc", expectedProject.getDescription(), project.getDescription() );
        assertEquals( "compare expectedProject - groupId", expectedProject.getGroupId(), project.getGroupId() );
        assertEquals( "compare expectedProject - artifactId", expectedProject.getArtifactId(),
                      project.getArtifactId() );
        assertEquals( "compare expectedProject - buildNumber", expectedProject.getBuildNumber(),
                      project.getBuildNumber() );
        assertEquals( "compare expectedProject - scmUrl", expectedProject.getScmUrl(), project.getScmUrl() );
        assertEquals( "compare expectedProject - state", expectedProject.getState(), project.getState() );
        assertEquals( "compare expectedProject - url", expectedProject.getUrl(), project.getUrl() );
        assertEquals( "compare expectedProject - version", expectedProject.getVersion(), project.getVersion() );
        assertEquals( "compare expectedProject - workingDirectory", expectedProject.getWorkingDirectory(),
                      project.getWorkingDirectory() );
    }

    protected static void assertProjectGroupEquals( ProjectGroup expectedGroup, ProjectGroup actualGroup )
    {
        assertEquals( "compare project groups", expectedGroup, actualGroup );
        assertNotSame( expectedGroup, actualGroup );
        // aggressive compare, as equals is using the identity
        assertEquals( "compare project groups - name", expectedGroup.getName(), actualGroup.getName() );
        assertEquals( "compare project groups - desc", expectedGroup.getDescription(), actualGroup.getDescription() );
        assertEquals( "compare project groups - groupId", expectedGroup.getGroupId(), actualGroup.getGroupId() );
    }

    protected static void assertScheduleEquals( Schedule expectedSchedule, Schedule actualSchedule )
    {
        assertNotSame( expectedSchedule, actualSchedule );
        assertEquals( "compare schedule - id", expectedSchedule.getId(), actualSchedule.getId() );
        assertEquals( "compare schedule - name", expectedSchedule.getName(), actualSchedule.getName() );
        assertEquals( "compare schedule - desc", expectedSchedule.getDescription(), actualSchedule.getDescription() );
        assertEquals( "compare schedule - delay", expectedSchedule.getDelay(), actualSchedule.getDelay() );
        assertEquals( "compare schedule - cron", expectedSchedule.getCronExpression(),
                      actualSchedule.getCronExpression() );
        assertEquals( "compare schedule - active", expectedSchedule.isActive(), actualSchedule.isActive() );
    }

    protected static void assertProfileEquals( Profile expectedProfile, Profile actualProfile )
    {
        assertNotSame( expectedProfile, actualProfile );
        assertEquals( "compare profile - name", expectedProfile.getName(), actualProfile.getName() );
        assertEquals( "compare profile - desc", expectedProfile.getDescription(), actualProfile.getDescription() );
        assertEquals( "compare profile - scmMode", expectedProfile.getScmMode(), actualProfile.getScmMode() );
        assertEquals( "compare profile - build w/o changes", expectedProfile.isBuildWithoutChanges(),
                      actualProfile.isBuildWithoutChanges() );
        assertEquals( "compare profile - active", expectedProfile.isActive(), actualProfile.isActive() );
    }

    protected static void assertInstallationEquals( Installation expected, Installation actual )
    {
        assertEquals( "compare installation - name", expected.getName(), actual.getName() );
        assertEquals( "compare installation - path", expected.getPath(), actual.getPath() );
        assertEquals( "compare installation - version", expected.getVersion(), actual.getVersion() );
    }

    protected static void assertBuildResultEquals( BuildResult expected, BuildResult actual )
    {
        assertEquals( "compare build result - build #", expected.getBuildNumber(), actual.getBuildNumber() );
        assertEquals( "compare build result - end time", expected.getEndTime(), actual.getEndTime() );
        assertEquals( "compare build result - error", expected.getError(), actual.getError() );
        assertEquals( "compare build result - exit code", expected.getExitCode(), actual.getExitCode() );
        assertEquals( "compare build result - start time", expected.getStartTime(), actual.getStartTime() );
        assertEquals( "compare build result - state", expected.getState(), actual.getState() );
        assertEquals( "compare build result - trigger", expected.getTrigger(), actual.getTrigger() );
    }

    protected static void assertScmResultEquals( ScmResult expected, ScmResult actual )
    {
        assertEquals( "compare SCM result - output", expected.getCommandOutput(), actual.getCommandOutput() );
        assertEquals( "compare SCM result - message", expected.getProviderMessage(), actual.getProviderMessage() );
        assertEquals( "compare SCM result - success", expected.isSuccess(), actual.isSuccess() );
        assertEquals( "compare SCM result - changes size", actual.getChanges().size(), expected.getChanges().size() );
        for ( int i = 0; i < actual.getChanges().size(); i++ )
        {
            assertChangeSetEquals( (ChangeSet) expected.getChanges().get( i ),
                                   (ChangeSet) actual.getChanges().get( i ) );
        }
    }

    private static void assertChangeSetEquals( ChangeSet expected, ChangeSet actual )
    {
        assertEquals( "compare change set result - author", expected.getAuthor(), actual.getAuthor() );
        assertEquals( "compare change set result - comment", expected.getComment(), actual.getComment() );
        //Remove this test, in some case we have a 1ms difference between two dates
        //assertEquals( "compare change set result - date", changeSet.getDate(), retrievedChangeSet.getDate() );
        assertEquals( "compare change set result - files size", expected.getFiles().size(), actual.getFiles().size() );
        for ( int i = 0; i < actual.getFiles().size(); i++ )
        {
            assertChangeFileEquals( (ChangeFile) expected.getFiles().get( i ),
                                    (ChangeFile) actual.getFiles().get( i ) );
        }
    }

    private static void assertChangeFileEquals( ChangeFile expected, ChangeFile actual )
    {
        assertEquals( "compare change file result - name", expected.getName(), actual.getName() );
        assertEquals( "compare change file result - revision", expected.getRevision(), actual.getRevision() );
    }

    protected static void assertNotifiersEqual( List expected, List actual )
    {
        for ( int i = 0; i < actual.size(); i++ )
        {
            assertNotifierEquals( (ProjectNotifier) expected.get( i ), (ProjectNotifier) actual.get( i ) );
        }
    }

    protected static void assertNotifierEquals( ProjectNotifier expected, ProjectNotifier actual )
    {
        assertEquals( "compare notifier - recipient type", expected.getRecipientType(), actual.getRecipientType() );
        assertEquals( "compare notifier - type", expected.getType(), actual.getType() );
        assertEquals( "compare notifier - configuration", expected.getConfiguration(), actual.getConfiguration() );
        assertEquals( "compare notifier - send on success", expected.isSendOnSuccess(), actual.isSendOnSuccess() );
        assertEquals( "compare notifier - send on failure", expected.isSendOnFailure(), actual.isSendOnFailure() );
        assertEquals( "compare notifier - send on error", expected.isSendOnError(), actual.isSendOnError() );
    }

    protected static void assertBuildDefinitionsEqual( List expectedBuildDefinitions, List actualBuildDefinitions )
    {
        for ( int i = 0; i < expectedBuildDefinitions.size(); i++ )
        {
            BuildDefinition expectedBuildDefinition = (BuildDefinition) expectedBuildDefinitions.get( i );
            BuildDefinition actualBuildDefinition = (BuildDefinition) actualBuildDefinitions.get( i );
            assertBuildDefinitionEquals( expectedBuildDefinition, actualBuildDefinition );
            assertScheduleEquals( expectedBuildDefinition.getSchedule(), actualBuildDefinition.getSchedule() );
            assertProfileEquals( expectedBuildDefinition.getProfile(), actualBuildDefinition.getProfile() );
        }
    }

    protected static void assertBuildDefinitionEquals( BuildDefinition expectedBuildDefinition,
                                                       BuildDefinition actualBuildDefinition )
    {
        assertEquals( "compare build definition - arguments", expectedBuildDefinition.getArguments(),
                      actualBuildDefinition.getArguments() );
        assertEquals( "compare build definition - build file", expectedBuildDefinition.getBuildFile(),
                      actualBuildDefinition.getBuildFile() );
        assertEquals( "compare build definition - goals", expectedBuildDefinition.getGoals(),
                      actualBuildDefinition.getGoals() );
    }

    protected static void assertDevelopersEqual( List expectedDevelopers, List actualDevelopers )
    {
        for ( int i = 0; i < actualDevelopers.size(); i++ )
        {
            assertDeveloperEquals( (ProjectDeveloper) expectedDevelopers.get( i ),
                                   (ProjectDeveloper) actualDevelopers.get( i ) );
        }
    }

    protected static void assertDeveloperEquals( ProjectDeveloper expectedDeveloper, ProjectDeveloper actualDeveloper )
    {
        assertEquals( "compare developer - name", expectedDeveloper.getName(), actualDeveloper.getName() );
        assertEquals( "compare developer - email", expectedDeveloper.getEmail(), actualDeveloper.getEmail() );
        assertEquals( "compare developer - scmId", expectedDeveloper.getScmId(), actualDeveloper.getScmId() );
        assertEquals( "compare developer - continuumId", expectedDeveloper.getContinuumId(),
                      actualDeveloper.getContinuumId() );
    }

    protected static void assertDependenciesEqual( List expectedDependencies, List actualDependencies )
    {
        for ( int i = 0; i < actualDependencies.size(); i++ )
        {
            assertDependencyEquals( (ProjectDependency) expectedDependencies.get( i ),
                                    (ProjectDependency) actualDependencies.get( i ) );
        }
    }

    protected static void assertDependencyEquals( ProjectDependency expectedDependency,
                                                  ProjectDependency actualDependency )
    {
        assertEquals( "compare dependency - groupId", expectedDependency.getGroupId(), actualDependency.getGroupId() );
        assertEquals( "compare dependency - artifactId", expectedDependency.getArtifactId(),
                      actualDependency.getArtifactId() );
        assertEquals( "compare dependency - version", expectedDependency.getVersion(), actualDependency.getVersion() );
    }

    protected static ProjectDependency createTestDependency( ProjectDependency dependency )
    {
        return createTestDependency( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }

    protected static ProjectDeveloper createTestDeveloper( ProjectDeveloper developer )
    {
        return createTestDeveloper( developer.getContinuumId(), developer.getEmail(), developer.getName(),
                                    developer.getScmId() );
    }

    protected static ProjectDependency createTestDependency( String groupId, String artifactId, String version )
    {
        ProjectDependency dependency = new ProjectDependency();
        dependency.setArtifactId( artifactId );
        dependency.setGroupId( groupId );
        dependency.setVersion( version );
        return dependency;
    }

    protected static ProjectDeveloper createTestDeveloper( int continuumId, String email, String name, String scmId )
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
    protected ContinuumStore createStore()
        throws Exception
    {
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:" + getName() );

        return (ContinuumStore) lookup( ContinuumStore.ROLE );
    }
}
