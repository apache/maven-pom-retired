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

import java.util.List;

import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.store.ContinuumStore;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumTest
    extends PlexusTestCase
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

    public void testUpdateMavenTwoProject()
        throws Exception
    {
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

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

        assertNotNull( "The command line arguments are null.", project.getCommandLineArguments() );
    }
}
