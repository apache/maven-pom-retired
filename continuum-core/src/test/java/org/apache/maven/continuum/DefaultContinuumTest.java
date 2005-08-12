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

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
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

        int projectCount = getStore().getAllProjects().size();

        int projectGroupCount = getStore().getAllProjectGroupsWithProjects().size();

        File rootPom = getTestFile( "src/test/resources/projects/continuum/continuum-notifiers/pom.xml" );

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( rootPom.toURL().toExternalForm() );

        assertNotNull( result );

        assertEquals( "result.warnings.size", 0, result.getWarnings().size() );

        assertEquals( "result.projects.size", 2, result.getProjects().size() );

        assertEquals( "result.projectGroups.size", 1, result.getProjectGroups().size() );

        System.err.println( "number of projects: " + getStore().getAllProjects().size() );

        System.err.println( "number of project groups: " + getStore().getAllProjectGroupsWithProjects().size() );

        assertEquals( "Total project count", projectCount + 2, getStore().getAllProjects().size() );

        assertEquals( "Total project group count.", projectGroupCount + 1,
                      getStore().getAllProjectGroupsWithProjects().size() );

        Map projects = new HashMap();

        for ( Iterator i = getStore().getAllProjects().iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            projects.put( project.getName(), project );
        }

        assertTrue( "no irc notifier", projects.containsKey( "Continuum IRC Notifier" ) );

        assertTrue( "no jabber notifier", projects.containsKey( "Continuum Jabber Notifier" ) );
        // TODO: assert that the project is the in the group
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

        assertEquals( MavenTwoProject.class, projects.get( 0 ).getClass() );

        MavenTwoProject project = (MavenTwoProject) projects.get( 0 );

        project.setName( project.getName() + " 2" );

        project.setCommandLineArguments( null );

        continuum.updateMavenTwoProject( project );

        project = continuum.getMavenTwoProject( project.getId() );
    }
}
