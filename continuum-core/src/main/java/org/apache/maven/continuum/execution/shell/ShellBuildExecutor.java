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
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;

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

    public static final String ID = "shell";

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

    public synchronized ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition,
                                                             File buildOutput )
        throws ContinuumBuildExecutorException
    {
        // TODO: this should be validated earlier?
        String executable = buildDefinition.getBuildFile();

        if ( new File( executable ).isAbsolute() )
        {
            throw new ContinuumBuildExecutorException(
                "The shell script must be a relative path. It will be relative to the checkout." );
        }

        return executeShellCommand( project, executable, buildDefinition.getArguments(), buildOutput );
    }

    public void updateProjectFromCheckOut( File workingDirectory, Project project )
        throws ContinuumBuildExecutorException
    {
    }
}
