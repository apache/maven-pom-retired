package org.apache.maven.continuum.buildqueue;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
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
        Project project = addProject( getStore(), "Build Queue Project 1" );

        int projectId = project.getId();

        buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );

        assertNextBuildIs( projectId );

        assertNextBuildIsNull();

        buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );

        buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );
        buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );
        buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );
        buildProject( projectId, ContinuumProjectState.TRIGGER_SCHEDULED );

        assertNextBuildIs( projectId );

        assertNextBuildIsNull();
    }

    public void testTheQueueWithMultipleProjects()
        throws Exception
    {
        int projectId1 = addProject( getStore(), "Build Queue Project 2" ).getId();

        int projectId2 = addProject( getStore(), "Build Queue Project 3" ).getId();

        buildProject( projectId1, ContinuumProjectState.TRIGGER_SCHEDULED );

        buildProject( projectId2, ContinuumProjectState.TRIGGER_SCHEDULED );

        assertNextBuildIs( projectId1 );

        assertNextBuildIs( projectId2 );

        assertNextBuildIsNull();

        for ( int i = 0; i < 5; i++ )
        {
            buildProject( projectId1, ContinuumProjectState.TRIGGER_SCHEDULED );
            buildProject( projectId2, ContinuumProjectState.TRIGGER_SCHEDULED );
        }

        assertNextBuildIs( projectId1 );
        assertNextBuildIs( projectId2 );

        assertNextBuildIsNull();
    }

    public void testTestTheQueueWithASingleProjectAndForcedBuilds()
        throws Exception
    {
        String name = "Build Queue Project 4";

        int projectId = addProject( getStore(), name ).getId();

        buildProject( projectId, ContinuumProjectState.TRIGGER_FORCED );

        assertNextBuildIs( projectId );

        assertNextBuildIsNull();

        for ( int i = 0; i < 5; i++ )
        {
            buildProject( projectId, ContinuumProjectState.TRIGGER_FORCED );
        }

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

    private void buildProject( int projectId, int trigger )
        throws Exception
    {
        buildQueue.put( new BuildProjectTask( projectId, 0, trigger ) );
    }

    private void assertNextBuildIs( int expectedProjectId )
        throws Exception
    {
        Task task = buildQueue.take();

        assertEquals( BuildProjectTask.class.getName(), task.getClass().getName() );

        BuildProjectTask buildProjectTask = (BuildProjectTask) task;

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
