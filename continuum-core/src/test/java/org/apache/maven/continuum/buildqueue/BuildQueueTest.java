package org.apache.maven.continuum.buildqueue;

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

import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ModelloJPoxContinuumStoreTest;
import org.apache.maven.continuum.project.ContinuumProject;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: BuildQueueTest.java,v 1.1.1.1 2005/03/29 20:42:04 trygvis Exp $
 */
public class BuildQueueTest
    extends PlexusTestCase
{
    private TaskQueue buildQueue;

    private ContinuumStore store;

    public void setUp()
        throws Exception
    {
        super.setUp();

        buildQueue = (TaskQueue) lookup( TaskQueue.ROLE, "build-project" );

        store = (ContinuumStore) lookup( ContinuumStore.ROLE );
    }

    public void testTestTheQueueWithASingleProject()
        throws Exception
    {
        String name = "Project 1";

        String project = ModelloJPoxContinuumStoreTest.addProject( store, name );

        String build = buildProject( project, false );

        assertNextBuildIs( build );

        assertNextBuildIsNull();

        String buildX = buildProject( project, false );

        buildProject( project, false );
        buildProject( project, false );
        buildProject( project, false );
        buildProject( project, false );

        assertNextBuildIs( buildX );

        assertNextBuildIsNull();
    }

    public void testTheQueueWithMultipleProjects()
        throws Exception
    {
        String name1 = "Project 1";

        String name2 = "Project 2";

        String project1 = ModelloJPoxContinuumStoreTest.addProject( store, name1 );

        String project2 = ModelloJPoxContinuumStoreTest.addProject( store, name2 );

        String build1 = buildProject( project1, false );

        String build2 = buildProject( project2, false );

        assertNextBuildIs( build1 );

        assertNextBuildIs( build2 );

        assertNextBuildIsNull();

        String buildX1 = buildProject( project1, false );

        String buildX2 = buildProject( project2, false );

        buildProject( project1, false );
        buildProject( project2, false );
        buildProject( project1, false );
        buildProject( project2, false );
        buildProject( project1, false );
        buildProject( project2, false );
        buildProject( project1, false );
        buildProject( project2, false );

        assertNextBuildIs( buildX1 );
        assertNextBuildIs( buildX2 );

        assertNextBuildIsNull();
    }

    public void testTestTheQueueWithASingleProjectAndForcedBuilds()
        throws Exception
    {
        String name = "Project 1";

        String project = ModelloJPoxContinuumStoreTest.addProject( store, name );

        String build = buildProject( project, true );

        assertNextBuildIs( build );

        assertNextBuildIsNull();

        String build1 = buildProject( project, true );
        String build2 = buildProject( project, true );
        String build3 = buildProject( project, true );
        String build4 = buildProject( project, true );
        String build5 = buildProject( project, true );

        assertNextBuildIs( build1 );
        assertNextBuildIs( build2 );
        assertNextBuildIs( build3 );
        assertNextBuildIs( build4 );
        assertNextBuildIs( build5 );

        assertNextBuildIsNull();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String buildProject( String projectId, boolean force )
        throws Exception
    {
        ContinuumProject project = store.getProject( projectId );

        String buildId = store.createBuild( project.getId(), force );

        buildQueue.put( new BuildProjectTask( projectId, buildId, force ) );

        return buildId;
    }

    private void assertNextBuildIs( String expectedBuildId )
        throws Exception
    {
        Task task = buildQueue.take();

        assertEquals( BuildProjectTask.class.getName(), task.getClass().getName() );

        BuildProjectTask buildProjectTask = ( BuildProjectTask ) task;

        assertEquals( "Didn't get the expected build id.", expectedBuildId, buildProjectTask.getBuildId() );
    }

    private void assertNextBuildIsNull()
        throws Exception
    {
        Task task = buildQueue.take();

        if ( task != null )
        {
            fail( "Got a non-null build id returned: " + (( BuildProjectTask ) task ).getBuildId() );
        }
    }
}
