/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ValidateProject
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws Exception
    {
        ContinuumProject project = getUnvalidatedProject( context );

        // ----------------------------------------------------------------------
        // Make sure that the builder id is correct before starting to check
        // stuff out
        // ----------------------------------------------------------------------

        if ( !getBuildExecutorManager().hasBuildExecutor( project.getExecutorId() ) )
        {
            throw new ContinuumException( "No such executor with id '" + project.getExecutorId() + "'." );
        }

        // TODO: re-enable
//        if ( getStore().getProjectByName( project.getName() ) != null )
//        {
//            throw new ContinuumException( "A project with the name '" + project.getName() + "' already exist." );
//        }

//        if ( getProjectByScmUrl( scmUrl ) != null )
//        {
//            throw new ContinuumStoreException( "A project with the scm url '" + scmUrl + "' already exist." );
//        }

        // TODO: validate that the SCM provider id

        // ----------------------------------------------------------------------
        // Validate each field
        // ----------------------------------------------------------------------

        // This is not really validating but sanitizing.

        project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );
    }
}
