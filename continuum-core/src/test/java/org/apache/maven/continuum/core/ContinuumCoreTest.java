package org.apache.maven.continuum.core;

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

import java.io.File;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.store.ContinuumStore;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumCoreTest
    extends PlexusTestCase
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        File plexusHome = new File( (String) getContainer().getContext().get( "plexus.home" ) );

        if ( plexusHome.exists() )
        {
            FileUtils.cleanDirectory( plexusHome );
        }
    }

    public void testUpdateProject()
        throws Exception
    {
        ContinuumCore core = (ContinuumCore) lookup( ContinuumCore.ROLE );

        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        // ----------------------------------------------------------------------
        // Test projects with duplicate names
        // ----------------------------------------------------------------------

        String projectId = (String) core.addProjectsFromUrl( getTestFile( "src/test-projects/project1/pom.xml" ).toURL(),
                                                             MavenTwoContinuumProjectBuilder.ID ).iterator().next();

        ContinuumProject project = store.getProject( projectId );

        core.updateProject( projectId,
                            project.getName() + "2",
                            project.getScmUrl(),
                            project.getNagEmailAddress(),
                            project.getVersion(),
                            null );

        project = store.getProject( projectId );

        assertNotNull( "The command line arguments are null.", project.getCommandLineArguments() );
    }

    public void testAddDuplicateProject()
        throws Exception
    {
        ContinuumCore core = (ContinuumCore) lookup( ContinuumCore.ROLE );

        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        // ----------------------------------------------------------------------
        // Test projects with duplicate names
        // ----------------------------------------------------------------------

        String projectId = (String) core.addProjectsFromUrl( getTestFile( "src/test-projects/project1/pom.xml" ).toURL(),
                                                             MavenTwoContinuumProjectBuilder.ID ).iterator().next();

        ContinuumProject project = store.getProjectByName( "Continuum Test Project 1" );

        assertNotNull( project );

        assertEquals( projectId, project.getId() );

        try
        {
            core.addProjectsFromUrl( getTestFile( "src/test-projects/project1/pom.xml" ).toURL(),
                                     MavenTwoContinuumProjectBuilder.ID ).iterator().next();

            fail( "Expected a ContinuumException." );
        }
        catch ( ContinuumException e )
        {
            assertTrue( e.getMessage().indexOf( "project with the name" ) != -1 );
        }
    }
}
