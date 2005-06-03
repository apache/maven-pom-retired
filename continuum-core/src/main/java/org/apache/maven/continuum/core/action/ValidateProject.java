/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.project.ContinuumProject;
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

    public void execute( Map context )
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
}
