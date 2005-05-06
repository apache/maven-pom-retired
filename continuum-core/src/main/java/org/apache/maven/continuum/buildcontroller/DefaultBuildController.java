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
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.CollectionUtils;

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
    //
    // ----------------------------------------------------------------------

    private static class BuildContext
    {
        ContinuumProject project;

        ContinuumBuilder builder;

        boolean forced;

        UpdateScmResult scmResult;

        ContinuumBuildResult result;

        int state;

        Throwable cause;

        ContinuumBuild build;
    }

    // ----------------------------------------------------------------------
    // BuildController Implementation
    // ----------------------------------------------------------------------

    public void build( String projectId, boolean forced )
    {
        BuildContext context = new BuildContext();

        context.forced = forced;

        // ----------------------------------------------------------------------
        // Initialize the context
        // ----------------------------------------------------------------------

        // if these calls fail we're screwed anyway
        // and it will only be logged through the logger.

        try
        {
            context.project = store.getProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            getLogger().error( "Internal error while building the project.", ex );

            return;
        }

        try
        {
            context.builder = builderManager.getBuilder( context.project.getBuilderId() );
        }
        catch ( ContinuumException e )
        {
            getLogger().fatalError( "Error while getting builder '" + context.project.getBuilderId() + "'. " +
                                    "Project Id: '" + projectId + "'.", e );

            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        try
        {
            notifier.buildStarted( context.project );

            // ----------------------------------------------------------------------
            // Update the project
            //
            // If this fails, create a build, store the scm result and set the
            // build status to error.
            // ----------------------------------------------------------------------

            if ( !update( context ) )
            {
                return;
            }

            if ( !updateProjectMetadata( context ) )
            {
                return;
            }

            // ----------------------------------------------------------------------
            // Figure out if the project needs to be built
            // ----------------------------------------------------------------------

            if ( context.scmResult.getUpdatedFiles().size() == 0 &&
                 !forced &&
                 !isNew( context.project ) )
            {
                getLogger().info( "No files updated, not building. Project id '" + context.project.getId() + "'." );

                store.setBuildNotExecuted( projectId );

                return;
            }

            makeBuild( context );

            buildProject( context );
        }
        catch ( ContinuumStoreException ex )
        {
            if ( !Thread.interrupted() )
            {
                getLogger().error( "Internal error while building the project.", ex );
            }

            return;
        }
        finally
        {
            notifier.buildComplete( context.project, context.build );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private boolean update( BuildContext context )
        throws ContinuumStoreException
    {
        try
        {
            store.setIsUpdating( context.project.getId() );

            notifier.checkoutStarted( context.project );

            context.scmResult = scm.updateProject( context.project );

            store.setUpdateDone( context.project.getId() );

            return true;
        }
        catch ( ContinuumScmException e )
        {
            getLogger().fatalError( "Error while updating from SCM. Project id '" + context.project.getId() + "'." );

            makeAndSetErrorBuildResult( context, e );

            return false;
        }
        finally
        {
            notifier.checkoutComplete( context.project, context.scmResult );
        }
    }

    private boolean updateProjectMetadata( BuildContext context )
        throws ContinuumStoreException
    {
        // TODO: Update the metadata files and then update the project descriptor
        // before updating the project itself. This will make it possible to migrate
        // a project from one SCM to another.

        ContinuumBuilder builder = context.builder;

        ContinuumProject project = context.project;

        File workingDirectory = new File( project.getWorkingDirectory() );

        try
        {
            builder.updateProjectFromCheckOut( workingDirectory, project );
        }
        catch ( ContinuumException e )
        {
            getLogger().fatalError( "Error while updating project metadata from check out.", e );

            makeAndSetErrorBuildResult( context, e );

            return false;
        }

        String projectId = project.getId();

        store.updateProject( projectId,
                             project.getName(),
                             project.getScmUrl(),
                             project.getNagEmailAddress(),
                             project.getVersion() );

//            store.updateProjectConfiguration( projectId, project.getConfiguration() );

        return true;
    }

    private void buildProject( BuildContext context )
        throws ContinuumStoreException
    {
        try
        {
            notifier.runningGoals( context.project, context.build );

            context.result = context.builder.build( context.project );

            if ( context.result.isSuccess() )
            {
                context.state = ContinuumProjectState.OK;
            }
            else
            {
                context.state = ContinuumProjectState.FAILED;
            }

            setBuildResult( context.build.getId(), context.state, context.result, context.scmResult, null );
        }
        catch ( Throwable ex )
        {
            getLogger().fatalError( "Error building the project, project id: '" + context.project.getId() + "'.", ex );

            makeAndSetErrorBuildResult( context, ex );
        }
        finally
        {
            notifier.goalsCompleted( context.project, context.build );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void makeAndSetErrorBuildResult( BuildContext context, Throwable e )
        throws ContinuumStoreException
    {
        makeBuild( context );

        context.result = new ContinuumBuildResult();

        context.result.setSuccess( false );

        context.state = ContinuumProjectState.ERROR;

        setBuildResult( context.build.getId(), context.state, context.result, context.scmResult, e );
    }

    private void setBuildResult( String buildId, int state, ContinuumBuildResult result, UpdateScmResult scmResult,
                                 Throwable e )
        throws ContinuumStoreException
    {
        System.err.println( "Setting the build id '" + buildId + "' state to " + state );

        store.setBuildResult( buildId, state, result, scmResult, e );
    }

    private void makeBuild( BuildContext context )
        throws ContinuumStoreException
    {
        String buildId = store.createBuild( context.project.getId(), context.forced );

        getLogger().info( "Build id: '" + buildId + "'." );

        context.build = store.getBuild( buildId );

        context.build.setUpdateScmResult( context.scmResult );
    }

    // Check to see if there is only a single build in the builds list.
    public boolean isNew( ContinuumProject project )
        throws ContinuumStoreException
    {
        Iterator it = store.getBuildsForProject( project.getId(), 0, 0 );

        return CollectionUtils.iteratorToList( it ).size() == 0;
    }
}
