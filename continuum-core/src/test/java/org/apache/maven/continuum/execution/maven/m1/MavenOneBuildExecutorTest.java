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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneBuildExecutorTest
    extends AbstractContinuumTest
{
    public void testUpdatingAProjectFromScmWithAExistingProjectAndAEmptyMaven1Pom()
        throws Exception
    {
        BuildExecutorManager builderManager = (BuildExecutorManager) lookup( BuildExecutorManager.ROLE );

        MavenOneBuildExecutor executor = (MavenOneBuildExecutor) builderManager.getBuildExecutor(
            MavenOneBuildExecutor.ID );

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

        Project project = new Project();

        project.setName( "Maven" );

        project.setGroupId( "org.apache.maven" );

        project.setArtifactId( "maven" );

        project.setScmUrl( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/" );

        ProjectNotifier notifier = new ProjectNotifier();

        Properties props = new Properties();

        props.put( "address", "dev@maven.apache.org" );

        notifier.setConfiguration( props );

        List notifiers = new ArrayList();

        notifiers.add( notifier );

        project.setNotifiers( notifiers );

        project.setVersion( "1.1-SNAPSHOT" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        executor.updateProjectFromCheckOut( checkOut, project, null );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( project );

        assertEquals( "Maven", project.getName() );

        assertEquals( "scm:svn:http://svn.apache.org/repos/asf:maven/maven-1/core/trunk/", project.getScmUrl() );

        ProjectNotifier actualNotifier = (ProjectNotifier) project.getNotifiers().get( 0 );

        assertEquals( "dev@maven.apache.org", actualNotifier.getConfiguration().get( "address" ) );

        assertEquals( "1.1-SNAPSHOT", project.getVersion() );
    }
}
