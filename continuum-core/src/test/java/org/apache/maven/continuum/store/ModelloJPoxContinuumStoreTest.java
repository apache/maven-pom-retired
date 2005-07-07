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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.UpdateScmResult;

import org.codehaus.plexus.jdo.JdoFactory;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloJPoxContinuumStoreTest
    extends AbstractContinuumTest
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

        String projectId = store.addProject( makeMavenTwoProject( "Test Project",
                                                                  "scm:local:src/test/repo",
                                                                  "foo@bar.com",
                                                                  "1.0",
                                                                  "a b",
                                                                  "/tmp" ) );

        assertNotNull( "The project id is null.", projectId );

        ContinuumProject actual = store.getProject( projectId );

        assertProjectEquals( projectId, makeMavenTwoProject( "Test Project",
                                                             "scm:local:src/test/repo",
                                                             "foo@bar.com",
                                                             "1.0",
                                                             "a b",
                                                             "/tmp" ), actual );
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
        String workingDirectory = "/tmp";

        ContinuumProject project = makeMavenTwoProject( name,
                                                        scmUrl,
                                                        nagEmailAddress,
                                                        version,
                                                        commandLineArguments,
                                                        workingDirectory );

        String projectId = store.addProject( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = store.getProject( projectId );

        assertNotNull( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        CheckOutScmResult checkOutScmResult = new CheckOutScmResult();

        checkOutScmResult.setSuccess( true );

        setCheckoutDone( store, projectId, checkOutScmResult, null, null );

        project = store.getProject( projectId );

        assertNotNull( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = store.getProject( projectId );

        String name2 = "name 2";
        String scmUrl2 = "scm url 2";
        String emailAddress2 = "2@bar";
        String version2 = "v2";
        String commandLineArguments2 = "";

        project.setName( name2 );
        project.setScmUrl( scmUrl2 );

        ContinuumNotifier notifier = ((ContinuumNotifier) project.getNotifiers().get( 0 ));
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

        assertProjectEquals( projectId,
                             name2,
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
        String workingDirectory1 = "/tmp";

        String id1 = addMavenTwoProject( store,
                                 name1,
                                 scmUrl1,
                                 nagEmailAddress1,
                                 version1,
                                 commandLineArguments1,
                                 workingDirectory1 );

        String name2 = "Test Project 2";
        String scmUrl2 = "scm:local:src/test/repo/bar";
        String nagEmailAddress2 = "foo@bar.com";
        String version2 = "1.0";
        String commandLineArguments2 = "";
        String workingDirectory2 = "/tmp";

        String id2 = addMavenTwoProject( store,
                                 name2,
                                 scmUrl2,
                                 nagEmailAddress2,
                                 version2,
                                 commandLineArguments2,
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
                             MavenTwoBuildExecutor.ID,
                             workingDirectory1,
                             project1 );

        ContinuumProject project2 = (ContinuumProject) projects.get( name2 );

        assertProjectEquals( id2,
                             name2,
                             scmUrl2,
                             nagEmailAddress2,
                             version2,
                             commandLineArguments2,
                             MavenTwoBuildExecutor.ID,
                             workingDirectory2,
                             project2 );
    }

    public void testRemoveProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String projectId = addMavenTwoProject( "Remove Test Project", "scm:remove-project" );

        String buildId = createBuild( store, projectId, false );

        UpdateScmResult scmResult = new UpdateScmResult();

        ScmFile file = new ScmFile();

        file.setPath( "foo" );

        scmResult.addUpdatedFile( file );

        setBuildResult( store,
                        buildId,
                        ContinuumProjectState.OK,
                        makeContinuumBuildExecutionResult( true, "", "", 0 ),
                        scmResult,
                        null );

        store.removeProject( projectId );
    }

    private ContinuumBuildExecutionResult makeContinuumBuildExecutionResult( boolean success,
                                                                             String standardOutput,
                                                                             String standardError,
                                                                             int exitCode )
    {
        return new ContinuumBuildExecutionResult( success,
                                                  standardOutput,
                                                  standardError,
                                                  exitCode );
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

        // ----------------------------------------------------------------------
        // Construct a build object
        // ----------------------------------------------------------------------

        String buildId = createBuild( store, projectId, false );

        UpdateScmResult updateScmResult = new UpdateScmResult();

        updateScmResult.setCommandOutput( "commandOutput" );

        updateScmResult.setProviderMessage( "providerMessage" );

        updateScmResult.setSuccess( true );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        updateScmResult.getUpdatedFiles().add( scmFile );

        setBuildComplete( store,
                          buildId,
                          updateScmResult,
                          makeContinuumBuildExecutionResult( true, "stdout", "stderr", 10 ) );

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

    private void setBuildComplete( ContinuumStore store,
                                   String buildId,
                                   UpdateScmResult updateScmResult,
                                   ContinuumBuildExecutionResult result )
        throws ContinuumStoreException
    {
        ContinuumBuild build = store.getBuild( buildId );

        build.setUpdateScmResult( updateScmResult );

        build.setSuccess( result.isSuccess() );

        build.setStandardOutput( result.getStandardOutput() );

        build.setStandardError( result.getStandardError() );

        build.setExitCode( result.getExitCode() );

        store.updateBuild( build );
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

        String projectIdFoo = addMavenTwoProject( "Foo Project", "scm:association-foo" );

        String projectIdBar = addMavenTwoProject( "Bar Project", "scm:association-bar" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( 0, createBuild( store, projectId, false ) );

            createBuild( store, projectIdFoo, false );

            createBuild( store, projectIdBar, false );

            createBuild( store, projectIdFoo, false );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getLatestBuildForProject( projectId );

        assertNotNull( build );

        assertEquals( build.getId(), (String) expectedBuilds.get( 0 ) );

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

        int size = 10;

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < size; i++ )
        {
            expectedBuilds.add( createBuild( store, projectId, false ) );
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
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        lookup( JdoFactory.ROLE );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String projectId = addMavenTwoProject( "Build Result Project", "scm:build/result" );

        long now = System.currentTimeMillis();

        String buildId = createBuild( store, projectId, false );

        assertIsCommitted( store );

        assertNotNull( buildId );

        // ----------------------------------------------------------------------
        // Check that the project's state has been updated
        // ----------------------------------------------------------------------

        assertIsCommitted( store );

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

        assertIsCommitted( store );

        UpdateScmResult scmResult = new UpdateScmResult();

        setBuildResult( store,
                        buildId,
                        ContinuumProjectState.OK,
                        makeContinuumBuildExecutionResult( true, "output", "error", 1 ),
                        scmResult,
                        null );

        assertIsCommitted( store );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        build = store.getBuild( buildId );

        assertIsCommitted( store );

        assertEquals( 1, build.getExitCode() );

        assertEquals( "output", build.getStandardOutput() );

        assertEquals( "error", build.getStandardError() );
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

    public static String addMavenTwoProject( ContinuumStore store,
                                             ContinuumProject project )
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

        setCheckoutDone( store, projectId, checkOutScmResult, null, null );

        project = store.getProject( projectId );

        assertNotNull( project );

        return projectId;
    }

    public static String addMavenTwoProject( ContinuumStore store,
                                             String name,
                                             String scmUrl )
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
                                             String workingDirectory )
        throws Exception
    {
        String projectId = store.addProject( makeMavenTwoProject( name,
                                                                  scmUrl,
                                                                  nagEmailAddress,
                                                                  version,
                                                                  commandLineArguments,
                                                                  workingDirectory ) );

        CheckOutScmResult checkOutScmResult = new CheckOutScmResult();

        checkOutScmResult.setSuccess( true );

        setCheckoutDone( store, projectId, checkOutScmResult, null, null );

        ContinuumProject project = store.getProject( projectId );

        assertNotNull( project );

        return projectId;
    }

    public static String createBuild( ContinuumStore store, String projectId, boolean forced )
        throws ContinuumStoreException
    {
        ContinuumBuild build = new ContinuumBuild();

        build.setStartTime( System.currentTimeMillis() );

        build.setState( ContinuumProjectState.BUILDING );

        build.setForced( forced );

        return store.addBuild( projectId, build );
    }

    private static void setCheckoutDone( ContinuumStore store,
                                  String projectId,
                                  CheckOutScmResult checkOutScmResult,
                                  String errorMessage,
                                  Throwable exception  )
        throws ContinuumStoreException
    {
        ContinuumProject project = store.getProject( projectId );

        project.setCheckOutScmResult( checkOutScmResult );

        project.setCheckOutErrorMessage( errorMessage );

        project.setCheckOutErrorException( ContinuumUtils.throwableToString( exception ) );

        store.updateProject( project );
    }

    public static void setBuildResult( ContinuumStore store,
                                       String buildId,
                                       int state,
                                       ContinuumBuildExecutionResult result,
                                       UpdateScmResult scmResult,
                                       Throwable error )
        throws ContinuumStoreException
    {
        ContinuumBuild build = store.getBuild( buildId );

        build.setState( state );

        build.setEndTime( new Date().getTime() );

        build.setError( ContinuumUtils.throwableToString( error ) );

        build.setUpdateScmResult( scmResult );

        // ----------------------------------------------------------------------
        // Copy over the build result
        // ----------------------------------------------------------------------

        build.setSuccess( result.isSuccess() );

        build.setStandardOutput( result.getStandardOutput() );

        build.setStandardError( result.getStandardError() );

        build.setExitCode( result.getExitCode() );

        store.updateBuild( build );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

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
        assertProjectEquals( projectId,
                             name,
                             scmUrl,
                             createMailNotifierList( emailAddress),
                             version,
                             commandLineArguments,
                             builderId,
                             workingDirectory,
                             actual );
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

        assertNotNull( "project.notifiers", actual.getNotifiers() );

        assertEquals( "project.notifiers.size", notifiers.size(), actual.getNotifiers().size() );

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
