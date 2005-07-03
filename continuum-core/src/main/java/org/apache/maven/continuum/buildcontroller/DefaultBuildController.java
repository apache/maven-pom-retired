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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultBuildController
    extends AbstractLogEnabled
    implements BuildController
{
    /** @plexus.requirement */
    private ContinuumStore store;

    /** @plexus.requirement */
    private ContinuumNotificationDispatcher notifier;

    /** @plexus.requirement */
    private ActionManager actionManager;

    // ----------------------------------------------------------------------
    // BuildController Implementation
    // ----------------------------------------------------------------------

    public void build( String projectId, boolean forced )
    {
        long startTime = System.currentTimeMillis();

        // ----------------------------------------------------------------------
        // Initialize the context
        // ----------------------------------------------------------------------

        // if these calls fail we're screwed anyway
        // and it will only be logged through the logger.

        ContinuumProject project;

        String buildId = null;

        try
        {
            project = store.getProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            getLogger().error( "Internal error while building the project.", ex );

            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        try
        {
            notifier.buildStarted( project );

            Map actionContext = new HashMap();

            actionContext.put( AbstractContinuumAction.KEY_PROJECT_ID, projectId );

            actionContext.put( AbstractContinuumAction.KEY_FORCED, Boolean.valueOf( forced ) );

            UpdateScmResult scmResult = null;

            try
            {
                actionManager.lookup( "update-working-directory-from-scm" ).execute( actionContext );

                scmResult = (UpdateScmResult) actionContext.get( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT );

                actionManager.lookup( "update-project-from-working-directory" ).execute( actionContext );

                actionManager.lookup( "execute-builder" ).execute( actionContext );

                buildId = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );
            }
            catch ( Throwable e )
            {
                buildId = makeAndSetErrorBuildResult( store.getProject( projectId ),
                                                      scmResult,
                                                      startTime,
                                                      forced, e );
            }

/////////////////////// This section should delegate to UpdateWorkingDirectoryFromScmContinuumAction
/*
/////////////////////// START SECTION
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
/////////////////////// END SECTION
*/
/*
/////////////////////// This section should delegate to UpdateProjectFromWorkingDirectoryContinuumAction
/////////////////////// START SECTION
            if ( !updateProjectMetadata( context ) )
            {
                return;
            }
/////////////////////// END SECTION
*/
/*
/////////////////////// This section should delegate to ExecuteBuilderContinuumAction
/////////////////////// START SECTION

            // ----------------------------------------------------------------------
            // Figure out if the project needs to be built
            // ----------------------------------------------------------------------

            if ( context.scmResult.getUpdatedFiles().size() == 0 &&
                 !forced &&
                 !isNew( context.project ) )
            {
                getLogger().info( "No files updated, not building. Project id '" + context.project.getId() + "'." );

//                Nothing to do, the project doesn't contain any state anymore
//                store.setBuildNotExecuted( projectId );

                return;
            }

            makeBuild( context );

            buildProject( context );

/////////////////////// END SECTION
*/
        }
        catch ( Exception ex )
        {
            if ( !Thread.interrupted() )
            {
                getLogger().error( "Internal error while building the project.", ex );
            }
        }
        finally
        {
            ContinuumBuild build = null;

            if ( buildId != null )
            {
                try
                {
                    build = store.getBuild( buildId );
                }
                catch ( ContinuumStoreException e )
                {
                    // ignore
                }
            }

            notifier.buildComplete( project, build );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------
/*
    private boolean update( BuildContext context )
        throws ContinuumStoreException
    {
        try
        {
// No state in the project
//            store.setIsUpdating( context.project.getId() );

            notifier.checkoutStarted( context.project );

            context.scmResult = scm.updateProject( context.project );

// No state in the project
//            store.setUpdateDone( context.project.getId() );

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
*/
/*
    private boolean updateProjectMetadata( BuildContext context )
        throws ContinuumStoreException
    {
        // TODO: Update the metadata files and then update the project descriptor
        // before updating the project itself. This will make it possible to migrate
        // a project from one SCM to another.

        ContinuumProject project = context.project;

        try
        {
            continuum.updateProjectFromScm( project.getId() );
        }
        catch ( ContinuumException e )
        {
            getLogger().fatalError( "Error while updating project metadata from check out.", e );

            makeAndSetErrorBuildResult( context, e );

            return false;
        }

        return true;
    }
*/
/*
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

            setBuildResult( context.build.getId(),
                            context.state,
                            context.result,
                            context.scmResult,
                            null );
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
*/
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String makeAndSetErrorBuildResult( ContinuumProject project,
                                             UpdateScmResult scmResult,
                                             long startTime,
                                             boolean forced,
                                             Throwable e )
        throws ContinuumStoreException
    {
        getLogger().error( "Error while building project.", e );

        ContinuumBuild build = new ContinuumBuild();

        build.setState( ContinuumProjectState.ERROR );

        build.setForced( forced );

        build.setStartTime( startTime );

        build.setEndTime( System.currentTimeMillis() );

        build.setError( throwableToString( e ) );

        build.setSuccess( false );

        build.setUpdateScmResult( scmResult );

        String buildId = store.addBuild( project.getId(), build );

        getLogger().info( "Build id: '" + buildId + "'." );

        return buildId;
    }

    // Check to see if there is only a single build in the builds list.
    public boolean isNew( ContinuumProject project )
        throws ContinuumStoreException
    {
        Collection builds = store.getBuildsForProject( project.getId(), 0, 0 );

        return builds.size() == 0;
    }

    public static String throwableToString( Throwable error )
    {
        if ( error == null )
        {
            return "";
        }

        StringWriter writer = new StringWriter();

        PrintWriter printer = new PrintWriter( writer );

        error.printStackTrace( printer );

        printer.flush();

        return writer.getBuffer().toString();
    }
}
