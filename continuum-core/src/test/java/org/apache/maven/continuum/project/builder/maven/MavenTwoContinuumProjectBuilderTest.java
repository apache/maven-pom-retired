package org.apache.maven.continuum.project.builder.maven;

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
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoContinuumProjectBuilderTest
    extends AbstractContinuumTest
{
    public void testGetEmailAddressWhenTypeIsSetToEmail()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE,
                                                                                   MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-1.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null );

        assertNotNull( result.getWarnings() );

        assertEquals( 0, result.getWarnings().size() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        Project project = (Project) result.getProjects().get( 0 );

        assertNotNull( project.getNotifiers() );

        assertEquals( 1, project.getNotifiers().size() );

        ProjectNotifier notifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "mail", notifier.getType() );

        assertEquals( "foo@bar", notifier.getConfiguration().get( "address" ) );
    }

    public void testGetEmailAddressWhenTypeIsntSet()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE,
                                                                                   MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-2.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( pom.toURL(), null, null );

        assertNotNull( result.getWarnings() );

        assertEquals( 0, result.getWarnings().size() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        Project project = (Project) result.getProjects().get( 0 );

        assertNotNull( project.getNotifiers() );

        assertEquals( 1, project.getNotifiers().size() );

        ProjectNotifier notifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "mail", notifier.getType() );

        assertEquals( "foo@bar", notifier.getConfiguration().get( "address" ) );
    }

    public void testCreateProjectsWithModules()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE,
                                                                                   MavenTwoContinuumProjectBuilder.ID );

        String url = getTestFile( "src/test/resources/projects/continuum/pom.xml" ).toURL().toExternalForm();

        // Eat System.out
        PrintStream ps = System.out;

        System.setOut( new PrintStream( new ByteArrayOutputStream() ) );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( new URL( url ), null, null );

        System.setOut( ps );

        assertNotNull( result );

        // ----------------------------------------------------------------------
        // Assert the warnings
        // ----------------------------------------------------------------------

        assertNotNull( result.getWarnings() );

        assertEquals( 1, result.getWarnings().size() );

        assertTrue( "Does not end with \"I'm-not-here-project/pom.xml\"",
                    result.getWarnings().get( 0 ).toString().indexOf( "I'm-not-here-project/pom.xml" ) != -1 );

        // ----------------------------------------------------------------------
        // Assert the project group built
        // ----------------------------------------------------------------------

        assertNotNull( result.getProjectGroups() );

        assertEquals( 1, result.getProjectGroups().size() );

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().iterator().next();

        assertEquals( "projectGroup.groupId", "org.apache.maven.continuum", projectGroup.getGroupId() );

        assertEquals( "projectGroup.name", "Continuum Parent Project", projectGroup.getName() );

        assertEquals( "projectGroup.description", "Continuum Project Description", projectGroup.getDescription() );

//        assertEquals( "projectGroup.url", "http://cvs.continuum.codehaus.org/", projectGroup.getUrl() );

        // ----------------------------------------------------------------------
        // Assert the projects built
        // ----------------------------------------------------------------------

        assertNotNull( result.getProjects() );

        assertEquals( 5, result.getProjects().size() );

        Map projects = new HashMap();

        for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
        {
            Project project = (Project) it.next();

            assertNotNull( project.getName() );

            projects.put( project.getName(), project );
        }

        assertMavenTwoProject( "Continuum Core", projects );
        assertMavenTwoProject( "Continuum Model", projects );
        assertMavenTwoProject( "Continuum Plexus Application", projects );
        assertMavenTwoProject( "Continuum Web", projects );
        assertMavenTwoProject( "Continuum XMLRPC Interface", projects );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void assertMavenTwoProject( String name, Map projects )
    {
        Project project = (Project) projects.get( name );

        assertNotNull( project );

        assertEquals( name, project.getName() );

        String scmUrl = "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/trunk";

        assertTrue( project.getScmUrl().startsWith( scmUrl ) );
    }
}
