/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StoreProjectAction
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws ContinuumException, ContinuumStoreException
    {
        ContinuumProject project = getUnvalidatedProject( context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = getStore().addProject( project );

        context.put( KEY_PROJECT_ID, project.getId() );

        // ----------------------------------------------------------------------
        // Set the working directory
        // ----------------------------------------------------------------------

        File projectWorkingDirectory = new File( getCore().getWorkingDirectory(), project.getId() );

        if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
        {
            throw new ContinuumException( "Could not make the working directory for the project " +
                                          "'" + projectWorkingDirectory.getAbsolutePath() + "'." );
        }

        // The working directory is created based on the project id so we can always
        // figure out what it is.

        project.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );

        project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );

        getStore().updateProject( project );
    }
}
