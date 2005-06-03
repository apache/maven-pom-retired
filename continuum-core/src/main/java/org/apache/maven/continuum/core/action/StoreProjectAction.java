/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.store.ContinuumStore;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StoreProjectAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.configuration
     *   default-value="${plexus.home}/temp"
     */
    private String workingDirectory;

    public void execute( Map context )
        throws Exception
    {
        ContinuumProject project = getProject( context );

        String projectId = store.addProject( project.getName(),
                                             project.getScmUrl(),
                                             project.getNagEmailAddress(),
                                             project.getVersion(),
                                             project.getCommandLineArguments(),
                                             project.getExecutorId(),
                                             null,
                                             project.getConfiguration() );

        // ----------------------------------------------------------------------
        // Set the working directory
        // ----------------------------------------------------------------------

        File projectWorkingDirectory = new File( workingDirectory, projectId );

        if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
        {
            throw new ContinuumException( "Could not make the working directory for the project " +
                                          "'" + projectWorkingDirectory.getAbsolutePath() + "'." );
        }

        project.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );
    }
}
