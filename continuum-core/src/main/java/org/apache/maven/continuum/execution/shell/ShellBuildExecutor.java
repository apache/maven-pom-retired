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

import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ShellProject;

import java.io.File;

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

    public ShellBuildExecutor()
    {
        super( ID, false );
    }

    // ----------------------------------------------------------------------
    // ContinuumBuilder implementation
    // ----------------------------------------------------------------------

    public synchronized ContinuumBuildExecutionResult build( ContinuumProject p, File buildOutput )
        throws ContinuumBuildExecutorException
    {
        ShellProject project = (ShellProject) p;

        File workingDirectory = new File( project.getWorkingDirectory() );

        String executable = project.getExecutable();

        return executeShellCommand( workingDirectory,
                                    executable,
                                    project.getCommandLineArguments(),
                                    buildOutput );
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject p )
        throws ContinuumBuildExecutorException
    {
        ShellProject project = (ShellProject) p;

        String executable = project.getExecutable();

        if ( new File( executable ).isAbsolute() )
        {
            throw new ContinuumBuildExecutorException( "The shell script must be a relative path. " +
                                                       "It will be relative to the checkout" );
        }
    }
}
