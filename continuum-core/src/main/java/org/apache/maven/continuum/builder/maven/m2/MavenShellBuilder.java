package org.apache.maven.continuum.builder.maven.m2;

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
import org.apache.maven.continuum.builder.ContinuumBuilder;
import org.apache.maven.continuum.builder.shell.ExecutionResult;
import org.apache.maven.continuum.builder.shell.ShellCommandHelper;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: MavenShellBuilder.java,v 1.2 2005/04/07 23:27:39 trygvis Exp $
 */
public class MavenShellBuilder
    extends AbstractContinuumBuilder
    implements ContinuumBuilder
{
    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = "maven2";

    /** @requirement */
    private ShellCommandHelper shellCommandHelper;

    /** @requirement */
    private MavenBuilderHelper builderHelper;

    /** @configuration */
    private String executable;

    /** @configuration */
    private String arguments;

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildResult build( ContinuumProject project )
        throws ContinuumException
    {
        File workingDirectory = new File( project.getWorkingDirectory() );

        ExecutionResult executionResult;

        String[] arguments = getArguments( project );

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

        MavenTwoBuildResult result = new MavenTwoBuildResult();

        result.setSuccess( success );

        result.setStandardOutput( executionResult.getStandardOutput() );

        result.setStandardError( executionResult.getStandardError() );

        result.setExitCode( executionResult.getExitCode() );

        return result;
    }

    public ContinuumProject createProjectFromMetadata( URL metadata )
        throws ContinuumException
    {
        return builderHelper.createProjectFromMetadata( metadata );
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumException
    {
        builderHelper.updateProjectFromMetadata( workingDirectory, project );
    }

    protected String[] getArguments( ContinuumProject project )
        throws ContinuumException
    {
        String[] a = splitAndTrimString( this.arguments, " " );

        String[] goals = getConfigurationStringArray( project.getConfiguration(), CONFIGURATION_GOALS, "," );

        String[] arguments = new String[ a.length + goals.length ];

        System.arraycopy( a, 0, arguments, 0, a.length );

        System.arraycopy( goals, 0, arguments, a.length, goals.length );

        return arguments;
    }
}
