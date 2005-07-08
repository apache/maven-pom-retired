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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;

import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

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
    private ContinuumNotificationDispatcher notifierDispatcher;

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
            notifierDispatcher.buildStarted( project );

            Map actionContext = new HashMap();

            actionContext.put( AbstractContinuumAction.KEY_PROJECT_ID, projectId );

            actionContext.put( AbstractContinuumAction.KEY_FORCED, Boolean.valueOf( forced ) );

            UpdateScmResult scmResult = null;

            try
            {
                actionManager.lookup( "check-working-directory" ).execute( actionContext );

                boolean workingDirectoryExists = AbstractContinuumAction.getBoolean( actionContext, AbstractContinuumAction.KEY_WORKING_DIRECTORY_EXISTS );

                if ( workingDirectoryExists )
                {
                    actionManager.lookup( "update-working-directory-from-scm" ).execute( actionContext );
                }
                else
                {
                    actionContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY, project.getWorkingDirectory() );

                    actionManager.lookup( "checkout-project" ).execute( actionContext );

                    CheckOutScmResult checkOutScmResult = AbstractContinuumAction.getCheckoutResult( actionContext, null );

                    String checkoutErrorMessage = AbstractContinuumAction.getCheckoutErrorMessage( actionContext, null );

                    String checkoutErrorException = AbstractContinuumAction.getCheckoutErrorException( actionContext, null );

                    // ----------------------------------------------------------------------
                    // Check to see if there was a error while checking out the project
                    // ----------------------------------------------------------------------

                    if ( StringUtils.isEmpty( checkoutErrorMessage ) && StringUtils.isEmpty( checkoutErrorException ) )
                    {
                        // TODO: remove once CONTINUUM-193 is resolved
                        UpdateScmResult updateScmResult = new UpdateScmResult();

                        updateScmResult.setCommandOutput( checkOutScmResult.getCommandOutput() );

                        updateScmResult.setProviderMessage( checkOutScmResult.getProviderMessage() );

                        updateScmResult.setSuccess( false );

                        for ( Iterator it = checkOutScmResult.getCheckedOutFiles().iterator(); it.hasNext(); )
                        {
                            ScmFile scmFile = (ScmFile) it.next();

                            updateScmResult.getUpdatedFiles().add( scmFile );
                        }

                        actionContext.put( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT, updateScmResult );
                    }
                    else
                    {
                        ContinuumBuild build = makeBuildResult( scmResult, startTime, forced );

                        String error = "";

                        if ( !StringUtils.isEmpty( checkoutErrorMessage ) )
                        {
                            error = "Error message:" + System.getProperty( "line.separator" );
                            error = checkoutErrorException;
                        }

                        if ( !StringUtils.isEmpty( checkoutErrorException ) )
                        {
                            error += "Exception:" + System.getProperty( "line.separator" );
                            error += checkoutErrorException;
                        }

                        build.setError( error );

                        buildId = storeBuild( project, build );

                        return;
                     }
                }

                scmResult = (UpdateScmResult) actionContext.get( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT );

                actionManager.lookup( "update-project-from-working-directory" ).execute( actionContext );

                actionManager.lookup( "execute-builder" ).execute( actionContext );

                buildId = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );
            }
            catch ( Throwable e )
            {
                getLogger().error( "Error while building project.", e );

                ContinuumBuild build = makeBuildResult( scmResult, startTime, forced );

                build.setError( ContinuumUtils.throwableToString( e ) );

                buildId = storeBuild( project, build );

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

    private String storeBuild( ContinuumProject project, ContinuumBuild build )
        throws ContinuumStoreException
    {
        String buildId = store.addBuild( project.getId(), build );

        getLogger().info( "Build id: '" + buildId + "'." );

        return buildId;
    }

    private ContinuumBuild makeBuildResult( UpdateScmResult scmResult,
                                            long startTime,
                                            boolean forced )
    {
        ContinuumBuild build = new ContinuumBuild();

        build.setState( ContinuumProjectState.ERROR );

        build.setForced( forced );

        build.setStartTime( startTime );

        build.setEndTime( System.currentTimeMillis() );

        build.setSuccess( false );

        build.setUpdateScmResult( scmResult );

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
