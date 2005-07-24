package org.apache.maven.continuum.core.action;

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

import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExecuteBuilderContinuumAction
    extends AbstractContinuumAction
{
    private BuildExecutorManager buildExecutorManager;

    private ContinuumStore store;

    private ContinuumNotificationDispatcher notifier;

    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        ContinuumProject project = store.getProject( getProjectId( context ) );

        boolean forced = isForced( context );

        ScmResult scmResult = getUpdateScmResult( context );

        ContinuumBuildExecutor buildExecutor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        // ----------------------------------------------------------------------
        // This is really a precondition for this action to execute
        // ----------------------------------------------------------------------

        if ( scmResult.getFiles().size() == 0 &&
             !forced &&
             !isNew( project ) )
        {
            getLogger().info( "No files updated, not building. Project id '" + project.getId() + "'." );

            return;
        }

        // ----------------------------------------------------------------------
        // Make the build
        // ----------------------------------------------------------------------

        ContinuumBuild build = new ContinuumBuild();

        build.setStartTime( new Date().getTime() );

        build.setState( ContinuumProjectState.BUILDING );

        build.setForced( forced );

        build.setScmResult( scmResult );

        String buildId = store.addBuild( project.getId(), build ).getId();

        context.put( KEY_BUILD_ID, buildId );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        build = store.getBuild( buildId );

        String output = null;

        try
        {
            notifier.runningGoals( project, build );

            ContinuumBuildExecutionResult result = buildExecutor.build( project );

            build.setState( result.getExitCode() == 0 ?
                            ContinuumProjectState.OK : ContinuumProjectState.FAILED );

            build.setExitCode( result.getExitCode() );

            output = result.getOutput();
        }
        catch( Throwable e )
        {
            build.setState( ContinuumProjectState.ERROR );

            build.setError( ContinuumUtils.throwableToString( e ) );
        }
        finally
        {
            build.setEndTime( new Date().getTime() );

            // ----------------------------------------------------------------------
            // Copy over the build result
            // ----------------------------------------------------------------------

            store.setBuildOutput( buildId, output );

            build = store.updateBuild( build );

            notifier.goalsCompleted( project, build );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private boolean isNew( ContinuumProject project )
        throws ContinuumStoreException
    {
        Collection builds = store.getBuildsForProject( project.getId(), 0, 0 );

        return builds.size() == 0;
    }
}
