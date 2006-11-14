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
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.apache.maven.continuum.buildcontroller.BuildController"
 *   role-hint="default"
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

    /**
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @throws TaskExecutionException
     */
    public void build( int projectId, int buildDefinitionId, int trigger )
        throws TaskExecutionException
    {
        getLogger().info( "Initializing build" );
        BuildContext context = initializeBuildContext( projectId, buildDefinitionId, trigger );

        getLogger().info( "Starting build of " + context.getProject().getName() );
        startBuild( context );

        try
        {
            // check if build definition requires smoking the existing checkout and rechecking out project
            if ( context.getBuildDefinition().isBuildFresh() )
            {
                getLogger().info( "Purging exiting working copy" );
                cleanWorkingDirectory( context );
            }

            // ----------------------------------------------------------------------
            // TODO: Centralize the error handling from the SCM related actions.
            // ContinuumScmResult should return a ContinuumScmResult from all
            // methods, even in a case of failure.
            // ----------------------------------------------------------------------
            getLogger().info( "Updating working dir" );
            updateWorkingDirectory( context );

            getLogger().info( "Merging SCM results" );
            mergeScmResults( context );

            if ( !checkScmResult( context ) )
            {
                getLogger().info( "Error updating from SCM, not building" );
                return;
            }

            checkProjectDependencies( context );

            if ( !shouldBuild( context ) )
            {
                getLogger().info( "No changes, not building" );
                return;
            }

            getLogger().info( "Changes found, building" );

            Map actionContext = context.getActionContext();

            performAction( "update-project-from-working-directory", context );

            performAction( "execute-builder", context );

            performAction( "deploy-artifact", context );

            String s = (String) actionContext.get( AbstractContinuumAction.KEY_BUILD_ID );

            if ( s != null )
            {
                try
                {
                    context.setBuildResult( store.getBuildResult( Integer.valueOf( s ).intValue() ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new TaskExecutionException( "Internal error: build id not an integer", e );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    throw new TaskExecutionException( "Internal error: Cannot find build result", e );
                }
                catch ( ContinuumStoreException e )
                {
                    throw new TaskExecutionException( "Error loading build result", e );
                }
            }
        }
        finally
        {
            endBuild( context );
        }
    }

    /**
     * Checks if the build should be marked as ERROR and notifies the end of the build.
     *
     * @param context
     * @throws TaskExecutionException
     */
    private void endBuild( BuildContext context )
        throws TaskExecutionException
    {
        Project project = context.getProject();

        try
        {
            if ( project.getState() != ContinuumProjectState.NEW &&
                project.getState() != ContinuumProjectState.CHECKEDOUT &&
                project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR )
            {
                try
                {
                    String s = (String) context.getActionContext().get( AbstractContinuumAction.KEY_BUILD_ID );

                    if ( s != null )
                    {
                        BuildResult buildResult = store.getBuildResult( Integer.valueOf( s ).intValue() );
                        project.setState( buildResult.getState() );
                    }
                    else
                    {
                        project.setState( ContinuumProjectState.ERROR );
                    }

                    store.updateProject( project );
                }
                catch ( ContinuumStoreException e )
                {
                    throw new TaskExecutionException( "Error storing the project", e );
                }
            }
        }
        finally
        {
            notifierDispatcher.buildComplete( project, context.getBuildResult() );
        }
    }

    private void updateBuildResult( BuildContext context, String error )
        throws TaskExecutionException
    {
        BuildResult build = context.getBuildResult();

        if ( build == null )
        {
            build = makeAndStoreBuildResult( context, error );
        }
        else
        {

            build.setError( error );

            try
            {
                store.updateBuildResult( build );

                build = store.getBuildResult( build.getId() );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error updating build result", e );
            }
        }

        context.getProject().setState( build.getState() );

        try
        {
            store.updateProject( context.getProject() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error updating project", e );
        }
    }

    private void startBuild( BuildContext context )
        throws TaskExecutionException
    {

        Project project = context.getProject();

        project.setOldState( project.getState() );

        project.setState( ContinuumProjectState.BUILDING );

        try
        {
            store.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error persisting project", e );
        }

        notifierDispatcher.buildStarted( project );

    }

    /**
     * Initializes a BuildContext for the build.
     *
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @return
     * @throws TaskExecutionException
     */
    protected BuildContext initializeBuildContext( int projectId, int buildDefinitionId, int trigger )
        throws TaskExecutionException
    {
        BuildContext context = new BuildContext();

        context.setStartTime( System.currentTimeMillis() );

        context.setTrigger( trigger );

        try
        {
            context.setProject( store.getProject( projectId ) );

            BuildDefinition buildDefinition = store.getBuildDefinition( buildDefinitionId );

            context.setBuildDefinition( buildDefinition );

            try
            {
                BuildResult oldBuildResult = store.getBuildResult( buildDefinition.getLatestBuildId() );

                context.setOldBuildResult( oldBuildResult );

                context.setOldScmResult( getOldScmResult( projectId, oldBuildResult.getEndTime() ) );

            }
            catch ( ContinuumObjectNotFoundException ex )
            {
                // Nothing to do
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error initializing the build context", e );
        }

        Map actionContext = context.getActionContext();

        actionContext.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        actionContext.put( AbstractContinuumAction.KEY_PROJECT, context.getProject() );

        actionContext.put( AbstractContinuumAction.KEY_BUILD_DEFINITION_ID, new Integer( buildDefinitionId ) );

        actionContext.put( AbstractContinuumAction.KEY_BUILD_DEFINITION, context.getBuildDefinition() );

        actionContext.put( AbstractContinuumAction.KEY_TRIGGER, new Integer( trigger ) );

        actionContext.put( AbstractContinuumAction.KEY_FIRST_RUN,
                           Boolean.valueOf( context.getOldBuildResult() == null ) );

        return context;
    }

    private void cleanWorkingDirectory( BuildContext context )
        throws TaskExecutionException
    {
        performAction( "clean-working-directory", context );
    }

    private void updateWorkingDirectory( BuildContext context )
        throws TaskExecutionException
    {
        Map actionContext = context.getActionContext();

        performAction( "check-working-directory", context );

        boolean workingDirectoryExists =
            AbstractContinuumAction.getBoolean( actionContext, AbstractContinuumAction.KEY_WORKING_DIRECTORY_EXISTS );

        ScmResult scmResult;

        if ( workingDirectoryExists )
        {
            performAction( "update-working-directory-from-scm", context );

            scmResult = AbstractContinuumAction.getUpdateScmResult( actionContext, null );
        }
        else
        {
            Project project = (Project) actionContext.get( AbstractContinuumAction.KEY_PROJECT );

            actionContext.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY,
                               workingDirectoryService.getWorkingDirectory( project ).getAbsolutePath() );

            performAction( "checkout-project", context );

            scmResult = AbstractContinuumAction.getCheckoutResult( actionContext, null );
        }

        context.setScmResult( scmResult );
    }

    private void performAction( String actionName, BuildContext context )
        throws TaskExecutionException
    {
        String error = null;
        TaskExecutionException exception = null;

        try
        {
            getLogger().info( "Performing action " + actionName );
            actionManager.lookup( actionName ).execute( context.getActionContext() );
            return;
        }
        catch ( ActionNotFoundException e )
        {
            error = ContinuumUtils.throwableToString( e );
            exception = new TaskExecutionException( "Error looking up action '" + actionName + "'", e );
        }
        catch ( ContinuumScmException e )
        {
            ScmResult result = e.getResult();

            if ( result != null )
            {
                error = convertScmResultToError( result );
            }

            if ( error == null )
            {
                error = ContinuumUtils.throwableToString( e );
            }

            exception = new TaskExecutionException( "SCM error while executing '" + actionName + "'", e );
        }
        catch ( Exception e )
        {
            exception = new TaskExecutionException( "Error executing action '" + actionName + "'", e );
        }

        // TODO: clean this up. We catch the original exception from the action, and then update the buildresult
        // for it - we need to because of the specialized error message for SCM.
        // If updating the buildresult fails, log the previous error and throw the new one.
        // If updating the buildresult succeeds, throw the original exception. The build result should NOT
        // be updated again - a TaskExecutionException is final, no further action should be taken upon it.

        try
        {
            updateBuildResult( context, error );
        }
        catch ( TaskExecutionException e )
        {
            getLogger().error( "Error updating build result after receiving the following exception: ", exception );
            throw e;
        }

        throw exception;
    }

    protected boolean shouldBuild( BuildContext context )
        throws TaskExecutionException
    {
        boolean shouldBuild = true;

        Project project = context.getProject();

        // Check SCM changes
        boolean allChangesUnknown = checkAllChangesUnknown( context.getScmResult().getChanges() );

        if ( allChangesUnknown && project.getOldState() != ContinuumProjectState.NEW &&
            project.getOldState() != ContinuumProjectState.CHECKEDOUT &&
            context.getTrigger() != ContinuumProjectState.TRIGGER_FORCED &&
            project.getState() != ContinuumProjectState.NEW && project.getState() != ContinuumProjectState.CHECKEDOUT )
        {
            if ( !context.getScmResult().getChanges().isEmpty() )
            {
                getLogger().info( "The project was not built because all changes are unknown." );
            }
            else
            {
                getLogger().info( "The project was not built because there are no changes." );
            }

            project.setState( project.getOldState() );

            project.setOldState( 0 );

            try
            {
                store.updateProject( project );
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error storing project", e );
            }

            shouldBuild = false;

            // Check dependencies changes
            if ( context.getModifiedDependencies() != null && !context.getModifiedDependencies().isEmpty() )
            {
                shouldBuild = true;
            }
        }
        else
        {
            // Check dependencies changes
            if ( context.getModifiedDependencies() != null && !context.getModifiedDependencies().isEmpty() )
            {
                shouldBuild = true;
            }
            else
            {
                shouldBuild = false;
            }
        }

        return shouldBuild;
    }

    private boolean checkAllChangesUnknown( List changes )
    {
        for ( Iterator iterChanges = changes.iterator(); iterChanges.hasNext(); )
        {
            ChangeSet changeSet = (ChangeSet) iterChanges.next();

            List changeFiles = changeSet.getFiles();

            Iterator iterFiles = changeFiles.iterator();

            while ( iterFiles.hasNext() )
            {
                ChangeFile changeFile = (ChangeFile) iterFiles.next();

                if ( !"unknown".equalsIgnoreCase( changeFile.getStatus() ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    protected void checkProjectDependencies( BuildContext context )
    {
        if ( context.getOldBuildResult() == null )
        {
            return;
        }

        try
        {
            Project project = store.getProjectWithAllDetails( context.getProject().getId() );
            List dependencies = project.getDependencies();

            if ( dependencies == null )
            {
                return;
            }
            List modifiedDependencies = new ArrayList();

            for ( Iterator i = dependencies.iterator(); i.hasNext(); )
            {
                ProjectDependency dep = (ProjectDependency) i.next();
                Project dependencyProject = store.getProject( dep.getGroupId(), dep.getArtifactId(), dep.getVersion() );

                if ( dependencyProject != null )
                {
                    List buildResults = store.getBuildResultsInSuccessForProject( dependencyProject.getId(),
                                                                                  context.getOldBuildResult().getEndTime() );
                    if ( buildResults != null && !buildResults.isEmpty() )
                    {
                        getLogger().debug( "Dependency changed: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" +
                            dep.getVersion() );
                        modifiedDependencies.add( dep );
                    }
                }
                else
                {
                    getLogger().debug( "Skip non Continuum project: " + dep.getGroupId() + ":" + dep.getArtifactId() +
                        ":" + dep.getVersion() );
                }
            }

            context.setModifiedDependencies( modifiedDependencies );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().warn( "Can't get the project dependencies", e );
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

    private BuildResult makeAndStoreBuildResult( BuildContext context, String error )
        throws TaskExecutionException
    {
        // Project project, ScmResult scmResult, long startTime, int trigger )
        // project, scmResult, startTime, trigger );

        BuildResult build = new BuildResult();

        build.setState( ContinuumProjectState.ERROR );

        build.setTrigger( context.getTrigger() );

        build.setStartTime( context.getStartTime() );

        build.setEndTime( System.currentTimeMillis() );

        build.setScmResult( context.getScmResult() );

        List dependencies = context.getModifiedDependencies();
        if ( dependencies != null && !dependencies.isEmpty() )
        {
            List modifiedDependencies = new ArrayList();
            for ( Iterator i = dependencies.iterator(); i.hasNext(); )
            {
                ProjectDependency dep = (ProjectDependency) i.next();
                modifiedDependencies.add( dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion() );
            }
            build.setModifiedDependencies( modifiedDependencies );
        }

        if ( error != null )
        {
            build.setError( error );
        }

        try
        {
            store.addBuildResult( context.getProject(), build );

            build = store.getBuildResult( build.getId() );

            context.setBuildResult( build );

            return build;
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error storing build result", e );
        }
    }

    private ScmResult getOldScmResult( int projectId, long fromDate )
    {
        List results = store.getBuildResultsForProject( projectId, fromDate );

        ScmResult res = new ScmResult();

        if ( results != null )
        {
            for ( Iterator i = results.iterator(); i.hasNext(); )
            {
                BuildResult result = (BuildResult) i.next();

                ScmResult scmResult = result.getScmResult();

                if ( scmResult != null )
                {
                    List changes = scmResult.getChanges();

                    if ( changes != null )
                    {
                        for ( Iterator j = changes.iterator(); j.hasNext(); )
                        {
                            ChangeSet changeSet = (ChangeSet) j.next();

                            if ( changeSet.getDate() < fromDate )
                            {
                                continue;
                            }

                            if ( !res.getChanges().contains( changeSet ) )
                            {
                                res.addChange( changeSet );
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    /**
     * Merges scm results so we'll have all changes since last execution of current build definition
     */
    private void mergeScmResults( BuildContext context )
    {
        ScmResult oldScmResult = context.getOldScmResult();
        ScmResult newScmResult = context.getScmResult();

        if ( oldScmResult != null )
        {
            if ( newScmResult == null )
            {
                context.setScmResult( oldScmResult );
            }
            else
            {
                List oldChanges = oldScmResult.getChanges();

                List newChanges = newScmResult.getChanges();

                for ( Iterator i = newChanges.iterator(); i.hasNext(); )
                {
                    ChangeSet change = (ChangeSet) i.next();

                    if ( !oldChanges.contains( change ) )
                    {
                        oldChanges.add( change );
                    }
                }

                newScmResult.setChanges( oldChanges );
            }
        }
    }

    /**
     * Check to see if there was a error while checking out/updating the project
     *
     * @throws TaskExecutionException
     */
    private boolean checkScmResult( BuildContext context )
        throws TaskExecutionException
    {
        ScmResult scmResult = context.getScmResult();

        if ( scmResult == null || !scmResult.isSuccess() )
        {
            // scmResult must be converted before storing it because jpox modifies values of all fields to null
            String error = convertScmResultToError( scmResult );

            BuildResult build = makeAndStoreBuildResult( context, error );

            try
            {
                Project project = context.getProject();

                project.setState( build.getState() );

                store.updateProject( project );

                return false;
            }
            catch ( ContinuumStoreException e )
            {
                throw new TaskExecutionException( "Error storing project", e );
            }
        }

        context.getActionContext().put( AbstractContinuumAction.KEY_UPDATE_SCM_RESULT, scmResult );

        return true;
    }

}
