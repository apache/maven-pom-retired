package org.apache.maven.continuum.execution.shell;

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
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ShellProject;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShellBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public final static String ID = "shell";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /** @requirement */
    private ShellCommandHelper shellCommandHelper;

    // ----------------------------------------------------------------------
    // ContinuumBuilder implementation
    // ----------------------------------------------------------------------

    public synchronized ContinuumBuildExecutionResult build( ContinuumProject p )
        throws ContinuumBuildExecutorException
    {
        ShellProject project = (ShellProject) p;

        File workingDirectory = new File( project.getWorkingDirectory() );

        ExecutionResult executionResult;

        String executable = project.getExecutable();

        if ( executable.charAt( 0 ) != '/' &&
             executable.charAt( 0 ) != '\\' )
        {
            executable = workingDirectory + File.separator + executable;
        }

        try
        {
            executionResult = shellCommandHelper.executeShellCommand( workingDirectory,
                                                                      executable,
                                                                      project.getCommandLineArguments() );
        }
        catch ( Exception e )
        {
            throw new ContinuumBuildExecutorException( "Error while executing shell command.", e );
        }

        return new ContinuumBuildExecutionResult( executionResult.getExitCode() == 0,
                                                  executionResult.getStandardOutput(),
                                                  executionResult.getStandardError(),
                                                  executionResult.getExitCode() );
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
    {
        // Not much to do.
    }
}
