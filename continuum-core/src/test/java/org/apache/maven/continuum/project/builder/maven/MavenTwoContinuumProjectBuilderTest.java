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

import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

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
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

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
        ContinuumProjectBuilder projectBuilder = (ContinuumProjectBuilder) lookup( ContinuumProjectBuilder.ROLE, MavenTwoContinuumProjectBuilder.ID );

        File pom = getTestFile( "src/test/repository/maven-builder-helper-2.xml" );

        ContinuumProjectBuildingResult result = projectBuilder.createProjectsFromMetadata( pom.toURL() );

        assertNotNull( result.getProjects() );

        assertEquals( 1, result.getProjects().size() );

        MavenTwoProject project = (MavenTwoProject) result.getProjects().get( 0 );

        assertEquals( "foo@bar", project.getNagEmailAddress() );
    }
}
