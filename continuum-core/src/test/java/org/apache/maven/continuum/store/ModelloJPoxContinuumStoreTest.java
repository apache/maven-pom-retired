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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.continuum.builder.shell.ShellBuildResult;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.ScmFile;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ModelloJPoxContinuumStoreTest.java,v 1.2 2005/04/07 23:27:41 trygvis Exp $
 */
public class ModelloJPoxContinuumStoreTest
    extends PlexusTestCase
{
    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.cleanDirectory( getTestPath( "target/plexus-home" ) );
    }

    public void testTransactionHandling()
        throws Exception
    {
        JdoFactory jdoFactory = (JdoFactory) lookup( JdoFactory.ROLE );

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

        System.err.println( "Added project, id: " + projectId );

        assertNotNull( "The project id is null.", projectId );

        System.err.println( "Loading project" );

        ContinuumProject project = store.getProject( projectId );

        System.err.println( "project.id: " + project.getId() );

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

        assertNotNull( store.getProject( projectId ) );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String name2 = "name 2";
        String scmUrl2 = "scm url 2";
        String nagEmailAddress2 = "2@bar";
        String version2 = "v2";
        Properties properties2 = new Properties();

        store.updateProject( projectId, name2, scmUrl2, nagEmailAddress2, version2  );

        ContinuumProject project = store.getProject( projectId );

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

        ContinuumProject a = store.getProject( id1 );
        System.err.println( "a.name: " + a.getName() );

        String name2 = "Test Project 2";
        String scmUrl2 = "scm:local:src/test/repo";
        String nagEmailAddress2 = "foo@bar.com";
        String version2 = "1.0";
        String builderId2 = "maven2";
        String workingDirectory2 = "/tmp";
        Properties configuration2 = new Properties();

        String id2 = store.addProject( name2, scmUrl2, nagEmailAddress2, version2, builderId2, workingDirectory2, configuration2 );

        Map projects = new HashMap();

        for ( Iterator it = store.getAllProjects(); it.hasNext(); )
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

        String buildId = store.createBuild( projectId );

        ContinuumBuildResult result = new ShellBuildResult();

        ScmFile file = new ScmFile();

        file.setPath( "foo" );

        result.addChangedFile( file );

        store.setBuildResult( buildId, ContinuumProjectState.OK, result, null );

        store.removeProject( projectId );
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

        String buildId = store.createBuild( projectId );

        Iterator it = store.getBuildsForProject( projectId, 0, 0 );

        assertNotNull( "The iterator with all builds was null.", it );

        List builds = CollectionUtils.iteratorToList( it );

        assertEquals( "Expected the build set to contain a single build.", 1, builds.size() );

        ContinuumBuild build = (ContinuumBuild) builds.get( 0 );

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
        //
        // ----------------------------------------------------------------------

        String projectId = addProject( "Test Project" );

        String projectIdFoo = addProject( "Foo Project" );

        String projectIdBar = addProject( "Bar Project" );

        List expectedBuilds = new ArrayList();

        for ( int i = 0; i < 10; i++ )
        {
            expectedBuilds.add( 0, store.createBuild( projectId ) );

            store.createBuild( projectIdFoo );

            store.createBuild( projectIdBar );

            store.createBuild( projectIdFoo );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Iterator builds = store.getBuildsForProject( projectId, 0, 0 );

        List actualBuilds = CollectionUtils.iteratorToList( builds );

        assertEquals( "builds.size", expectedBuilds.size(), actualBuilds.size() );

        Iterator it;

        int i;

        for ( it = expectedBuilds.iterator(), i = 0; it.hasNext(); i++ )
        {
            String expectedBuildId = (String) it.next();

            String actualBuildId = ((ContinuumBuild) actualBuilds.get( i )).getId();

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

        long now = System.currentTimeMillis();

        String buildId = store.createBuild( projectId );

        assertIsCommitted( store );

        assertNotNull( buildId );

        // ----------------------------------------------------------------------
        // Check that the project's has been updated
        // ----------------------------------------------------------------------

        ContinuumProject project = store.getProject( projectId );

        assertIsCommitted( store );

        assertEquals( ContinuumProjectState.BUILD_SIGNALED, project.getState() );

        // ----------------------------------------------------------------------
        // Check the build
        // ----------------------------------------------------------------------

        ContinuumBuild build = store.getBuild( buildId );

        assertIsCommitted( store );

        assertNotNull( build );

        assertEquals( now / 10000, build.getStartTime() / 10000 );

        assertEquals( 0, build.getEndTime() );

        assertNull( build.getError() );

        assertEquals( ContinuumProjectState.BUILD_SIGNALED, build.getState() );

        // ----------------------------------------------------------------------
        // Check the build result
        // ----------------------------------------------------------------------

        ContinuumBuildResult result = store.getBuildResultForBuild( buildId );

        assertIsCommitted( store );

        assertNull( result );

        ShellBuildResult shellBuildResult = new ShellBuildResult();

        shellBuildResult.setExitCode( 1 );

        shellBuildResult.setStandardOutput( "output" );

        shellBuildResult.setStandardError( "error" );

        store.setBuildResult( buildId, ContinuumProjectState.OK, shellBuildResult, null );

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
    //
    // ----------------------------------------------------------------------

    private void assertIsCommitted( ContinuumStore store )
    {
        ContinuumJPoxStore.ThreadState state = ( (ModelloJPoxContinuumStore) store ).getStore().getThreadState();

        assertNull( state );
    }

    private String addProject( String name )
        throws Exception
    {
        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        String scmUrl = "scm:local:src/test/repo";
        String nagEmailAddress = "foo@bar.com";
        String version = "1.0";
        String builderId = "maven2";
        String workingDirectory = "/tmp";
        Properties configuration = new Properties();

        return store.addProject( name, scmUrl, nagEmailAddress, version, builderId, workingDirectory, configuration );
    }

    private void assertProjectEquals( String projectId, String name, String scmUrl, String nagEmailAddress, String version, String builderId, String workingDirectory, Properties configuration, ContinuumProject project )
    {
        assertEquals( "project.id", projectId, project.getId() );

        assertEquals( "porject.name", name, project.getName() );

        assertEquals( "porject.scmUrl", scmUrl, project.getScmUrl() );

        assertEquals( "project.nagEmailAddress", nagEmailAddress, project.getNagEmailAddress() );

        assertEquals( "project.version", version, project.getVersion() );

        assertEquals( "project.builderId", builderId, project.getBuilderId() );

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
