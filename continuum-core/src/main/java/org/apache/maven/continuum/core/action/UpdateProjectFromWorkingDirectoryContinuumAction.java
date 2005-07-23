package org.apache.maven.continuum.core.action;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.store.ContinuumStoreException;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UpdateProjectFromWorkingDirectoryContinuumAction
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws ContinuumStoreException, ContinuumException, ContinuumBuildExecutorException
    {
        ContinuumProject project = getProject( context );

        getLogger().info( "Updating project '" + project.getName() + "' from checkout." );

        // ----------------------------------------------------------------------
        // Make a new descriptor
        // ----------------------------------------------------------------------

        ContinuumBuildExecutor builder = getBuildExecutorManager().getBuildExecutor( project.getExecutorId() );

        builder.updateProjectFromCheckOut( new File( project.getWorkingDirectory() ), project );

        // ----------------------------------------------------------------------
        // Store the new descriptor
        // ----------------------------------------------------------------------

        getStore().updateProject( project );
    }
}
