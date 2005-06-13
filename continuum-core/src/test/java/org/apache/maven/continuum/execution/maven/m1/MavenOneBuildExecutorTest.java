package org.apache.maven.continuum.execution.maven.m1;

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

import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.project.MavenOneProject;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneBuildExecutorTest
    extends PlexusTestCase
{
    public void testUpdatingAProjectFromScmWithAExistingProjectAndAEmptyMaven1Pom()
        throws Exception
    {
        BuildExecutorManager builderManager = (BuildExecutorManager) lookup( BuildExecutorManager.ROLE );

        MavenOneBuildExecutor executor = (MavenOneBuildExecutor) builderManager.getBuildExecutor( MavenOneBuildExecutor.ID );

        // ----------------------------------------------------------------------
        // Make a checkout
        // ----------------------------------------------------------------------

        File checkOut = getTestFile( "target/test-checkout" );

        if ( !checkOut.exists() )
        {
            assertTrue( checkOut.mkdirs() );
        }

        FileUtils.cleanDirectory( checkOut );

        FileUtils.fileWrite( new File( checkOut, "project.xml" ).getAbsolutePath(), "<project/>" );

        // ----------------------------------------------------------------------
        // Make the "existing" project
        // ----------------------------------------------------------------------

        MavenOneProject project = new MavenOneProject();

        project.setName( "Maven" );

        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/" );

        project.setNagEmailAddress( "dev@maven.apache.org" );

        project.setVersion( "1.1-SNAPSHOT" );

        project.setGoals( "clean:clean jar:install" );

//        Properties expectedConfiguration = new Properties();
//
//        expectedConfiguration.put( MavenOneBuildExecutor.CONFIGURATION_GOALS, "clean:clean jar:install" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

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
