package org.apache.maven.continuum.it;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneIntegrationTest
    extends AbstractIntegrationTest
{
    public void testBasic()
        throws Exception
    {
        Continuum continuum = getContinuum();

        initializeCvsRoot();

        progress( "Initializing Maven 1 CVS project" );

        File root = getItFile( "maven-1" );

        initMaven1Project( root, "maven-1", "cvs", getCvsRoot() );

        progress( "Adding Maven 1 project" );

        int projectId = getProjectId( continuum.addMavenOneProject( getFileUrl( root ) + "/project.xml" ) );

        waitForSuccessfulCheckout( projectId );

        Project project = continuum.getProjectWithCheckoutResult( projectId );

        assertProject( projectId, "Maven 1 Project", "1.0", "", MavenOneBuildExecutor.ID, project );

        assertCheckedOutFiles( project, new String[]{"/project.xml", "/src/main/java/Foo.java"} );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        progress( "Building Maven 1 project" );

        int buildId = buildProject( projectId, false ).getId();

        assertSuccessfulMaven1Build( buildId, projectId );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        progress( "Testing that the POM is updated before each build." );

        File coDir = getTempCoDir();

        cleanDirectory( coDir );

        cvsCheckout( getCvsRoot(), "maven-1", coDir );

        File projectXml = new File( coDir, "/project.xml" );

        String pom = FileUtils.fileRead( projectXml );

        pom = pom.replaceAll( "Maven 1 Project", "Maven 1 Project - Changed" );

        pom = pom.replaceAll( "1.0", "1.1" );

        FileUtils.fileWrite( projectXml.getAbsolutePath(), pom );

        cvsCommit( coDir );

//#c.updateProjectFromScm( maven1.id )
//#maven1 = c.getProject( maven1.id )
//#assertEquals( "The project name wasn't changed.", "Maven 1 Project - Changed", maven1.name )
//#assertEquals( "The project version wasn't changed.", "1.1", maven1.version )

        removeProject( project.getId() );
    }

    private void initMaven1Project( File root, String artifactId, String scm, File scmRoot )
        throws IOException, CommandLineException
    {
        deleteAndCreateDirectory( root );

        writeMavenOnePom( new File( root, "/project.xml" ), artifactId, makeScmUrl( scm, scmRoot, artifactId ),
                          getEmail() );

        assertTrue( new File( root, "/src/main/java" ).mkdirs() );

        PrintWriter writer = new PrintWriter( new FileWriter( new File( root, "/src/main/java/Foo.java" ) ) );
        writer.write( "class Foo { }" );
        writer.close();

        scmImport( root, artifactId, scm, scmRoot );
    }
}
