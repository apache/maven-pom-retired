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

import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultBuildController
    extends AbstractLogEnabled
    implements BuildController
{
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
        // TODO: Centralize the error handling from the SCM related actions.
        // ContinuumScmResult should return a ContinuumScmResult from all
        // methods, even in a case of failure.
        // ----------------------------------------------------------------------

        try
        {
            notifierDispatcher.buildStarted( project );

            Map actionContext = new HashMap();

            actionContext.put( AbstractContinuumAction.KEY_PROJECT_ID, projectId );

            actionContext.put( AbstractContinuumAction.KEY_FORCED, Boolean.valueOf( forced ) );

            ScmResult scmResult = null;

            try
            {
                actionManager.lookup( "check-working-directory" ).execute( actionContext );

                boolean workingDirectoryExists = AbstractContinuumAction.getBoolean( actionContext,
                                                                                     AbstractContinuumAction.KEY_WORKING_DIRECTORY_EXISTS );

                if ( workingDirectoryExists )
                {
                    actionManager.lookup( "update-working-directory-from-scm" ).execute( actionContext );
                }
                else
                {
                    actionContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY,
                                       workingDirectoryService.getWorkingDirectory( project ) );

                    actionManager.lookup( "checkout-project" ).execute( actionContext );

                    ScmResult checkOutScmResult = AbstractContinuumAction.getCheckoutResult( actionContext, null );

                    String checkoutErrorMessage = AbstractContinuumAction.getCheckoutErrorMessage( actionContext,
                                                                                                   null );

                    String checkoutErrorException = AbstractContinuumAction.getCheckoutErrorException( actionContext,
                                                                                                       null );

                    // ----------------------------------------------------------------------
                    // Check to see if there was a error while checking out the project
                    // ----------------------------------------------------------------------

                    if ( !StringUtils.isEmpty( checkoutErrorMessage ) ||
                        !StringUtils.isEmpty( checkoutErrorException ) || checkOutScmResult == null )
                    {
                        ContinuumBuild build = makeBuildResult( scmResult, startTime, forced );

                        String error = "";

                        if ( !StringUtils.isEmpty( checkoutErrorMessage ) )
                        {
                            error = "Error message:" + System.getProperty( "line.separator" );
                            error += checkoutErrorException;
                        }

                        if ( !StringUtils.isEmpty( checkoutErrorException ) )
                        {
                            error += "Exception:" + System.getProperty( "line.separator" );
                            error += checkoutErrorException;
                        }

                        build.setError( error );

                        buildId = storeBuild( project, build ).getId();

                        return;
                    }

                    actionContext.put( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT, checkOutScmResult );
                }

                scmResult = (ScmResult) actionContext.get( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT );

                actionManager.lookup( "update-project-from-working-directory" ).execute( actionContext );

                actionManager.lookup( "execute-builder" ).execute( actionContext );

                buildId = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );
            }
            catch ( Throwable e )
            {
                getLogger().error( "Error while building project.", e );

                ContinuumBuild build = makeBuildResult( scmResult, startTime, forced );

                // This can happen if the "update project from scm" action fails

                String error;

                if ( e instanceof ContinuumScmException )
                {
                    ContinuumScmException ex = (ContinuumScmException) e;

                    ScmResult result = ex.getResult();

                    error = "";

                    if ( result != null )
                    {
                        error += "Provider message: " + StringUtils.clean( result.getProviderMessage() ) +
                            System.getProperty( "line.separator" );
                        error += "Command output: " + System.getProperty( "line.separator" );
                        error += "-------------------------------------------------------------------------------" +
                            System.getProperty( "line.separator" );
                        error += StringUtils.clean( result.getCommandOutput() ) +
                            System.getProperty( "line.separator" );
                        error += "-------------------------------------------------------------------------------" +
                            System.getProperty( "line.separator" );
                    }

                    error += "Exception:" + System.getProperty( "line.separator" );
                    error += ContinuumUtils.throwableToString( e );
                }
                else
                {
                    error = ContinuumUtils.throwableToString( e );
                }

                build.setError( error );

                buildId = storeBuild( project, build ).getId();

                project.setState( ContinuumProjectState.ERROR );
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

            notifierDispatcher.buildComplete( project, build );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ContinuumBuild storeBuild( ContinuumProject project, ContinuumBuild build )
        throws ContinuumStoreException
    {
        build = store.addBuild( project.getId(), build );

        getLogger().info( "Build id: '" + build.getId() + "'." );

        return build;
    }

    private ContinuumBuild makeBuildResult( ScmResult scmResult, long startTime, boolean forced )
    {
        ContinuumBuild build = new ContinuumBuild();

        build.setState( ContinuumProjectState.ERROR );

        // TODO: set trigger properly
        build.setTrigger( forced ? ContinuumProjectState.TRIGGER_FORCED : ContinuumProjectState.TRIGGER_UNKNOWN );

        build.setStartTime( startTime );

        build.setEndTime( System.currentTimeMillis() );

        build.setScmResult( scmResult );

        return build;
    }

    // Check to see if there is only a single build in the builds list.
    public boolean isNew( ContinuumProject project )
        throws ContinuumStoreException
    {
        Collection builds = store.getBuildsForProject( project.getId(), 0, 0 );

        return builds.size() == 0;
    }
}
