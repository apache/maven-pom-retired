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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: MavenBuilderHelperTest.java,v 1.1.1.1 2005/03/29 20:42:04 trygvis Exp $
 */
public class MavenTwoContinuumProjectBuilderTest
    extends PlexusTestCase
{
    public void testGetNagEmailAddressWhenTypeIsSetToEmail()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder)
            lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-1.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.createProjectsFromMetadata( pom.toURL() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        MavenTwoProject project = ( MavenTwoProject ) result.getProjects().get( 0 );

        assertEquals( "foo@bar", project.getNagEmailAddress() );
    }

    public void testGetNagEmailAddressWhenTypeIsntSet()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder)
            lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-2.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.createProjectsFromMetadata( pom.toURL() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        MavenTwoProject project = (MavenTwoProject) result.getProjects().get( 0 );

        assertEquals( "foo@bar", project.getNagEmailAddress() );
    }

    public void testCreateProjectsWithModules()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder)
            lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        String url = getTestFile( "src/test/resources/projects/continuum/pom.xml?foo=bar" ).toURL().toExternalForm();

        ContinuumProjectBuildingResult result = projectBuilder.createProjectsFromMetadata( new URL( url ) );

        assertNotNull( result );

        assertEquals( 6, result.getProjects().size() );

        Map projects = new HashMap();

        for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
        {
            MavenTwoProject project = (MavenTwoProject) it.next();

            assertNotNull( project.getName() );

            projects.put( project.getName(), project );
        }

        assertMavenTwoProject( "Continuum Parent Project", projects );
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
        MavenTwoProject project = (MavenTwoProject) projects.get( name );

        assertNotNull( project );

        assertEquals( name, project.getName() );

        String scmUrl = "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/trunk";

        assertTrue( project.getScmUrl().startsWith( scmUrl ) );

        assertEquals( 1, project.getConfiguration().size() );

        assertEquals( "clean:clean, install",
                      project.getConfiguration().get( MavenTwoBuildExecutor.CONFIGURATION_GOALS ) );
    }
}
