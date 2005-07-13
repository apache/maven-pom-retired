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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.buildcontroller.DefaultBuildController;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ScmResult;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExecuteBuilderContinuumAction
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        ContinuumProject project = getProject( context );

        boolean forced = isForced( context );

        ScmResult scmResult = getUpdateScmResult( context );

        ContinuumBuildExecutor buildExecutor = getCore().getBuildExecutor( project.getExecutorId() );

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

        String buildId = getStore().addBuild( project.getId(), build );

        context.put( KEY_BUILD_ID, buildId );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        build = getStore().getBuild( buildId );

        try
        {
            getNotifier().runningGoals( project, getBuild( context ) );

            ContinuumBuildExecutionResult result = buildExecutor.build( project );

            if ( result.isSuccess() )
            {
                build.setState( ContinuumProjectState.OK );
            }
            else
            {
                build.setState( ContinuumProjectState.FAILED );
            }

            build.setSuccess( result.isSuccess() );

            build.setStandardOutput( result.getStandardOutput() );

            build.setStandardError( result.getStandardError() );

            build.setExitCode( result.getExitCode() );
        }
        catch( Throwable e )
        {
            build.setState( ContinuumProjectState.ERROR );

            build.setSuccess( false );

            build.setError( ContinuumUtils.throwableToString( e ) );
        }
        finally
        {
            build.setEndTime( new Date().getTime() );

            // ----------------------------------------------------------------------
            // Copy over the build result
            // ----------------------------------------------------------------------

            getStore().updateBuild( build );

            getNotifier().goalsCompleted( project, build );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private boolean isNew( ContinuumProject project )
        throws ContinuumException
    {
        Collection builds = getCore().getBuildsForProject( project.getId() );

        return builds.size() == 0;
    }
}
