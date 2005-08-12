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
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.project.ContinuumProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShellIntegrationTest
    extends AbstractIntegrationTest
{
    public static final String EOL = System.getProperty( "line.separator" );

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

        ContinuumProject p = new ContinuumProject();
        p.setScmUrl( "scm|cvs|local|" + getCvsRoot() + "|shell" );
        p.setName( "Shell Project" );
//        p.getNotifiers().add( makeMailNotifier( email ) );
        p.setVersion( "3.0" );

        BuildDefinition bd = new BuildDefinition();
        bd.setArguments( "" );
        bd.setBuildFile( getScriptName() );
        p.addBuildDefinition( bd );

        String projectId = continuum.addProject( p, ShellBuildExecutor.SHELL_EXECUTOR_ID );
        waitForSuccessfulCheckout( projectId );

        ContinuumProject project = continuum.getProject( projectId );
        assertProject( projectId, "Shell Project", "3.0", "", "shell", project );
        progress( "Building Shell project" );
        String buildId = buildProject( projectId, false ).getId();
        assertSuccessfulShellBuild( buildId, projectId, "" );

        // Test project reconfiguration
        // Test that a project will be built after a changed file is committed
        progress( "Building Shell project with alternative configuration" );

        File coDir = getTempCoDir();

        cvsCheckout( getCvsRoot(), "shell", coDir );

        addExtraPartInScript( root, coDir );

        cvsCommit( coDir );

        ContinuumProject shellProject = continuum.getProject( projectId );
        bd = (BuildDefinition) shellProject.getBuildDefinitions().iterator().next();
        bd.setArguments( "a b" );
        continuum.updateProject( shellProject );

        shellProject = continuum.getProject( projectId );
        // TODO: better way?
        bd = (BuildDefinition) shellProject.getBuildDefinitions().iterator().next();
        assertEquals( "Updated command line arguments doesn't match", "a b", bd.getArguments() );

        buildId = buildProject( projectId, false ).getId();
        assertSuccessfulShellBuild( buildId, projectId, "a" + EOL + "b" + EOL );

        removeProject( projectId );
    }

    private void initShellProject( File root )
        throws IOException, CommandLineException
    {
        deleteAndCreateDirectory( root );

        File script = new File( root, getScriptName() );

        FileUtils.fileWrite( script.getAbsolutePath(), getScriptContent() );

        if ( !System.getProperty( "os.name" ).startsWith( "Windows" ) ||
            "true".equals( System.getProperty( "cygwin" ) ) )
        {
            system( root, "chmod", "+x " + script.getAbsolutePath() );
        }
    }

    private String getScriptName()
    {
        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) &&
            !"true".equals( System.getProperty( "cygwin" ) ) )
        {
            return "script.bat";
        }
        else
        {
            return "script.sh";
        }
    }

    private String getScriptContent()
    {
        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) &&
            !"true".equals( System.getProperty( "cygwin" ) ) )
        {
            return "@ECHO OFF" + EOL + "IF \"%*\" == \"\" GOTO end" + EOL + "FOR %%a IN (%*) DO ECHO %%a" + EOL +
                ":end" + EOL;
        }
        else
        {
            return "#!/bin/bash" + EOL + "for arg in \"$@\"; do" + EOL + "  echo $arg" + EOL + "done";
        }
    }

    private void addExtraPartInScript( File rootDir, File coDir )
        throws Exception
    {
        File s = new File( coDir, getScriptName() );
        String script = FileUtils.fileRead( s );
        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) &&
            !"true".equals( System.getProperty( "cygwin" ) ) )
        {
            FileUtils.fileWrite( s.getAbsolutePath(), script + EOL + " REM Extra part" );
        }
        else
        {
            FileUtils.fileWrite( s.getAbsolutePath(), script + " # Extra part" );
            system( rootDir, "chmod", "+x " + s.getAbsolutePath() );
        }
    }
}
