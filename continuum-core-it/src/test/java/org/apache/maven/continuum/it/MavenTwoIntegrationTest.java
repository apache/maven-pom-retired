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

import java.io.IOException;
import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoIntegrationTest
    extends AbstractIntegrationTest
{
    public void testBasic()
        throws Exception
    {
        Continuum continuum = getContinuum();

        initializeCvsRoot();

        progress( "Initializing Maven 2 CVS project" );

        File root = getItFile( "maven-two" );

        initMaven2Project( root, "maven-two" );

        progress( "Adding Maven 2 project" );

        String projectId = getProjectId( continuum.addMavenTwoProject( "file:" + root.getAbsolutePath() + "/pom.xml" ) );

        waitForSuccessfulCheckout( projectId );

        ContinuumProject project = continuum.getProject( projectId );

        assertProject( projectId,
                       "Maven 2 Project",
                       "2.0-SNAPSHOT",
                       "-N -B",
                       MavenTwoBuildExecutor.ID,
                       project );

        assertEquals( "project.notifiers.size", 2, project.getNotifiers().size() );

        //TODO: Activate this test when CONTINUUM-252 will be fixed
        //removeNotifier( projectId, ( (ContinuumNotifier) project.getNotifiers().get( 1 ) ).getType() );

        //assertEquals( "project.notifiers.size", 1, project.getNotifiers().size() );

        Map configuration = ((ContinuumNotifier) project.getNotifiers().get( 0 )).getConfiguration();

        assertEquals( "project.notifiers[1].configuration.size", 1, configuration.size() );

        assertEquals( "project.notifiers[1].configuration['address']", getEmail(), configuration.get( "address" ) );

        progress( "Building Maven 2 project" );

        String buildId = buildProject( projectId, false ).getId();

        assertSuccessfulMaven2Build( buildId );

        progress( "Test that a build without any files changed won't execute the executor" );

        int expectedSize = continuum.getBuildsForProject( projectId ).size();

        continuum.buildProject( projectId, false );

        Thread.sleep( 3000 );

        int actualSize = continuum.getBuildsForProject( projectId ).size();

        assertEquals( "A build has unexpectedly been executed.", expectedSize, actualSize );

        progress( "Test that a forced build without any files changed executes the executor" );

        buildId = buildProject( projectId, true ).getId();

        ContinuumBuild build = assertSuccessfulMaven2Build( buildId );

        assertTrue( "The 'build forced' flag wasn't true", build.isForced() );

        removeProject( projectId );
    }

    private void initMaven2Project( File basedir, String artifactId )
        throws IOException, CommandLineException
    {
        File cvsRoot = getCvsRoot();

        deleteAndCreateDirectory( basedir );

        FileUtils.fileWrite( new File( basedir, "pom.xml" ).getAbsolutePath(),
            "<project>\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>continuum</groupId>\n" +
            "  <artifactId>" + artifactId + "</artifactId>\n" +
            "  <version>2.0-SNAPSHOT</version>\n" +
            "  <name>Maven 2 Project</name>\n" +
            "  <ciManagement>\n" +
            "    <notifiers>\n" +
            "      <notifier>\n" +
            "        <type>mail</type>\n" +
            "        <configuration>\n" +
            "          <address>" + getEmail() + "</address>\n" +
            "        </configuration>\n" +
            "      </notifier>\n" +
            "      <notifier>\n" +
            "        <type>irc</type>\n" +
            "        <configuration>\n" +
            "          <host>irc.codehaus.org</host>\n" +
            "          <port>6667</port>\n" +
            "          <channel>#test</channel>\n" +
            "        </configuration>\n" +
            "      </notifier>\n" +
            "    </notifiers>\n" +
            "  </ciManagement>\n" +
            "  <scm>\n" +
            "    <connection>" + makeScmUrl( "cvs", cvsRoot, artifactId ) + "</connection>\n" +
            "  </scm>\n" +
            "</project>" );

        assertTrue( new File( basedir + "/src/main/java" ).mkdirs() );

        FileUtils.fileWrite( new File( basedir + "/src/main/java/Foo.java" ).getAbsolutePath(),
                             "class Foo { }" );

        cvsImport( basedir, artifactId, getCvsRoot() );
    }

    private void removeNotifier( String projectId, String notifierType )
    {
        try
        {
            getContinuum().removeNotifier( projectId, notifierType );
        }
        catch( Exception e )
        {
            e.printStackTrace();

            fail( "Unexpected exception after removing notifier '" + notifierType + "' for project '" + projectId );
        }
    }
}
