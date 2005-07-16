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

import java.io.File;
import java.io.IOException;

import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.Continuum;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShellIntegrationTest
    extends AbstractIntegrationTest
{
    public final static String EOL = System.getProperty( "line.separator" );

    public void testBasic()
        throws Exception
    {
        Continuum continuum = getContinuum();

        initializeCvsRoot();

        progress( "Initializing Shell CVS project" );

        File root = getItFile( "shell" );

        initShellProject( root );

        cvsImport( root, "shell", getCvsRoot() );

        progress( "Adding CVS Shell project" );

        ShellProject p = new ShellProject();
        p.setScmUrl( "scm:cvs:local:" + getCvsRoot() + ":shell" );
        p.setName( "Shell Project" );
//        p.getNotifiers().add( makeMailNotifier( email ) );
        p.setVersion( "3.0" );
        p.setCommandLineArguments( "" );
        p.setExecutable( "script.sh" );
        String projectId = continuum.addShellProject( p );
        waitForSuccessfulCheckout( projectId );

        ContinuumProject project = continuum.getProject( projectId );
        assertProject( projectId, "Shell Project", "3.0", "", "shell", project );
        progress( "Building Shell project" );
        String buildId = buildProject( projectId, false ).getId();
        assertSuccessfulShellBuild( buildId, "" );

        // Test project reconfiguration
        // Test that a project will be built after a changed file is committed
        progress( "Building Shell project with alternative configuration" );

        File coDir = getTempCoDir();

        cvsCheckout( getCvsRoot(), "shell", coDir );

        File s = new File( coDir, "script.sh" );
        String script = FileUtils.fileRead( s );
        FileUtils.fileWrite( s.getAbsolutePath(), script + " # Extra part" );
        system( root, "chmod", "+x " + s.getAbsolutePath() );

        cvsCommit( coDir );

        ShellProject shellProject = continuum.getShellProject( projectId );
        shellProject.setCommandLineArguments( "a b" );
        continuum.updateShellProject( shellProject );

        shellProject = continuum.getShellProject( projectId );
        assertEquals( "Updated command line arguments doesn't match", "a b", shellProject.getCommandLineArguments() );

        buildId = buildProject( projectId, false ).getId();
        assertSuccessfulShellBuild( buildId, "a" + EOL +
                                             "b" + EOL );
        removeProject( projectId );
    }

    private void initShellProject( File root )
        throws IOException, CommandLineException
    {
        deleteAndCreateDirectory( root );

        File script = new File( root, "script.sh" );

        FileUtils.fileWrite( script.getAbsolutePath(),
                             "#!/bin/bash" + EOL +
                             "for arg in \"$@\"; do" + EOL +
                             "  echo $arg" + EOL +
                             "done");

        system( root, "chmod", "+x " + script.getAbsolutePath() );
    }
}
