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

import org.apache.maven.continuum.AbstractContinuumTest;

import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class BuildQueueTest
    extends AbstractContinuumTest
{
    private TaskQueue buildQueue;

    public void setUp()
        throws Exception
    {
        super.setUp();

        buildQueue = (TaskQueue) lookup( TaskQueue.ROLE, "build-project" );
    }

    public void testTestTheQueueWithASingleProject()
        throws Exception
    {
        String projectId = AbstractContinuumTest.addMavenTwoProject( getStore(), "Build Queue Project 1", "1" );

        buildProject( projectId, false );

        assertNextBuildIs( projectId );

        assertNextBuildIsNull();

        buildProject( projectId, false );

        buildProject( projectId, false );
        buildProject( projectId, false );
        buildProject( projectId, false );
        buildProject( projectId, false );

        assertNextBuildIs( projectId );

        assertNextBuildIsNull();
    }

    public void testTheQueueWithMultipleProjects()
        throws Exception
    {
        String projectId1 = AbstractContinuumTest.addMavenTwoProject( getStore(), "Build Queue Project 2", "foo" );

        String projectId2 = AbstractContinuumTest.addMavenTwoProject( getStore(), "Build Queue Project 3", "bar" );

        buildProject( projectId1, false );

        buildProject( projectId2, false );

        assertNextBuildIs( projectId1 );

        assertNextBuildIs( projectId2 );

        assertNextBuildIsNull();

        buildProject( projectId1, false );

        buildProject( projectId2, false );

        buildProject( projectId1, false );
        buildProject( projectId2, false );
        buildProject( projectId1, false );
        buildProject( projectId2, false );
        buildProject( projectId1, false );
        buildProject( projectId2, false );
        buildProject( projectId1, false );
        buildProject( projectId2, false );

        assertNextBuildIs( projectId1 );
        assertNextBuildIs( projectId2 );

        assertNextBuildIsNull();
    }

    public void testTestTheQueueWithASingleProjectAndForcedBuilds()
        throws Exception
    {
        String name = "Build Queue Project 4";

        String projectId = AbstractContinuumTest.addMavenTwoProject( getStore(), name, "4" );

        buildProject( projectId, true );

        assertNextBuildIs( projectId );

        assertNextBuildIsNull();

        buildProject( projectId, true );
        buildProject( projectId, true );
        buildProject( projectId, true );
        buildProject( projectId, true );
        buildProject( projectId, true );

        assertNextBuildIs( projectId );
        assertNextBuildIs( projectId );
        assertNextBuildIs( projectId );
        assertNextBuildIs( projectId );
        assertNextBuildIs( projectId );

        assertNextBuildIsNull();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void buildProject( String projectId, boolean force )
        throws Exception
    {
        buildQueue.put( new BuildProjectTask( projectId, force ) );
    }

    private void assertNextBuildIs( String expectedProjectId )
        throws Exception
    {
        Task task = buildQueue.take();

        assertEquals( BuildProjectTask.class.getName(), task.getClass().getName() );

        BuildProjectTask buildProjectTask = ( BuildProjectTask ) task;

        assertEquals( "Didn't get the expected project id.", expectedProjectId, buildProjectTask.getProjectId() );
    }

    private void assertNextBuildIsNull()
        throws Exception
    {
        Task task = buildQueue.take();

        if ( task != null )
        {
            fail( "Got a non-null build task returned. Project id: " + ( (BuildProjectTask) task ).getProjectId() );
        }
    }
}
