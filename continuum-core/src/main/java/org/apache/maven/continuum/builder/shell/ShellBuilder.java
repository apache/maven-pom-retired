package org.apache.maven.continuum.builder.shell;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builder.AbstractContinuumBuilder;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ShellBuilder.java,v 1.2 2005/04/07 23:27:40 trygvis Exp $
 */
public class ShellBuilder
    extends AbstractContinuumBuilder
{
    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public final static String CONFIGURATION_ARGUMENTS = "arguments";

    /** @requirement */
    private ShellCommandHelper shellCommandHelper;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected boolean prependWorkingDirectoryIfMissing()
    {
        return true;
    }

    protected String getExecutable( ContinuumProject project )
        throws ContinuumException
    {
        return getConfigurationString( project.getConfiguration(), CONFIGURATION_EXECUTABLE );
    }

    protected String[] getArguments( ContinuumProject project )
        throws ContinuumException
    {
        return getConfigurationStringArray( project.getConfiguration(), CONFIGURATION_ARGUMENTS, " ", new String[ 0 ] );
    }

    // ----------------------------------------------------------------------
    // ContinuumBuilder implementation
    // ----------------------------------------------------------------------

    public synchronized ContinuumBuildResult build( ContinuumProject project )
        throws ContinuumException
    {
        File workingDirectory = new File( project.getWorkingDirectory() );

        ExecutionResult executionResult;

        String executable = getExecutable( project );

        String[] arguments = getArguments( project );

        if ( !executable.startsWith( "/" ) &&
             !executable.startsWith( "\\" ) &&
             prependWorkingDirectoryIfMissing() )
        {
            executable = workingDirectory + File.separator + executable;
        }

        try
        {
            executionResult = shellCommandHelper.executeShellCommand( workingDirectory,
                                                                      executable,
                                                                      arguments );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while executing shell command.", e );
        }

        boolean success = executionResult.getExitCode() == 0;

        ShellBuildResult result = new ShellBuildResult();

        result.setSuccess( success );

        result.setStandardOutput( executionResult.getStandardOutput() );

        result.setStandardError( executionResult.getStandardError() );

        result.setExitCode( executionResult.getExitCode() );

        return result;
    }

    public ContinuumProject createProjectFromMetadata( URL metadata )
        throws ContinuumException
    {
        throw new ContinuumException( "The Ant builder cannot create metadata from a URL." );
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumException
    {
        // Not much to do.
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected Commandline createCommandline( ContinuumProject project, String executable, String[] arguments )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( executable );

        cl.setWorkingDirectory( new File( project.getWorkingDirectory() ).getAbsolutePath() );

        for ( int i = 1; i < arguments.length; i++ )
        {
            cl.createArgument().setValue( arguments[i] );
        }

        getLogger().warn( "Executing external command '" + executable + "'." );

        getLogger().warn( "Executing external command. Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        return cl;
    }
}
