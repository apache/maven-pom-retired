package org.apache.maven.continuum.core.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.scm.TestResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.utils.ContinuumUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action"
 * role-hint="execute-builder"
 */
public class ExecuteBuilderContinuumAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifier;

    public void execute( Map context )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        Project project = getProject( context );

        BuildDefinition buildDefinition = getBuildDefinition( context );

        int trigger = getTrigger( context );

        ScmResult scmResult = getUpdateScmResult( context );

        List updatedDependencies = getUpdatedDependencies( context );

        boolean hasUpdatedDependencies = updatedDependencies != null && !updatedDependencies.isEmpty();

        boolean isFirstRun = ( (Boolean) context.get( AbstractContinuumAction.KEY_FIRST_RUN ) ).booleanValue();

        ContinuumBuildExecutor buildExecutor = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        // ----------------------------------------------------------------------
        // This is really a precondition for this action to execute
        // ----------------------------------------------------------------------

        if ( !isFirstRun && project.getOldState() != ContinuumProjectState.NEW &&
            project.getOldState() != ContinuumProjectState.CHECKEDOUT && scmResult.getChanges().size() == 0 &&
            !hasUpdatedDependencies && trigger != ContinuumProjectState.TRIGGER_FORCED && !isNew( project ) )
        {
            getLogger().info( "No files updated, not building. Project id '" + project.getId() + "'." );

            project.setState( project.getOldState() );

            project.setOldState( 0 );

            store.updateProject( project );

            return;
        }

        // ----------------------------------------------------------------------
        // Make the buildResult
        // ----------------------------------------------------------------------

        BuildResult buildResult = new BuildResult();

        buildResult.setStartTime( new Date().getTime() );

        buildResult.setState( ContinuumProjectState.BUILDING );

        buildResult.setTrigger( trigger );

        buildResult.setScmResult( scmResult );

        buildResult.setModifiedDependencies( updatedDependencies );

        store.addBuildResult( project, buildResult );

        context.put( KEY_BUILD_ID, Integer.toString( buildResult.getId() ) );

        buildResult = store.getBuildResult( buildResult.getId() );

        try
        {
            notifier.runningGoals( project, buildDefinition, buildResult );

            File buildOutputFile = configurationService.getBuildOutputFile( buildResult.getId(), project.getId() );

            ContinuumBuildExecutionResult result = buildExecutor.build( project, buildDefinition, buildOutputFile );

            buildResult.setState( result.getExitCode() == 0 ? ContinuumProjectState.OK : ContinuumProjectState.FAILED );

            buildResult.setExitCode( result.getExitCode() );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error running buildResult", e );

            buildResult.setState( ContinuumProjectState.ERROR );

            buildResult.setError( ContinuumUtils.throwableToString( e ) );
        }
        finally
        {
            buildResult.setEndTime( new Date().getTime() );

            if ( buildResult.getState() == ContinuumProjectState.OK )
            {
                project.setBuildNumber( project.getBuildNumber() + 1 );
            }

            project.setLatestBuildId( buildResult.getId() );

            buildDefinition.setLatestBuildId( buildResult.getId() );

            buildResult.setBuildNumber( project.getBuildNumber() );

            if ( buildResult.getState() != ContinuumProjectState.OK &&
                buildResult.getState() != ContinuumProjectState.FAILED &&
                buildResult.getState() != ContinuumProjectState.ERROR )
            {
                buildResult.setState( ContinuumProjectState.ERROR );
            }

            project.setState( buildResult.getState() );

            // ----------------------------------------------------------------------
            // Set the test result
            // ----------------------------------------------------------------------

            try
            {
                TestResult testResult = buildExecutor.getTestResults( project );
                buildResult.setTestResult( testResult );
            }
            catch ( Throwable t )
            {
                getLogger().error( "Error getting test results", t );
            }

            // ----------------------------------------------------------------------
            // Copy over the buildResult result
            // ----------------------------------------------------------------------

            store.updateBuildResult( buildResult );

            buildResult = store.getBuildResult( buildResult.getId() );

            store.storeBuildDefinition( buildDefinition );

            store.updateProject( project );

            notifier.goalsCompleted( project, buildDefinition, buildResult );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private boolean isNew( Project project )
    {
        return project.getState() == ContinuumProjectState.NEW ||
            project.getState() == ContinuumProjectState.CHECKEDOUT;
    }
}
