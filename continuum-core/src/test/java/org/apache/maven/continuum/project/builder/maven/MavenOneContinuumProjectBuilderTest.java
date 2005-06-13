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

import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneContinuumProjectBuilderTest
    extends PlexusTestCase
{
    public void testBuildingAProjectFromMetadataWithACompleteMaven1Pom()
        throws Exception
    {
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenOneContinuumProjectBuilder.ID );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( getTestFile( "src/test/resources/projects/maven-1.pom.xml" ).toURL() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        MavenOneProject project = (MavenOneProject) result.getProjects().get( 0 );

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/", project.getScmUrl() );

        assertEquals( "dev@maven.apache.org", project.getNagEmailAddress() );

        assertEquals( "1.1-SNAPSHOT", project.getVersion() );

//        Properties configuration = project.getConfiguration();
//
//        assertNotNull( configuration );
//
//        assertEquals( 1, configuration.size() );
//
//        assertEquals( "clean:clean jar:install", configuration.getProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS ) );
        assertEquals( "clean:clean jar:install", project.getGoals() );
    }
}
