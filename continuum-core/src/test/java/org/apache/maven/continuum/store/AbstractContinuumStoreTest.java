package org.apache.maven.continuum.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.scheduler.ContinuumSchedulerConstants;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.ScmResult;

import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AbstractContinuumStoreTest
    extends AbstractContinuumTest
{
    private ContinuumStore store;

    private String roleHint;

    private Class implementationClass;

    public AbstractContinuumStoreTest( String roleHint, Class implementationClass )
    {
        this.roleHint = roleHint;

        this.implementationClass = implementationClass;
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        store = (ContinuumStore) lookup( "ContinuumStore", roleHint );

        assertEquals( implementationClass, store.getClass() );

        for ( Iterator it = store.getAllProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            store.removeProject( project.getId() );
        }
    }

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    public void testAddProject()
        throws Exception
    {
        Properties configuration = new Properties();

        configuration.setProperty( "foo", "bar" );

        ContinuumProject actual = store.addProject( makeMavenTwoProject( "Test Project",
                                                                         "scm:local:src/test/repo",
                                                                         "foo@bar.com",
                                                                         "1.0",
                                                                         "a b",
                                                                         "/tmp" ) );

        assertNotNull( "The project id is null.", actual.getId() );

        assertProjectEquals( makeMavenTwoProject( "Test Project",
                                                  "scm:local:src/test/repo",
                                                  "foo@bar.com",
                                                  "1.0",
                                                  "a b",
                                                  "/tmp" ), actual );
    }

    public void testGetNonExistingProject()
        throws Exception
    {
        try
        {
            store.getProject( "foo" );

            fail( "Expected ContinuumObjectNotFoundException.") ;
        }
        catch( ContinuumObjectNotFoundException ex )
        {
            // expected
        }
        // TODO: Remove me when the generated stuff throws a better exception when the object is missing
        catch ( ContinuumStoreException ex )
        {
            // expected
        }
    }

    public void testProjectCRUD()
        throws Exception
    {
        String name = "Test Project 2";
        String scmUrl = "scm:local:jalla";
        String nagEmailAddress = "foo@bar.com";
        String version = "1.0";
        String commandLineArguments = "";
        String workingDirectory = "/tmp";

        ContinuumProject expected = makeMavenTwoProject( name,
                                                         scmUrl,
                                                         nagEmailAddress,
                                                         version,
                                                         commandLineArguments,
                                                         workingDirectory );

        ContinuumProject project = store.addProject( expected );

        String projectId = project.getId();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = store.getProjectByName( name );

        assertNotNull( project );

        assertEquals( projectId, project.getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = store.getProjectByScmUrl( scmUrl );

        assertNotNull( project );

        assertEquals( projectId, project.getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        project = setCheckoutDone( store, store.getProject( projectId ), scmResult, null, null );

        assertNotNull( project );

        assertNotNull( project.getScmResult() );

        assertTrue( project.getScmResult().isSuccess() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String name2 = "name 2";
        String scmUrl2 = "scm url 2";
        String emailAddress2 = "2@bar";
        String version2 = "v2";
        String commandLineArguments2 = "";

        project.setName( name2 );
        project.setScmUrl( scmUrl2 );

        assertNotNull( project.getNotifiers() );
        assertEquals( 1, project.getNotifiers().size() );
        ContinuumNotifier notifier = (ContinuumNotifier) project.getNotifiers().get( 0 );
        notifier.setType( "kewk" );
        notifier.getConfiguration().put( "address", emailAddress2 );
        notifier.getConfiguration().put( "name", "tryg" );
        project.setVersion( version2 );
        project.setCommandLineArguments( commandLineArguments2 );

        store.updateProject( project );

        project = store.getProject( projectId );

        notifier = new ContinuumNotifier();
        notifier.setType( "kewk" );
        notifier.getConfiguration().put( "address", emailAddress2 );
        notifier.getConfiguration().put( "name", "tryg" );
        List notifiers = new ArrayList();
        notifiers.add( notifier );

        assertProjectEquals( name2,
                             scmUrl2,
                             notifiers,
                             version2,
                             commandLineArguments2,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory,
                             project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store.removeProject( projectId );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        try
        {
            store.getProject( "foo" );

            fail( "Expected ContinuumStoreException." );
        }
        catch ( ContinuumObjectNotFoundException ex )
        {
            // expected
        }
        // TODO: Remove me when the generated stuff throws a better exception when the object is missing
        catch ( ContinuumStoreException ex )
        {
            // expected
        }
    }

    public void testGetAllProjects()
        throws Exception
    {
        String name1 = "Test All Projects 1";
        String scmUrl1 = "scm:local:src/test/repo/foo";
        String nagEmailAddress1 = "foo@bar.com";
        String version1 = "1.0";
        String commandLineArguments1 = "";
        String workingDirectory1 = "/tmp";

        ContinuumProject project1 = addMavenTwoProject( store,
                                                        name1,
                                                        scmUrl1,
                                                        nagEmailAddress1,
                                                        version1,
                                                        commandLineArguments1,
                                                        workingDirectory1 );

        String name2 = "Test All Projects 2";
        String scmUrl2 = "scm:local:src/test/repo/bar";
        String nagEmailAddress2 = "foo@bar.com";
        String version2 = "1.0";
        String commandLineArguments2 = "";
        String workingDirectory2 = "/tmp";

        ContinuumProject project2 = addMavenTwoProject( store,
                                                        name2,
                                                        scmUrl2,
                                                        nagEmailAddress2,
                                                        version2,
                                                        commandLineArguments2,
                                                        workingDirectory2 );

        Map projects = new HashMap();

        Collection projectsCollection = store.getAllProjects();

        assertEquals( 2, projectsCollection.size() );

        for ( Iterator it = projectsCollection.iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            assertNotNull( "While getting all projects: project.id", project.getId() );

            assertNotNull( "While getting all projects: project.name", project.getName() );

            projects.put( project.getName(), project );
        }

        assertProjectEquals( name1,
                             scmUrl1,
                             (String)null,
                             version1,
                             commandLineArguments1,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory1,
                             project1 );

        assertProjectEquals( name2,
                             scmUrl2,
                             (String)null,
                             version2,
                             commandLineArguments2,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory2,
                             project2 );
    }

    public void testRemoveProject()
        throws Exception
    {
        ContinuumProject project = addMavenTwoProject( store, "Remove Test Project", "scm:remove-project" );

        ContinuumBuild build = createBuild( store, project.getId(), false );

        ScmResult scmResult = new ScmResult();

        ScmFile file = new ScmFile();

        file.setPath( "foo" );

        scmResult.addFile( file );

        setBuildResult( store,
                        build,
                        ContinuumProjectState.OK,
                        makeContinuumBuildExecutionResult( "", 0 ),
                        scmResult,
                        null );

        store.removeProject( project.getId() );
    }

    private ContinuumBuildExecutionResult makeContinuumBuildExecutionResult( String output,
                                                                             int exitCode )
    {
        return new ContinuumBuildExecutionResult( output,
                                                  exitCode );
    }

    public void testGetLatestBuildForProject()
        throws Exception
    {
        String projectId = addMavenTwoProject( store,
                                               makeStubMavenTwoProject( "Last project", "scm:foo" ) ).getId();

        assertNull( store.getLatestBuildForProject( projectId ) );

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ).getId() );
        }

        assertEquals( expectedBuilds.get( expectedBuilds.size() - 1 ),
                      store.getLatestBuildForProject( projectId ).getId() );
    }

    // ----------------------------------------------------------------------
    // Maven Two project tests
    // ----------------------------------------------------------------------

    public void testUpdateMavenTwoProject()
        throws Exception
    {
        String projectId = addMavenTwoProject( store, "Maven Two Project", "scm:foo" ).getId();

        MavenTwoProject project = (MavenTwoProject) store.getProject( projectId );

        project.setName( "New name" );
        project.setGoals( "clean test" );

        store.updateProject( project );

        project = (MavenTwoProject) store.getProject( projectId );

        assertEquals( "New name", project.getName() );
        assertEquals( "clean test", project.getGoals() );
    }
/*
    public void testUpdateMavenTwoProjectWithANonJdoObject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        // ----------------------------------------------------------------------
        // Make a project in the store
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Maven Two Project", "scm:foo" );

        // ----------------------------------------------------------------------
        // This is a object constructed from outside Continuum, typically
        // something that comes in over the wire.
        // ----------------------------------------------------------------------

        MavenTwoProject external = makeStubMavenTwoProject( "Maven Two Project", "scm:foo" );

        external.setId( projectId );

        external.setName( "New name" );

        MavenTwoProject p = (MavenTwoProject) store.getProject( projectId );

        assertEquals( "Maven Two Project", p.getName() );

        store.updateProject( external );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MavenTwoProject actual = (MavenTwoProject) store.getProject( projectId );

        assertEquals( "New name", actual.getName() );
    }
*/
    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    public void testBuild()
        throws Exception
    {
        lookup( JdoFactory.ROLE );

        String projectId = addMavenTwoProject( store, "Build Test Project", "scm:build" ).getId();

        // ----------------------------------------------------------------------
        // Construct a build object
        // ----------------------------------------------------------------------

        ContinuumBuild build = createBuild( store, projectId, false );

        String buildId = build.getId();

        ScmResult scmResult = new ScmResult();

        scmResult.setCommandOutput( "commandOutput" );

        scmResult.setProviderMessage( "providerMessage" );

        scmResult.setSuccess( true );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        scmResult.getFiles().add( scmFile );

        setBuildComplete( store,
                          build,
                          scmResult,
                          makeContinuumBuildExecutionResult( "output", 10 ) );

        // ----------------------------------------------------------------------
        // Store and check the build object
        // ----------------------------------------------------------------------

        Collection builds = store.getBuildsForProject( projectId, 0, 0 );

        assertNotNull( "The collection with all builds was null.", builds );

        assertEquals( "Expected the build set to contain a single build.", 1, builds.size() );

        build = (ContinuumBuild) builds.iterator().next();

        assertNotNull( build );

        assertEquals( "build.id", buildId, build.getId() );
    }

    private void setBuildComplete( ContinuumStore store,
                                   ContinuumBuild build,
                                   ScmResult scmResult,
                                   ContinuumBuildExecutionResult result )
        throws ContinuumStoreException
    {
        build.setScmResult( scmResult );

        build.setExitCode( result.getExitCode() );

        store.setBuildOutput( build.getId(), "output" );

        store.updateBuild( build );
    }

    public void testTheAssociationBetweenTheProjectAndItsBuilds()
        throws Exception
    {
        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        // Set up projects
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( store, "Association Test Project", "scm:association" ).getId();

        String projectIdFoo = addMavenTwoProject( store, "Foo Project", "scm:association-foo" ).getId();

        String projectIdBar = addMavenTwoProject( store, "Bar Project", "scm:association-bar" ).getId();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ).getId() );

            createBuild( store, projectIdFoo, false );

            createBuild( store, projectIdBar, false );

            createBuild( store, projectIdFoo, false );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( expectedBuilds.size() - 1) );

        List actualBuilds = new ArrayList( store.getBuildsForProject( projectId, 0, 0 ) );

        Collections.reverse( actualBuilds );

        assertEquals( "builds.size", expectedBuilds.size(), actualBuilds.size() );

        Iterator expectedIt = expectedBuilds.iterator();

        Iterator actualIt = actualBuilds.iterator();

        for ( int i = 0; expectedIt.hasNext(); i++ )
        {
            String expectedBuildId = (String) expectedIt.next();

            String actualBuildId = ((ContinuumBuild) actualIt.next()).getId();

            assertEquals( "builds[" + i + "]", expectedBuildId, actualBuildId );
        }
    }

    public void testGetLatestBuild()
        throws Exception
    {
        String projectId = addMavenTwoProject( store, "Association Test Project", "scm:association" ).getId();

        int size = 10;

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < size; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ).getId() );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( size - 1 ) );

        Collection actualBuilds = store.getBuildsForProject( projectId, 0, 0 );

        assertEquals( build.getId(), ( (ContinuumBuild) actualBuilds.iterator().next() ).getId() );

        assertEquals( size, actualBuilds.size() );
    }

    public void testBuildResult()
        throws Exception
    {
        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( store, "Build Result Project", "scm:build/result" ).getId();

        long now = System.currentTimeMillis();

        ContinuumBuild build = createBuild( store, projectId, false );

        assertNotNull( build );

        assertNotNull( build.getId() );

        assertEquals( now / 10000, build.getStartTime() / 10000 );

        assertEquals( 0, build.getEndTime() );

        assertNull( build.getError() );

        assertEquals( ContinuumProjectState.BUILDING, build.getState() );

        // ----------------------------------------------------------------------
        // Check the build result
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        setBuildResult( store,
                        build,
                        ContinuumProjectState.OK,
                        makeContinuumBuildExecutionResult(  "output", 1 ),
                        scmResult,
                        null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        build = store.getBuild( build.getId() );

        assertEquals( 1, build.getExitCode() );

        assertEquals( "output", store.getBuildOutput( build.getId() ) );
    }

    // ----------------------------------------------------------------------
    // Notifiers
    // ----------------------------------------------------------------------

    public void testNotifiersAreBeingDetached()
        throws Exception
    {
        List notifiers = new ArrayList();

        ContinuumNotifier notifier = new ContinuumNotifier();

        notifier.setType( "foo" );

        Map configuration = new HashMap();

        configuration.put( "moo", "foo" );

        notifier.setConfiguration( configuration );

        notifiers.add( notifier );

        ContinuumProject project = new MavenTwoProject();

        project.setNotifiers( notifiers );

        project = getStore().addProject( project );

        notifiers = project.getNotifiers();

        assertNotNull( notifiers );

        notifier = (ContinuumNotifier) notifiers.get( 0 );

        assertEquals( "foo", notifier.getType() );

        configuration = notifier.getConfiguration();

        assertEquals( "foo", configuration.get( "moo" ) );
    }

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    public void testScheduleAdditionAndRemovalFromProject()
        throws Exception
    {
        // create project
        ContinuumProject project = addMavenTwoProject( store, "Project Scheduling", "scm:scheduling" );

        String projectId = project.getId();

        // add schedule
        //project.addSchedule( createStubSchedule( "schedule1" ) );

        ContinuumSchedule s = createStubSchedule( "schedule1" );

        s = store.addSchedule( s );

        project.addSchedule( s );

        // update project
        store.updateProject( project );

        assertEquals( 1, store.getSchedules().size() );

        // retrieve project
        project = store.getProject( projectId );

        assertNotNull( project );

        // get schedules out of the project
        Set schedules = project.getSchedules();

        assertEquals( 1, schedules.size() );

        // get individual schedule
        ContinuumSchedule schedule = (ContinuumSchedule) schedules.iterator().next();

        // test values within schedule
        assertEquals( "schedule1", schedule.getName() );

        assertEquals( "schedule1", schedule.getDescription() );

        assertTrue( schedule.isActive() );

        assertEquals( ContinuumSchedulerConstants.SCM_MODE_UPDATE, schedule.getScmMode() );

        assertEquals( 3600, schedule.getDelay() );

        assertEquals( "0 * * * * ?", schedule.getCronExpression() );

        // ----------------------------------------------------------------------
        // Now lookup the schedule on its own and make sure the project is
        // present within the schedule.
        // ----------------------------------------------------------------------

        String scheduleId = schedule.getId();

        schedule = store.getSchedule( scheduleId );

        assertNotNull( schedule );

        project = (ContinuumProject) schedule.getProjects().iterator().next();

        assertNotNull( project );

        assertEquals( "Project Scheduling", project.getName() );

        // ----------------------------------------------------------------------
        // Now delete the project from the store and make sure that the schedule
        // still remains in the store.
        // ----------------------------------------------------------------------

        store.removeProject( projectId );

        schedule = store.getSchedule( scheduleId );

        assertNotNull( schedule );
    }

    public void testProjectAdditionAndRemovalFromSchedule()
        throws Exception
    {
        // create schedule
        ContinuumSchedule schedule = createStubSchedule( "schedule2" );

        schedule = store.addSchedule( schedule );

        ContinuumProject project = addMavenTwoProject( store, "Project", "scm:scheduling" );

        // add project
        schedule.addProject( project );

        // update schedule
        schedule = store.updateSchedule( schedule );

        assertNotNull( schedule );

        // get projects out of the schedule
        Set projects = schedule.getProjects();

        assertEquals( 1, projects.size() );

        // get individual project
        project = (ContinuumProject) schedule.getProjects().iterator().next();

        // test values within project
        assertEquals( "Project", project.getName() );

        // ----------------------------------------------------------------------
        // Now lookup the project on its own and make sure the schedule is
        // present within the project.
        // ----------------------------------------------------------------------

        project = store.getProject( project.getId() );

        assertNotNull( project );

        schedule = (ContinuumSchedule) project.getSchedules().iterator().next();

        assertEquals( "schedule2", schedule.getName() );

        // ----------------------------------------------------------------------
        // Now delete the schedule from the store and make sure that the project
        // still remains in the store.
        // ----------------------------------------------------------------------

        schedule = store.getSchedule( schedule.getId() );

        store.removeSchedule( schedule.getId() );

        project = store.getProject( project.getId() );

        assertNotNull( project );
    }

    // ----------------------------------------------------------------------
    // name
    // description = name
    // active = true
    // scmMode = ContinuumSchedulerConstants.SCM_MODE_UPDATE
    // delay = 3600
    // cronExpression = 0 * * * * ?
    // ----------------------------------------------------------------------
    protected ContinuumSchedule createStubSchedule( String name )
    {
        ContinuumSchedule schedule = new ContinuumSchedule();

        schedule.setName( name );

        schedule.setDescription( name );

        schedule.setActive( true );

        schedule.setScmMode( ContinuumSchedulerConstants.SCM_MODE_UPDATE );

        schedule.setDelay( 3600 );

        schedule.setCronExpression( "0 * * * * ?" );

        return schedule;
    }
}
