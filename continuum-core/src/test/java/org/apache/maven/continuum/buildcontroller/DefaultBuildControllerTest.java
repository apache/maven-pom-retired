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
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.ContinuumProjectState;

import java.util.Calendar;

public class DefaultBuildControllerTest
    extends AbstractContinuumTest
{
    private DefaultBuildController controller;

    int projectId1;

    int projectId2;

    int buildDefinitionId1;

    int buildDefinitionId2;

    public void setUp()
        throws Exception
    {
        super.setUp();

        Project project1 = createProject( "project1" );
        BuildDefinition bd1 = createBuildDefinition();
        project1.addBuildDefinition( bd1 );
        project1.setState( ContinuumProjectState.OK );
        projectId1 = addProject( getStore(), project1 ).getId();
        buildDefinitionId1 = getStore().getDefaultBuildDefinition( projectId1 ).getId();
        project1 = getStore().getProject( projectId1 );
        BuildResult buildResult1 = new BuildResult();
        buildResult1.setStartTime( Calendar.getInstance().getTimeInMillis() );
        buildResult1.setEndTime( Calendar.getInstance().getTimeInMillis() );
        buildResult1.setState( ContinuumProjectState.OK);
        buildResult1.setSuccess( true );
        getStore().addBuildResult( project1, buildResult1 );
        BuildResult buildResult2 = new BuildResult();
        buildResult2.setStartTime( Calendar.getInstance().getTimeInMillis() - 7200000 );
        buildResult2.setEndTime( Calendar.getInstance().getTimeInMillis() - 7200000 );
        buildResult2.setSuccess( true );
        buildResult2.setState( ContinuumProjectState.OK);
        getStore().addBuildResult( project1, buildResult2 );

        Project project2 = createProject( "project2" );
        ProjectDependency dep1 = new ProjectDependency();
        dep1.setGroupId( "org.apache.maven.testproject" );
        dep1.setArtifactId( "project1" );
        dep1.setVersion( "1.0-SNAPSHOT" );
        project2.addDependency( dep1 );
        ProjectDependency dep2 = new ProjectDependency();
        dep2.setGroupId( "junit" );
        dep2.setArtifactId( "junit" );
        dep2.setVersion( "3.8.1" );
        project2.addDependency( dep2 );
        BuildDefinition bd2 = createBuildDefinition();
        project2.addBuildDefinition( bd2 );
        project2.setState( ContinuumProjectState.OK );
        projectId2 = addProject( getStore(), project2 ).getId();
        buildDefinitionId2 = getStore().getDefaultBuildDefinition( projectId2 ).getId();

        controller = (DefaultBuildController) lookup( BuildController.ROLE );
    }

    private Project createProject( String artifactId )
    {
        Project project = new Project();
        project.setExecutorId( "maven2" );
        project.setName( artifactId );
        project.setGroupId( "org.apache.maven.testproject" );
        project.setArtifactId( artifactId );
        project.setVersion( "1.0-SNAPSHOT" );
        return project;
    }

    private BuildDefinition createBuildDefinition()
    {
        BuildDefinition builddef = new BuildDefinition();
        builddef.setBuildFile( "pom.xml" );
        builddef.setGoals( "clean" );
        builddef.setDefaultForProject( true );
        return builddef;
    }

    private BuildContext getContext( int hourOfLastExecution )
        throws Exception
    {
        BuildContext context = controller.initializeBuildContext( projectId2, buildDefinitionId2,
                                                                  ContinuumProjectState.TRIGGER_SCHEDULED );
        BuildResult oldBuildResult = new BuildResult();
        oldBuildResult.setEndTime( Calendar.getInstance().getTimeInMillis() + ( hourOfLastExecution * 3600000 ) );
        context.setOldBuildResult( oldBuildResult );
        context.setScmResult( new ScmResult() );
        return context;
    }

    public void testWithoutDependencyChanges()
        throws Exception
    {
        BuildContext context = getContext( +1 );
        controller.checkProjectDependencies( context );
        assertEquals( 0, context.getModifiedDependencies().size() );
        assertFalse( controller.shouldBuild( context ) );
    }

    public void testWithNewProjects()
        throws Exception
    {
        Project p1 = getStore().getProject( projectId1 );
        p1.setState( ContinuumProjectState.NEW );
        getStore().updateProject( p1 );

        Project p2 = getStore().getProject( projectId2 );
        p2.setState( ContinuumProjectState.NEW );
        getStore().updateProject( p2 );

        BuildContext context = getContext( +1 );
        controller.checkProjectDependencies( context );
        assertEquals( 0, context.getModifiedDependencies().size() );
        assertTrue( controller.shouldBuild( context ) );
    }

    public void testWithDependencyChanges()
        throws Exception
    {
        BuildContext context = getContext( -1 );
        controller.checkProjectDependencies( context );
        assertEquals( 1, context.getModifiedDependencies().size() );
        assertTrue( controller.shouldBuild( context ) );
    }
}