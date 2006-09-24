/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.project.ContinuumProjectState;

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class DistrubutedBuildController extends AbstractLogEnabled {

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifierDispatcher;

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    
    public void build( Project project, BuildDefinition buildDefinition, int trigger )
    {

        long startTime = System.currentTimeMillis();

        // ----------------------------------------------------------------------
        // Initialize the context
        // ----------------------------------------------------------------------

        // if these calls fail we're screwed anyway
        // and it will only be logged through the logger.


        BuildResult build = null;

        // ----------------------------------------------------------------------
        // TODO: Centralize the error handling from the SCM related actions.
        // ContinuumScmResult should return a ContinuumScmResult from all
        // methods, even in a case of failure.
        // ----------------------------------------------------------------------

        try
        {
            notifierDispatcher.buildStarted( project );

            Map actionContext = new HashMap();

            actionContext.put( AbstractContinuumAgentAction.KEY_PROJECT, project );

            actionContext.put( AbstractContinuumAgentAction.KEY_BUILD_DEFINITION, buildDefinition );

            actionContext.put( AbstractContinuumAction.KEY_TRIGGER, new Integer( trigger ) );

            ScmResult scmResult = null;

            try
            {
                actionManager.lookup( "check-working-directory" ).execute( actionContext );

                boolean workingDirectoryExists = AbstractContinuumAction.getBoolean( actionContext,
                                                                                     AbstractContinuumAction.KEY_WORKING_DIRECTORY_EXISTS );

                if ( workingDirectoryExists )
                {
                    actionManager.lookup( "update-working-directory-from-scm" ).execute( actionContext );

                    scmResult = AbstractContinuumAction.getUpdateScmResult( actionContext, null );
                }
                else
                {
                    actionContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY,
                                       workingDirectoryService.getWorkingDirectory( project ).getAbsolutePath() );

                    actionManager.lookup( "checkout-project" ).execute( actionContext );

                    scmResult = AbstractContinuumAction.getCheckoutResult( actionContext, null );
                }

                // ----------------------------------------------------------------------
                // Check to see if there was a error while checking out/updating the project
                // ----------------------------------------------------------------------

                if ( scmResult == null || !scmResult.isSuccess() )
                {
                    // scmResult must be converted before sotring it because jpox modify value of all fields to null
                    String error = convertScmResultToError( scmResult );

                    build = makeAndStoreBuildResult( project, scmResult, startTime, trigger );

                    build.setError( error );

                    store.updateBuildResult( build );

                    build = store.getBuildResult( build.getId() );

                    project.setState( build.getState() );

                    store.updateProject( project );

                    return;
                }

                actionContext.put( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT, scmResult );

                scmResult = (ScmResult) actionContext.get( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT );

                actionManager.lookup( "update-project-from-working-directory" ).execute( actionContext );

                actionManager.lookup( "execute-builder" ).execute( actionContext );

                String s = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );

                if ( s != null )
                {
                    build = store.getBuildResult( Integer.valueOf( s ).intValue() );
                }
            }
            catch ( Throwable e )
            {
                getLogger().error( "Error while building project.", e );

                String s = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );

                if ( s != null )
                {
                    build = store.getBuildResult( Integer.valueOf( s ).intValue() );
                }
                else
                {
                    build = makeAndStoreBuildResult( project, scmResult, startTime, trigger );
                }

                // This can happen if the "update project from scm" action fails

                String error = null;

                if ( e instanceof ContinuumScmException )
                {
                    ContinuumScmException ex = (ContinuumScmException) e;

                    ScmResult result = ex.getResult();

                    if ( result != null )
                    {
                        error = convertScmResultToError( result );
                    }

                }
                if ( error == null )
                {
                    error = ContinuumUtils.throwableToString( e );
                }

                build.setError( error );

                store.updateBuildResult( build );

                build = store.getBuildResult( build.getId() );

                project.setState( build.getState() );

                store.updateProject( project );
            }
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
            try
            {
                project = store.getProject( project.getId() );
            }
            catch ( ContinuumStoreException ex )
            {
                getLogger().error( "Internal error while building the project.", ex );
            }

            if ( project.getState() != ContinuumProjectState.NEW &&
                 project.getState() != ContinuumProjectState.OK &&
                 project.getState() != ContinuumProjectState.FAILED &&
                 project.getState() != ContinuumProjectState.ERROR )
            {
                try
                {
                    project.setState( ContinuumProjectState.ERROR );

                    store.updateProject( project );
                }
                catch ( ContinuumStoreException e )
                {
                    getLogger().error( "Internal error while storing the project.", e );
                }
            }

            notifierDispatcher.buildComplete( project, build );
        }
    }

    private String convertScmResultToError( ScmResult result )
    {
        String error = "";

        if ( result == null )
        {
            error = "Scm result is null.";
        }
        else
        {
            if ( result.getCommandLine() != null )
            {
                error = "Command line: " + StringUtils.clean( result.getCommandLine() ) +
                    System.getProperty( "line.separator" );
            }

            if ( result.getProviderMessage() != null )
            {
                error = "Provider message: " + StringUtils.clean( result.getProviderMessage() ) +
                    System.getProperty( "line.separator" );
            }

            if ( result.getCommandOutput() != null )
            {
                error += "Command output: " + System.getProperty( "line.separator" );
                error += "-------------------------------------------------------------------------------" +
                    System.getProperty( "line.separator" );
                error += StringUtils.clean( result.getCommandOutput() ) + System.getProperty( "line.separator" );
                error += "-------------------------------------------------------------------------------" +
                    System.getProperty( "line.separator" );
            }

            if ( result.getException() != null )
            {
                error += "Exception:" + System.getProperty( "line.separator" );
                error += result.getException();
            }
        }

        return error;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private BuildResult makeAndStoreBuildResult( Project project, ScmResult scmResult, long startTime, int trigger )
        throws ContinuumStoreException
    {
        BuildResult build = new BuildResult();

        build.setState( ContinuumProjectState.ERROR );

        build.setTrigger( trigger );

        build.setStartTime( startTime );

        build.setEndTime( System.currentTimeMillis() );

        build.setScmResult( scmResult );

        store.addBuildResult( project, build );

        return store.getBuildResult( build.getId() );
    }

}
