package org.apache.maven.continuum.execution.maven.m2;

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

import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.shell.ExecutionResult;
import org.apache.maven.continuum.execution.shell.ShellCommandHelper;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenTwoProject;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = "maven2";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /** @requirement */
    private ShellCommandHelper shellCommandHelper;

    /** @requirement */
    private MavenBuilderHelper builderHelper;

    /** @configuration */
    private String executable;

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildResult build( ContinuumProject p )
        throws ContinuumBuildExecutorException
    {
        MavenTwoProject project = (MavenTwoProject) p;

        File workingDirectory = new File( project.getWorkingDirectory() );

        ExecutionResult executionResult;

        try
        {
            String arguments = project.getCommandLineArguments() + " " + project.getGoals();

            executionResult = shellCommandHelper.executeShellCommand( workingDirectory,
                                                                      executable,
                                                                      arguments );
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildExecutorException( "Error while executing shell command.", e );
        }

        boolean success = executionResult.getExitCode() == 0;

        MavenTwoBuildResult result = new MavenTwoBuildResult();

        result.setSuccess( success );

        result.setStandardOutput( executionResult.getStandardOutput() );

        result.setStandardError( executionResult.getStandardError() );

        result.setExitCode( executionResult.getExitCode() );

        return result;
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumBuildExecutorException
    {
        File f = new File( workingDirectory, "pom.xml" );

        try
        {
            builderHelper.mapMetadataToProject( f, (MavenTwoProject) project );
        }
        catch ( MavenBuilderHelperException e )
        {
            throw new ContinuumBuildExecutorException( "Error while mapping metadata.", e );
        }
    }
}
