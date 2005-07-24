package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ContinuumStore;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StoreProjectGroupAction
    extends AbstractContinuumAction
{
    private ContinuumStore store;

    public void execute( Map context )
        throws ContinuumException, ContinuumStoreException
    {
        ContinuumProjectGroup projectGroup = getUnvalidatedProjectGroup( context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        projectGroup = store.addProjectGroup( projectGroup );

        context.put( KEY_PROJECT_GROUP_ID, projectGroup.getId() );

        // ----------------------------------------------------------------------
        // Set the working directory
        // ----------------------------------------------------------------------

        File projectWorkingDirectory = new File( getWorkingDirectory( context ), projectGroup.getId() );

        if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
        {
            throw new ContinuumException( "Could not make the working directory for the project " +
                                          "'" + projectWorkingDirectory.getAbsolutePath() + "'." );
        }

        // The working directory is created based on the project id so we can always
        // figure out what it is.

        projectGroup.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );

        projectGroup = store.updateProjectGroup( projectGroup );

        context.put( KEY_UNVALIDATED_PROJECT_GROUP, projectGroup );
    }
}
