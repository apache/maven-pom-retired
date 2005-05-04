package org.apache.maven.continuum.buildcontroller;

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

import java.io.File;
import java.util.Iterator;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builder.ContinuumBuilder;
import org.apache.maven.continuum.builder.manager.BuilderManager;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: DefaultBuildController.java,v 1.4 2005/04/07 23:27:38 trygvis Exp $
 */
public class DefaultBuildController
    extends AbstractLogEnabled
    implements BuildController
{
    /** @requirement */
    private BuilderManager builderManager;

    /** @requirement */
    private ContinuumStore store;

    /** @requirement */
    private ContinuumNotificationDispatcher notifier;

    /** @requirement */
    private ContinuumScm scm;

    // ----------------------------------------------------------------------
    // BuildController Implementation
    // ----------------------------------------------------------------------

    public void build( String buildId )
    {
        ContinuumProject project;

        ContinuumBuild build;

        try
        {
            project = store.getProjectByBuild( buildId );

            build = store.getBuild( buildId );
        }
        catch ( ContinuumStoreException ex )
        {
            getLogger().error( "Internal error while building the project.", ex );

            return;
        }

        try
        {
            notifier.buildStarted( build );

            buildProject( project, build );
        }
        catch ( ContinuumException ex )
        {
            if ( !Thread.interrupted() )
            {
                getLogger().error( "Internal error while building the project.", ex );
            }

            return;
        }
        finally
        {
            // Reload the build as setBuildResult() will update the build
            try
            {
                build = store.getBuild( buildId );

                notifier.buildComplete( build );
            }
            catch ( ContinuumStoreException e )
            {
                getLogger().error( "Error while loading the build. id: '" + buildId + "'." );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * This method shall not throw any exceptions unless there
     * is something internally wrong. It shall NOT throw a exception when a build fails.
     *
     * @param project
     * @param build
     * @throws ContinuumException
     */
    private void buildProject( ContinuumProject project, ContinuumBuild build )
        throws ContinuumException
    {
        // if these calls fail we're screwed anyway
        // and it will only be logged through the logger.

        ContinuumBuilder builder = builderManager.getBuilder( project.getBuilderId() );

        int state = -1;

        ContinuumBuildResult result = null;

        Throwable error = null;

        // ----------------------------------------------------------------------
        // Build the project
        // ----------------------------------------------------------------------

        try
        {
            result = build( builder, build );

            if ( result.isSuccess() )
            {
                state = ContinuumProjectState.OK;
            }
            else
            {
                state = ContinuumProjectState.FAILED;
            }
        }
        catch ( Throwable ex )
        {
            getLogger().fatalError( "Error building the project, build id: '" + build.getId() + "'.", ex );

            error = ex;

            state = ContinuumProjectState.ERROR;
        }

        // ----------------------------------------------------------------------
        // Store the result
        // ----------------------------------------------------------------------

        try
        {
            store.setBuildResult( build.getId(), state, result, error );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while setting the build result.", e );
        }
    }

    private ContinuumBuildResult build( ContinuumBuilder builder, ContinuumBuild build )
        throws Exception
    {
        ContinuumProject project = store.getProjectByBuild( build.getId() );

        // TODO: Update the metadata files and then update the project descriptor
        // before updating the project itself. This will make it possible to migrate
        // a project from one SCM to another.

        UpdateScmResult scmResult;

        try
        {
            store.setIsUpdating( build.getId() );

            notifier.checkoutStarted( build );

            scmResult = scm.updateProject( project );

            store.setUpdateDone( build.getId(), scmResult );
        }
        finally
        {
            notifier.checkoutComplete( build );
        }

        ContinuumBuildResult result;

        // ----------------------------------------------------------------------
        // Build the project if
        // * there was any updated files
        // * the project is new (never been built before)
        // * the build is "forced"
        // ----------------------------------------------------------------------

        if ( scmResult.getUpdatedFiles().size() > 0 ||
             isNew( project ) ||
             build.isForced() )
        {
            File workingDirectory = new File( project.getWorkingDirectory() );

            builder.updateProjectFromCheckOut( workingDirectory, project );

            String projectId = project.getId();

            store.updateProject( projectId,
                                 project.getName(),
                                 project.getScmUrl(),
                                 project.getNagEmailAddress(),
                                 project.getVersion() );

//            store.updateProjectConfiguration( projectId, project.getConfiguration() );

            try
            {
                notifier.runningGoals( build );

                result = runGoals( builder, project );

                if ( result == null )
                {
                    return null;
                }

                result.setBuildExecuted( true );
            }
            finally
            {
                notifier.goalsCompleted( build );
            }
        }
        else
        {
            getLogger().info( "No files updated, not building. Build id '" + build.getId() + "'." );

            result = new ContinuumBuildResult();

            result.setSuccess( true );

            result.setBuildExecuted( false );
        }

        return result;
    }

    private ContinuumBuildResult runGoals( ContinuumBuilder builder, ContinuumProject project )
        throws ContinuumException
    {
        ContinuumBuildResult result = builder.build( project );

        if ( result == null )
        {
            getLogger().fatalError( "Internal error: the builder returned null." );
        }

        return result;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // Check to see if there is only a single build in the builds list.
    public boolean isNew( ContinuumProject project )
        throws ContinuumStoreException
    {
        Iterator it = store.getBuildsForProject( project.getId(), 0, 0 );

        if ( !it.hasNext() )
        {
            return true;
        }

        it.next();

        if ( it.hasNext() )
        {
            return false;
        }

        return true;
    }
}
