/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;

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
        throws Exception
    {
        ContinuumProject project = getUnvalidatedProject( context );

        File workingDirectory = getWorkingDirectory( context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String projectId = getStore().addProject( project );

        project = getStore().getProject( projectId );

        context.put( KEY_PROJECT_ID, projectId );

        // ----------------------------------------------------------------------
        // Set the working directory
        // ----------------------------------------------------------------------

        File projectWorkingDirectory = new File( workingDirectory, projectId );

        if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
        {
            throw new ContinuumException( "Could not make the working directory for the project " +
                                          "'" + projectWorkingDirectory.getAbsolutePath() + "'." );
        }

        // The working directory is created based on the project id so we can always
        // figure out what it is.

        project.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );

        getStore().updateProject( project );

//        getStore().setWorkingDirectory( projectId, projectWorkingDirectory.getAbsolutePath() );
    }
}
