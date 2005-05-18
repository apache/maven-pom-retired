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
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.UpdateScmResult;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.JdoFactory;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ModelloJPoxContinuumStoreTest.java,v 1.2 2005/04/07 23:27:41 trygvis Exp $
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

        String name = "Test Project";
        String scmUrl = "scm:local:src/test/repo";
        String nagEmailAddress = "foo@bar.com";
        String version = "1.0";
        String builderId = "maven2";
        String workingDirectory = "/tmp";
        Properties configuration = new Properties();

        configuration.setProperty( "foo", "bar" );

        String projectId = store.addProject( name, scmUrl, nagEmailAddress, version, builderId, workingDirectory, configuration );

        assertNotNull( "The project id is null.", projectId );

        ContinuumProject project = store.getProject( projectId );

        assertProjectEquals( projectId, name, scmUrl, nagEmailAddress, version, builderId, workingDirectory,
                             configuration, project );
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

        String name = "Test Project";
        String scmUrl = "scm:local:src/test/repo";
        String nagEmailAddress = "foo@bar.com";
        String version = "1.0";
        String builderId = "maven2";
        String workingDirectory = "/tmp";
        Properties properties = new Properties();

        String projectId = store.addProject( name, scmUrl, nagEmailAddress, version, builderId, workingDirectory, properties );

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
        String nagEmailAddress2 = "2@bar";
        String version2 = "v2";
        Properties properties2 = new Properties();

        store.updateProject( projectId, name2, scmUrl2, nagEmailAddress2, version2  );

        project = store.getProject( projectId );

        assertProjectEquals( projectId, name2, scmUrl2, nagEmailAddress2, version2, builderId, workingDirectory,
                             properties2, project );

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
        String scmUrl1 = "scm:local:src/test/repo";
        String nagEmailAddress1 = "foo@bar.com";
        String version1 = "1.0";
        String builderId1 = "maven2";
        String workingDirectory1 = "/tmp";
        Properties configuration1 = new Properties();

        String id1 = store.addProject( name1, scmUrl1, nagEmailAddress1, version1, builderId1, workingDirectory1, configuration1 );

        String name2 = "Test Project 2";
        String scmUrl2 = "scm:local:src/test/repo";
        String nagEmailAddress2 = "foo@bar.com";
        String version2 = "1.0";
        String builderId2 = "maven2";
        String workingDirectory2 = "/tmp";
        Properties configuration2 = new Properties();

        String id2 = store.addProject( name2, scmUrl2, nagEmailAddress2, version2, builderId2, workingDirectory2, configuration2 );

        Map projects = new HashMap();

        for ( Iterator it = store.getAllProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            assertNotNull( "While getting all projects: project.id == null", project.getId() );

            assertNotNull( "While getting all projects: project.name == null", project.getName() );

            projects.put( project.getName(), project );
        }

        ContinuumProject project1 = (ContinuumProject) projects.get( name1 );

        assertProjectEquals( id1, name1, scmUrl1, nagEmailAddress1, version1, builderId1, workingDirectory1,
                             configuration1, project1 );

        ContinuumProject project2 = (ContinuumProject) projects.get( name2 );

        assertProjectEquals( id2, name2, scmUrl2, nagEmailAddress2, version2, builderId2, workingDirectory2,
                             configuration2, project2 );
    }

    public void testUpdateProjectConfiguration()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String projectId = addProject( "Test Project" );

        ContinuumProject project = store.getProject( projectId );

        assertEquals( 0, project.getConfiguration().size() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Properties expected = new Properties();

        expected.put( "key", "value" );

        store.updateProjectConfiguration( projectId, expected );

        Properties actual = store.getProject( projectId ).getConfiguration();

        assertNotNull( "The configuration is null", actual );

        assertEquals( expected.size(), actual.size() );

        assertTrue( actual.containsKey( "key" ) );

        assertEquals( "value", actual.getProperty( "key" ) );
    }

    public void testRemoveProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String projectId = addProject( "Test Project" );

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

    public void testAddDuplicateProject()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        // ----------------------------------------------------------------------
        // Test projects with duplicate names
        // ----------------------------------------------------------------------

        String projectId = addProject( "trygve" );

        assertEquals( projectId, store.getProjectByName( "trygve" ).getId() );

        try
        {
            addProject( "trygve" );

            fail( "Expected a exception" );
        }
        catch( ContinuumStoreException e )
        {
            // expected
        }

        ContinuumProject project = makeStubProject( "brett" );

        project.setScmUrl( "foo" );

        addProject( project );

        assertNotNull( store.getProjectByScmUrl( "foo" ) );

        try
        {
            project.setName( "jason" );

            addProject( project );

            fail( "Expected a exception" );
        }
        catch ( ContinuumStoreException e )
        {
            // expected
        }
    }

    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    public void testBuild()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        JdoFactory jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.getPersistenceManagerFactory().close();

        String projectId = addProject( "Test Project" );

        store.setIsUpdating( projectId );

        store.setUpdateDone( projectId );

        String buildId = store.createBuild( projectId, false );

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

        JdoFactory jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.getPersistenceManagerFactory().close();

        // ----------------------------------------------------------------------
        // Set up projects
        // ----------------------------------------------------------------------

        String projectId = addProject( "Test Project" );

        store.setIsUpdating( projectId );

        store.setUpdateDone( projectId );

        String projectIdFoo = addProject( "Foo Project" );

        store.setIsUpdating( projectIdFoo );

        store.setUpdateDone( projectIdFoo );

        String projectIdBar = addProject( "Bar Project" );

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

    public void testBuildResult()
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        JdoFactory jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

        jdoFactory.getPersistenceManagerFactory().close();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String projectId = addProject( "Test Project" );

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
    // Utility methods
    // ----------------------------------------------------------------------

    public static ContinuumProject makeStubProject( String name )
    {
        ContinuumProject project = new ContinuumProject();

        project.setName( name );
        project.setScmUrl( "scm:local:src/test/repo" );
        project.setNagEmailAddress( "foo@bar.com" );
        project.setVersion( "1.0" );
        project.setExecutorId( "maven2" );
        project.setWorkingDirectory( "/tmp" );
        project.setConfiguration( new Properties() );

        return project;
    }

    public static ContinuumProject makeProject( String name,
                                                String scmUrl,
                                                String nagEmailAddress,
                                                String version,
                                                String executorId,
                                                String workingDirectory,
                                                Properties configuration )
    {
        ContinuumProject project = new ContinuumProject();

        project.setName( name );
        project.setScmUrl( scmUrl );
        project.setNagEmailAddress( nagEmailAddress );
        project.setVersion( version );
        project.setExecutorId( executorId );
        project.setWorkingDirectory( workingDirectory );
        project.setConfiguration( configuration );

        return project;
    }

    private String addProject( String name )
        throws Exception
    {
        return addProject( (ContinuumStore) lookup( ContinuumStore.ROLE ), makeStubProject( name ) );
    }

    private String addProject( ContinuumProject project )
        throws Exception
    {
        return addProject( (ContinuumStore) lookup( ContinuumStore.ROLE ), project );
    }

    public static String addProject( ContinuumStore store, ContinuumProject project )
        throws Exception
    {
        return addProject( store,
                           project.getName(),
                           project.getScmUrl(),
                           project.getNagEmailAddress(),
                           project.getVersion(),
                           project.getExecutorId(),
                           project.getWorkingDirectory(),
                           project.getConfiguration() );
    }

    public static String addProject( ContinuumStore store, String name )
        throws Exception
    {
        return addProject( store, makeStubProject( name ) );
    }

    public static String addProject( ContinuumStore store,
                                     String name,
                                     String scmUrl,
                                     String nagEmailAddress,
                                     String version,
                                     String executorId,
                                     String workingDirectory,
                                     Properties configuration )
        throws Exception
    {
        String projectId = store.addProject( name,
                                             scmUrl,
                                             nagEmailAddress,
                                             version,
                                             executorId,
                                             workingDirectory,
                                             configuration );

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

    private void assertProjectEquals( String projectId, String name, String scmUrl, String nagEmailAddress, String version, String builderId, String workingDirectory, Properties configuration, ContinuumProject project )
    {
        assertEquals( "project.id", projectId, project.getId() );

        assertEquals( "porject.name", name, project.getName() );

        assertEquals( "porject.scmUrl", scmUrl, project.getScmUrl() );

        assertEquals( "project.nagEmailAddress", nagEmailAddress, project.getNagEmailAddress() );

        assertEquals( "project.version", version, project.getVersion() );

        assertEquals( "project.executorId", builderId, project.getExecutorId() );

        assertEquals( "project.workingDirectory", workingDirectory, project.getWorkingDirectory() );

        for ( Iterator it = configuration.keySet().iterator(); it.hasNext(); )
        {
            String key = (String) it.next();

            String value = project.getConfiguration().getProperty( key );

            assertNotNull( "Value for key '" + key + "' was null.", value );

            assertEquals( "The values for '" + key + "' doesn't match.", configuration.getProperty( key ), value );
        }
    }
}
