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

import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ContinuumStore;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ValidateProject
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement
     */
    private ContinuumCore core;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    protected void doExecute( Map context )
        throws Exception
    {
        ContinuumProject project = getProject( context );

        // ----------------------------------------------------------------------
        // Make sure that the builder id is correct before starting to check
        // stuff out
        // ----------------------------------------------------------------------

        if ( !buildExecutorManager.hasBuildExecutor( project.getExecutorId() ) )
        {
            throw new ContinuumException( "No such executor with id '" + project.getExecutorId() + "'." );
        }

        if ( store.getProjectByName( project.getName() ) != null )
        {
            throw new ContinuumException( "A project with the name '" + project.getName() + "' already exist." );
        }

//        if ( getProjectByScmUrl( scmUrl ) != null )
//        {
//            throw new ContinuumStoreException( "A project with the scm url '" + scmUrl + "' already exist." );
//        }

        // ----------------------------------------------------------------------
        // Validate each field
        // ----------------------------------------------------------------------

        project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );
    }

    protected void handleException( Throwable throwable )
        throws ContinuumStoreException
    {
    }
}
