package org.apache.maven.continuum.buildcontroller;

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
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 */
public class BuildProjectTaskExecutorTest
    extends AbstractContinuumTest
{
    private ContinuumProjectBuilder projectBuilder;

    private TaskQueue buildQueue;

    private TaskQueueExecutor taskQueueExecutor;

    private ContinuumStore continuumStore;

    private ActionManager actionManager;

    public void setUp()
        throws Exception
    {
        super.setUp();

        projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE,
                                                           MavenTwoContinuumProjectBuilder.ID );

        buildQueue = (TaskQueue) lookup( TaskQueue.ROLE, "build-project" );

        taskQueueExecutor = (TaskQueueExecutor) lookup( TaskQueueExecutor.ROLE, "build-project" );

        continuumStore = (ContinuumStore) lookup( ContinuumStore.ROLE );

        actionManager = (ActionManager) lookup( ActionManager.ROLE );
    }

    public void testAutomaticCancellation()
        throws Exception
    {
        runTimeoutProject( 13000 );

        long taskStartTime = System.currentTimeMillis();

        // should be killed in 5 secs, plus slack
        waitForTaskDead( 10000 );

        // the project will sleep for 15 seconds and then write a file.
        // Make sure we sleep at least that long and then check for the file;
        // it should not be there.

        long taskWaitTime = 15000 - ( System.currentTimeMillis() - taskStartTime );

        System.err.println( "Sleeping " + taskWaitTime + "ms" );
        Thread.sleep( taskWaitTime );

        assertFalse( "Build completed", getTestFile( "src/test-projects/timeout/target/TEST-COMPLETED" ).exists() );
    }

    public void testManualCancellation()
        throws Exception
    {
        BuildProjectTask task = runTimeoutProject( 0 );

        assertFalse( "Build completed", getTestFile( "src/test-projects/timeout/target/TEST-COMPLETED" ).exists() );

        long taskStartTime = System.currentTimeMillis();

        assertTrue( taskQueueExecutor.cancelTask( task ) );

        waitForTaskDead( 5000 );

        long taskWaitTime = 15000 - ( System.currentTimeMillis() - taskStartTime );

        System.err.println( "Sleeping " + taskWaitTime + "ms" );
        Thread.sleep( taskWaitTime );

        assertFalse( "Build completed", getTestFile( "src/test-projects/timeout/target/TEST-COMPLETED" ).exists() );
    }

    public void testNoCancellation()
        throws Exception
    {
        runTimeoutProject( 0 );

        waitForFile( "src/test-projects/timeout/target/TEST-COMPLETED", 20000 );

        waitForTaskDead( 10000 );
    }

    private void waitForFile( String file, int max )
        throws InterruptedException
    {
        long time = System.currentTimeMillis();

        for ( int i = 0; i < max / 10; i++ )
        {
            if ( getTestFile( file ).exists() )
            {
                break;
            }
            Thread.sleep( 10 );
        }

        System.err.println( "Waited " + ( System.currentTimeMillis() - time ) + "ms for file " + file );

        assertTrue( "File " + file, getTestFile( file ).exists() );
    }

    private void waitForTaskDead( int maxWait )
        throws InterruptedException
    {
        for ( int i = 0; i < maxWait / 10; i++ )
        {
            if ( taskQueueExecutor.getCurrentTask() == null )
            {
                break;
            }

            Thread.sleep( 10 );
        }

        assertNull( "No current task", taskQueueExecutor.getCurrentTask() );
    }

    /**
     * Runs the timeout test project through the build queue and return when the unit test in it has started. The
     * project contains a unit test that sleeps for 15 seconds.
     *
     * @param maxRunTime
     *            maximum time the build may run before it's auto cancelled; 0 means forever.
     * @return
     * @throws Exception
     */
    private BuildProjectTask runTimeoutProject( int maxRunTime )
        throws Exception
    {
        BuildProjectTask task = createTask( maxRunTime );

        FileUtils.forceDelete( getTestFile( "src/test-projects/timeout/target/TEST-STARTED" ) );
        FileUtils.forceDelete( getTestFile( "src/test-projects/timeout/target/TEST-COMPLETED" ) );

        System.err.println( "Queueing build" );

        this.buildQueue.put( task );

        System.err.println( "Waiting for task to start" );

        Task curTask = null;

        // Sleep at most 10 seconds for the task to start
        for ( int i = 0; i < 1000; i++ )
        {
            curTask = taskQueueExecutor.getCurrentTask();

            if ( curTask != null )
            {
                break;
            }

            Thread.sleep( 10 );
        }

        assertNotNull( "Task not started", task );

        // wait for the start file to be written

        waitForFile( "src/test-projects/timeout/target/TEST-STARTED", 10000 );

        System.err.println( "Task started, TEST-STARTED file created." );

        return task;
    }

    private BuildProjectTask createTask( int maxRunTime )
        throws Exception
    {
        ProjectGroup projectGroup = getProjectGroup( "src/test-projects/timeout/pom.xml" );
        Project project = (Project) projectGroup.getProjects().get( 0 );

        BuildDefinition buildDefinition = new BuildDefinition();
        buildDefinition.setId( 0 );
        buildDefinition.setGoals( "install" );

        projectGroup.addBuildDefinition( buildDefinition );

        Map pgContext = new HashMap();

        pgContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY, project.getWorkingDirectory() );

        pgContext.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT_GROUP, projectGroup );

        actionManager.lookup( "validate-project-group" ).execute( pgContext );

        actionManager.lookup( "store-project-group" ).execute( pgContext );

        int projectGroupId = AbstractContinuumAction.getProjectGroupId( pgContext );

        projectGroup = continuumStore.getProjectGroupWithBuildDetailsByProjectGroupId( projectGroupId );

        project = (Project) projectGroup.getProjects().get( 0 );

        buildDefinition = (BuildDefinition) projectGroup.getBuildDefinitions().get( 0 );

        // projectGroup = continuumStore.addProjectGroup( projectGroup );

        BuildProjectTask task = new BuildProjectTask( project.getId(), buildDefinition.getId(), 0 );

        task.setMaxExecutionTime( maxRunTime );

        return task;
    }

    private ProjectGroup getProjectGroup( String pomResource )
        throws ContinuumProjectBuilderException, IOException
    {
        File pom = getTestFile( pomResource );

        assertNotNull( "Can't find project " + pomResource, pom );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null );

        // some assertions to make sure our expectations match. This is NOT
        // meant as a unit test for the projectbuilder!

        assertNotNull( "Project list not null", result.getProjects() );

        assertEquals( "#Projectgroups", 1, result.getProjectGroups().size() );

        ProjectGroup pg = (ProjectGroup) result.getProjectGroups().get( 0 );

        // If the next part fails, remove this code! Then result.getProjects
        // might be empty, and result.projectgroups[0].getProjects contains
        // the single project!

        assertEquals( "#Projects in result", 1, result.getProjects().size() );

        Project p = (Project) result.getProjects().get( 0 );

        pg.addProject( p );

        p.setWorkingDirectory( pom.getParent() );

        return pg;
    }
}
