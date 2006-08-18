package org.apache.maven.continuum;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumTest
    extends AbstractContinuumTest
{
    public void testContinuumConfiguration()
        throws Exception
    {
        lookup( Continuum.ROLE );
    }

    public void testLookups()
        throws Exception
    {
        lookup( TaskQueue.ROLE, "build-project" );

        lookup( TaskQueue.ROLE, "check-out-project" );

        lookup( TaskQueueExecutor.ROLE, "build-project" );

        lookup( TaskQueueExecutor.ROLE, "check-out-project" );
    }

    public void testAddMavenTwoProjectSet()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        int projectCount = getStore().getAllProjectsByName().size();

        int projectGroupCount = getStore().getAllProjectGroupsWithProjects().size();

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-notifiers/pom.xml" );

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( rootPom.toURL().toExternalForm() );

        assertNotNull( result );

        assertEquals( "result.warnings.size", 0, result.getWarnings().size() );

        assertEquals( "result.projects.size", 3, result.getProjects().size() );

        assertEquals( "result.projectGroups.size", 1, result.getProjectGroups().size() );

        System.err.println( "number of projects: " + getStore().getAllProjectsByName().size() );

        System.err.println( "number of project groups: " + getStore().getAllProjectGroupsWithProjects().size() );

        assertEquals( "Total project count", projectCount + 3, getStore().getAllProjectsByName().size() );

        assertEquals( "Total project group count.", projectGroupCount + 1,
                      getStore().getAllProjectGroupsWithProjects().size() );

        Map projects = new HashMap();

        for ( Iterator i = getStore().getAllProjectsByName().iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            projects.put( project.getName(), project );

            // validate project in project group
            assertTrue( "project not in project group", getStore().getProjectGroupByProjectId( project.getId() ) != null );
        }

        assertTrue( "no irc notifier", projects.containsKey( "Continuum IRC Notifier" ) );

        assertTrue( "no jabber notifier", projects.containsKey( "Continuum Jabber Notifier" ) );



    }

    public void testUpdateMavenTwoProject()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        // ----------------------------------------------------------------------
        // Test projects with duplicate names
        // ----------------------------------------------------------------------

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = (Project) projects.get( 0 );

        // reattach
        project = continuum.getProject( project.getId() );

        project.setName( project.getName() + " 2" );

        continuum.updateProject( project );

        project = continuum.getProject( project.getId() );
    }

    public void testBuildDefinitions()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        String url = getTestFile( "src/test-projects/project1/pom.xml" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

        assertNotNull( result );

        List projects = result.getProjects();

        assertEquals( 1, projects.size() );

        assertEquals( Project.class, projects.get( 0 ).getClass() );

        Project project = (Project) projects.get( 0 );

        // reattach
        project = continuum.getProject( project.getId() );

        ProjectGroup projectGroup  = getStore().getProjectGroupByProjectId( project.getId() );

        projectGroup = getStore().getProjectGroupWithBuildDetails( projectGroup.getId() );

        List buildDefs = projectGroup.getBuildDefinitions();

        assertTrue ("missing project group build definition", !buildDefs.isEmpty() );

        assertTrue ("more then one project group build definition on add project", buildDefs.size() == 1 );

        BuildDefinition pgbd = (BuildDefinition) buildDefs.get( 0 );

        assertTrue ( "project group build definition is not default", pgbd.isDefaultForProject() );

        assertTrue ( "project group build definition not default for project", continuum.getDefaultBuildDefinition( project.getId() ).getId() == pgbd.getId() );

        BuildDefinition nbd = new BuildDefinition();
        nbd.setGoals("clean");
        nbd.setArguments("");
        nbd.setDefaultForProject( true );
        nbd.setSchedule( getStore().getScheduleByName( DefaultContinuumInitializer.DEFAULT_SCHEDULE_NAME ) );

        continuum.addBuildDefinitionToProject( project.getId(), nbd );

        assertTrue ( "project lvl build definition not default for project", continuum.getDefaultBuildDefinition( project.getId() ).getId() == nbd.getId() );

        continuum.removeBuildDefinitionFromProject( project.getId(), nbd.getId() );

        assertTrue ( "default build definition didn't toggle back to project group level", continuum.getDefaultBuildDefinition( project.getId() ).getId() == pgbd.getId() );

        try
        {
            continuum.removeBuildDefinitionFromProjectGroup( projectGroup.getId(), pgbd.getId() );
            fail("we were able to remove the default build definition, and that is bad");
        }
        catch (ContinuumException expected)
        {

        }
    }
}
