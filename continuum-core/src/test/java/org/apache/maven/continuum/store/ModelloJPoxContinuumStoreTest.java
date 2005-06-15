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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.continuum.execution.shell.ShellBuildResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ant.AntBuildResult;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.UpdateScmResult;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloJPoxContinuumStoreTest
    extends PlexusTestCase
{
    private ContinuumStore store;

    private JdoFactory jdoFactory;

    public void setUp()
        throws Exception
    {
        super.setUp();

        jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

        store = (ContinuumStore) lookup( ContinuumStore.ROLE );
    }

    public void testTransactionHandling()
        throws Exception
    {
        ContinuumJPoxStore store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNull( store.getThreadState() );

        store.begin();

        assertNotNull( store.getThreadState() );

        store.commit();

        assertNull( store.getThreadState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 1, store.getThreadState().getDepth() );

        store.commit();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.commit();

        assertNull( store.getThreadState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 1, store.getThreadState().getDepth() );

        store.commit();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.rollback();

        assertNull( store.getThreadState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 1, store.getThreadState().getDepth() );

        store.begin();

        assertEquals( 2, store.getThreadState().getDepth() );

        store.rollback();

        assertNull( store.getThreadState() );

        store.begin();

        assertEquals( 0, store.getThreadState().getDepth() );

        store.commit();

        assertNull( store.getThreadState() );
    }

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    public void testStoreProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        assertEquals( ModelloJPoxContinuumStore.class, store.getClass() );

        Properties configuration = new Properties();

        configuration.setProperty( "foo", "bar" );

        ContinuumProject expected = makeMavenTwoProject( "Test Project",
                                                         "scm:local:src/test/repo",
                                                         "foo@bar.com",
                                                         "1.0",
                                                         "a b",
                                                         "maven2",
                                                         "/tmp" );

        String projectId = store.addProject( makeMavenTwoProject( "Test Project",
                                                                  "scm:local:src/test/repo",
                                                                  "foo@bar.com",
                                                                  "1.0",
                                                                  "a b",
                                                                  "maven2",
                                                                  "/tmp" ) );

        assertNotNull( "The project id is null.", projectId );

        ContinuumProject actual = store.getProject( projectId );

        assertProjectEquals( projectId, expected, actual );
    }


    public void testGetNonExistingProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        try
        {
            store.getProject( "foo" );

            fail( "Expected ContinuumStoreException.") ;
        }
        catch( ContinuumStoreException ex )
        {
            // expected
        }
    }

    public void testProjectCRUD()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String name = "Test Project 2";
        String scmUrl = "scm:local:jalla";
        String nagEmailAddress = "foo@bar.com";
        String version = "1.0";
        String commandLineArguments = "";
        String builderId = "maven2";
        String workingDirectory = "/tmp";

        String projectId = store.addProject( makeMavenTwoProject( name,
                                                                  scmUrl,
                                                                  nagEmailAddress,
                                                                  version,
                                                                  commandLineArguments,
                                                                  builderId,
                                                                  workingDirectory ) );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumProject project = store.getProject( projectId );

        assertNotNull( project );

        assertEquals( ContinuumProjectState.CHECKING_OUT, project.getState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        CheckOutScmResult checkOutScmResult = new CheckOutScmResult();

        checkOutScmResult.setSuccess( true );

        store.setCheckoutDone( projectId, checkOutScmResult, null, null );

        project = store.getProject( projectId );

        assertNotNull( project );

        assertEquals( ContinuumProjectState.NEW, project.getState() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String name2 = "name 2";
        String scmUrl2 = "scm url 2";
        String emailAddress2 = "2@bar";
        String version2 = "v2";
        String commandLineArguments2 = "";

        store.updateProject( projectId,
                             name2,
                             scmUrl2,
                             createNotifiers( emailAddress2 ),
                             version2,
                             commandLineArguments2 );

        project = store.getProject( projectId );

        assertProjectEquals( projectId,
                             name2,
                             scmUrl2,
                             createNotifiers( emailAddress2 ),
                             version2,
                             commandLineArguments2,
                             builderId,
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
        catch ( ContinuumStoreException ex )
        {
            // expected
        }
    }

    public void testGetAllProjects()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String name1 = "Test Project 1";
        String scmUrl1 = "scm:local:src/test/repo/foo";
        String nagEmailAddress1 = "foo@bar.com";
        String version1 = "1.0";
        String commandLineArguments1 = "";
        String builderId1 = "maven2";
        String workingDirectory1 = "/tmp";

        String id1 = addMavenTwoProject( store,
                                 name1,
                                 scmUrl1,
                                 nagEmailAddress1,
                                 version1,
                                 commandLineArguments1,
                                 builderId1,
                                 workingDirectory1 );

        String name2 = "Test Project 2";
        String scmUrl2 = "scm:local:src/test/repo/bar";
        String nagEmailAddress2 = "foo@bar.com";
        String version2 = "1.0";
        String commandLineArguments2 = "";
        String builderId2 = "maven2";
        String workingDirectory2 = "/tmp";

        String id2 = addMavenTwoProject( store,
                                 name2,
                                 scmUrl2,
                                 nagEmailAddress2,
                                 version2,
                                 commandLineArguments2,
                                 builderId2,
                                 workingDirectory2 );

        Map projects = new HashMap();

        for ( Iterator it = store.getAllProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            assertNotNull( "While getting all projects: project.id == null", project.getId() );

            assertNotNull( "While getting all projects: project.name == null", project.getName() );

            projects.put( project.getName(), project );
        }

        ContinuumProject project1 = (ContinuumProject) projects.get( name1 );

        assertProjectEquals( id1,
                             name1,
                             scmUrl1,
                             nagEmailAddress1,
                             version1,
                             commandLineArguments1,
                             builderId1,
                             workingDirectory1,
                             project1 );

        ContinuumProject project2 = (ContinuumProject) projects.get( name2 );

        assertProjectEquals( id2,
                             name2,
                             scmUrl2,
                             nagEmailAddress2,
                             version2,
                             commandLineArguments2,
                             builderId2,
                             workingDirectory2,
                             project2 );
    }

    public void testRemoveProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String projectId = addMavenTwoProject( "Remove Test Project", "scm:remove-project" );

        store.setIsUpdating( projectId );

        store.setUpdateDone( projectId );

        String buildId = store.createBuild( projectId, false );

        UpdateScmResult scmResult = new UpdateScmResult();

        ScmFile file = new ScmFile();

        file.setPath( "foo" );

        scmResult.addUpdatedFile( file );

        ContinuumBuildResult result = new ShellBuildResult();

        store.setBuildResult( buildId, ContinuumProjectState.OK, result, scmResult, null );

        store.removeProject( projectId );
    }

    // ----------------------------------------------------------------------
    // Maven Two project tests
    // ----------------------------------------------------------------------

    public void testUpdateMavenTwoProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String projectId = addMavenTwoProject( "Maven Two Project", "scm:foo" );

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
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        lookup( JdoFactory.ROLE );

        String projectId = addMavenTwoProject( "Build Test Project", "scm:build" );

        store.setIsUpdating( projectId );

        store.setUpdateDone( projectId );

        // ----------------------------------------------------------------------
        // Construct a build object
        // ----------------------------------------------------------------------

        String buildId = store.createBuild( projectId, false );

        UpdateScmResult updateScmResult = new UpdateScmResult();

        updateScmResult.setCommandOutput( "commandOutput" );

        updateScmResult.setProviderMessage( "providerMessage" );

        updateScmResult.setSuccess( true );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        updateScmResult.getUpdatedFiles().add( scmFile );

        AntBuildResult buildResult = new AntBuildResult();

        buildResult.setExitCode( 10 );

        buildResult.setStandardError( "stderr" );

        buildResult.setStandardOutput( "stdout" );

        store.setBuildComplete( buildId, updateScmResult, buildResult );

        // ----------------------------------------------------------------------
        // Store and check the build object
        // ----------------------------------------------------------------------

        Collection builds = store.getBuildsForProject( projectId, 0, 0 );

        assertNotNull( "The collection with all builds was null.", builds );

        assertEquals( "Expected the build set to contain a single build.", 1, builds.size() );

        ContinuumBuild build = (ContinuumBuild) builds.iterator().next();

        assertNotNull( build );

        assertEquals( "build.id", buildId, build.getId() );
    }

    public void testTheAssociationBetweenTheProjectAndItsBuilds()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        // Set up projects
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Association Test Project", "scm:association" );

        store.setIsUpdating( projectId );

        store.setUpdateDone( projectId );

        String projectIdFoo = addMavenTwoProject( "Foo Project", "scm:association-foo" );

        store.setIsUpdating( projectIdFoo );

        store.setUpdateDone( projectIdFoo );

        String projectIdBar = addMavenTwoProject( "Bar Project", "scm:association-bar" );

        store.setIsUpdating( projectIdBar );

        store.setUpdateDone( projectIdBar );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( 0, store.createBuild( projectId, false ) );

            store.createBuild( projectIdFoo, false );

            store.createBuild( projectIdBar, false );

            store.createBuild( projectIdFoo, false );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( 9 ) );

        Collection actualBuilds = store.getBuildsForProject( projectId, 0, 0 );

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
        String projectId = addMavenTwoProject( "Association Test Project", "scm:association" );

        store.setIsUpdating( projectId );

        store.setUpdateDone( projectId );

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( 0, store.createBuild( projectId, false ) );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( 9 ) );

        Collection actualBuilds = store.getBuildsForProject( projectId, 0, 0 );

        assertEquals( 10, actualBuilds.size() );
    }

    public void testBuildResult()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Build Result Project", "scm:build/result" );

        store.setIsUpdating( projectId );

        assertInState( projectId, ContinuumProjectState.UPDATING );

        store.setUpdateDone( projectId );

        assertInState( projectId, ContinuumProjectState.BUILDING );

        long now = System.currentTimeMillis();

        String buildId = store.createBuild( projectId, false );

        assertInState( projectId, ContinuumProjectState.BUILDING );

        assertIsCommitted( store );

        assertNotNull( buildId );

        // ----------------------------------------------------------------------
        // Check that the project's state has been updated
        // ----------------------------------------------------------------------

        ContinuumProject project = store.getProject( projectId );

        assertIsCommitted( store );

        assertEquals( ContinuumProjectState.BUILDING, project.getState() );

        // ----------------------------------------------------------------------
        // Check the build
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getBuild( buildId );

        assertIsCommitted( store );

        assertNotNull( build );

        assertEquals( now / 10000, build.getStartTime() / 10000 );

        assertEquals( 0, build.getEndTime() );

        assertNull( build.getError() );

        assertEquals( ContinuumProjectState.BUILDING, build.getState() );

        // ----------------------------------------------------------------------
        // Check the build result
        // ----------------------------------------------------------------------

        ContinuumBuildResult result = store.getBuildResultForBuild( buildId );

        assertIsCommitted( store );

        assertNull( result );

        UpdateScmResult scmResult = new UpdateScmResult();

        ShellBuildResult shellBuildResult = new ShellBuildResult();

        shellBuildResult.setExitCode( 1 );

        shellBuildResult.setStandardOutput( "output" );

        shellBuildResult.setStandardError( "error" );

        store.setBuildResult( buildId, ContinuumProjectState.OK, shellBuildResult, scmResult, null );

        assertIsCommitted( store );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        shellBuildResult = (ShellBuildResult) store.getBuildResultForBuild( buildId );

        assertIsCommitted( store );

        assertEquals( 1, shellBuildResult.getExitCode() );

        assertEquals( "output", shellBuildResult.getStandardOutput() );

        assertEquals( "error", shellBuildResult.getStandardError() );
    }

    // ----------------------------------------------------------------------
    // Private utility methods
    // ----------------------------------------------------------------------

    private String addMavenTwoProject( String name, String scmUrl )
        throws Exception
    {
        return addMavenTwoProject( (ContinuumStore) lookup( ContinuumStore.ROLE ),
                           makeStubMavenTwoProject( name, scmUrl ) );
    }

    // ----------------------------------------------------------------------
    // Public utility methods
    // ----------------------------------------------------------------------

    public static MavenTwoProject makeStubMavenTwoProject( String name, String scmUrl )
    {
        return makeMavenTwoProject( name,
                                    scmUrl,
                                    "foo@bar.com",
                                    "1.0",
                                    "",
                                    ContinuumBuildExecutor.MAVEN_TWO_EXECUTOR_ID,
                                    "/tmp" );
    }

    public static MavenTwoProject makeMavenTwoProject( String name,
                                                       String scmUrl,
                                                       String emailAddress,
                                                       String version,
                                                       String commandLineArguments,
                                                       String executorId,
                                                       String workingDirectory )
    {
        MavenTwoProject project = new MavenTwoProject();

        project.setName( name );
        project.setScmUrl( scmUrl );

        List notifiers = createNotifiers( emailAddress );
        project.setNotifiers( notifiers );

        project.setVersion( version );
        project.setCommandLineArguments( commandLineArguments );
        project.setExecutorId( executorId );
        project.setWorkingDirectory( workingDirectory );

        return project;
    }

    private static List createNotifiers( String emailAddress )
    {
        ContinuumNotifier notifier = new ContinuumNotifier();

        Properties props = new Properties();

        props.put( "address", emailAddress );

        notifier.setConfiguration( props );

        List notifiers = new ArrayList();

        notifiers.add( notifier );

        return notifiers;
    }

    public static String addMavenTwoProject( ContinuumStore store, ContinuumProject project )
        throws Exception
    {
        String projectId = store.addProject( project );

        CheckOutScmResult checkOutScmResult = new CheckOutScmResult();

        checkOutScmResult.setSuccess( true );

        checkOutScmResult.setCommandOutput( "commandOutput" );

        checkOutScmResult.setProviderMessage( "providerMessage" );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        checkOutScmResult.addCheckedOutFile( scmFile );

        store.setCheckoutDone( projectId, checkOutScmResult, null, null );

        project = store.getProject( projectId );

        assertNotNull( project );

        assertEquals( ContinuumProjectState.NEW, project.getState() );

        return projectId;
    }

    public static String addMavenTwoProject( ContinuumStore store, String name, String scmUrl )
        throws Exception
    {
        return addMavenTwoProject( store, makeStubMavenTwoProject( name, scmUrl ) );
    }

    public static String addMavenTwoProject( ContinuumStore store,
                                     String name,
                                     String scmUrl,
                                     String nagEmailAddress,
                                     String version,
                                     String commandLineArguments,
                                     String executorId,
                                     String workingDirectory  )
        throws Exception
    {
        String projectId = store.addProject( makeMavenTwoProject( name,
                                                                  scmUrl,
                                                                  nagEmailAddress,
                                                                  version,
                                                                  commandLineArguments,
                                                                  executorId,
                                                                  workingDirectory ) );

        CheckOutScmResult checkOutScmResult = new CheckOutScmResult();

        checkOutScmResult.setSuccess( true );

        store.setCheckoutDone( projectId, checkOutScmResult, null, null );

        ContinuumProject project = store.getProject( projectId );

        assertNotNull( project );

        assertEquals( ContinuumProjectState.NEW, project.getState() );

        return projectId;
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    private void assertInState( String projectId, int state )
        throws ContinuumStoreException
    {
        ContinuumProject project = store.getProject( projectId );

        assertEquals( state, project.getState() );
    }

    private void assertIsCommitted( ContinuumStore store )
    {
        ContinuumJPoxStore.ThreadState state = ( (ModelloJPoxContinuumStore) store ).getStore().getThreadState();

        assertNull( state );
    }

    private void assertProjectEquals( String projectId,
                                      ContinuumProject expected,
                                      ContinuumProject actual )
    {
        assertProjectEquals( projectId,
                             expected.getName(),
                             expected.getScmUrl(),
                             expected.getNotifiers(),
                             expected.getVersion(),
                             expected.getCommandLineArguments(),
                             expected.getExecutorId(),
                             expected.getWorkingDirectory(),
                             actual );
    }

    private void assertProjectEquals( String projectId,
                                      String name,
                                      String scmUrl,
                                      String emailAddress,
                                      String version,
                                      String commandLineArguments,
                                      String builderId,
                                      String workingDirectory,
                                      ContinuumProject actual )
    {
        assertProjectEquals( projectId, name, scmUrl, createNotifiers( emailAddress), version, commandLineArguments,
                             builderId, workingDirectory, actual );
    }
    private void assertProjectEquals( String projectId,
                                      String name,
                                      String scmUrl,
                                      List notifiers,
                                      String version,
                                      String commandLineArguments,
                                      String builderId,
                                      String workingDirectory,
                                      ContinuumProject actual )
    {
        assertEquals( "project.id", projectId, actual.getId() );

        assertEquals( "project.name", name, actual.getName() );

        assertEquals( "project.scmUrl", scmUrl, actual.getScmUrl() );

        assertNotNull( notifiers );

        assertEquals( "project.notifiers", notifiers.size(), actual.getNotifiers().size() );

        for ( int i = 0; i < notifiers.size(); i++ )
        {
            ContinuumNotifier notifier = (ContinuumNotifier) notifiers.get( i );

            ContinuumNotifier actualNotifier = (ContinuumNotifier) actual.getNotifiers().get( i );

            assertEquals( "project.notifiers.notifier.type", notifier.getType(), actualNotifier.getType() );

            assertEquals( "project.notifiers.notifier.configuration.address",
                          notifier.getConfiguration().get( "address" ),
                          actualNotifier.getConfiguration().get( "address" ) );
        }

        assertEquals( "project.version", version, actual.getVersion() );

        assertEquals( "project.commandLineArguments", commandLineArguments, actual.getCommandLineArguments() );

        assertEquals( "project.executorId", builderId, actual.getExecutorId() );

        assertEquals( "project.workingDirectory", workingDirectory, actual.getWorkingDirectory() );
    }
}
