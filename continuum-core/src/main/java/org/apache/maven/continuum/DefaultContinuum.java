package org.apache.maven.continuum;

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

import org.apache.maven.continuum.build.settings.SchedulesActivationException;
import org.apache.maven.continuum.build.settings.SchedulesActivator;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.AddProjectToCheckOutQueueAction;
import org.apache.maven.continuum.core.action.CreateProjectsFromMetadata;
import org.apache.maven.continuum.core.action.StoreProjectAction;
import org.apache.maven.continuum.initialization.ContinuumInitializationException;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenOneContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ProjectSorter;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class DefaultContinuum
    extends AbstractLogEnabled
    implements Continuum, Initializable, Startable
{
    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumInitializer initializer;

    /**
     * @plexus.requirement
     */
    private SchedulesActivator schedulesActivator;

    /**
     * @plexus.requirement
     */
    private ContinuumSecurity security;

    // ----------------------------------------------------------------------
    // Moved from core
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue checkoutQueue;

    /**
     * @plexus.configuration
     */
    private String workingDirectory;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public Collection getProjects()
        throws ContinuumException
    {
        return store.getAllProjectsByName();
    }

    public Map getLatestBuildResults()
    {
        return store.getLatestBuildResults();
    }

    public Map getBuildResultsInSuccess()
    {
        return store.getBuildResultsInSuccess();
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        return store.getLatestBuildResultForProject( projectId );
    }

    public BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException
    {
        List builds = store.getBuildResultByBuildNumber( projectId, buildNumber );

        if ( builds.isEmpty() )
        {
            return null;
        }
        else
        {
            return (BuildResult) builds.get( 0 );
        }
    }

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    public boolean isInBuildingQueue( int projectId )
        throws ContinuumException
    {
        List queue;

        try
        {
            queue = buildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumException( "Error while getting the building queue.", e );
        }

        for ( Iterator it = queue.iterator(); it.hasNext(); )
        {
            BuildProjectTask task = (BuildProjectTask) it.next();

            if ( task.getProjectId() == projectId )
            {
                return true;
            }
        }

        return false;
    }

    public boolean isInCheckoutQueue( int projectId )
        throws ContinuumException
    {
        List queue;

        try
        {
            queue = checkoutQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumException( "Error while getting the checkout queue.", e );
        }

        for ( Iterator it = queue.iterator(); it.hasNext(); )
        {
            CheckOutTask task = (CheckOutTask) it.next();

            if ( task.getProjectId() == projectId )
            {
                return true;
            }
        }

        return false;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void removeProject( int projectId )
        throws ContinuumException
    {
        try
        {
            Project project = store.getProject( projectId );

            getLogger().info( "Remove project " + project.getName() + "(" + projectId + ")" );

            File workingDirectory = getWorkingDirectory( projectId );

            FileUtils.deleteDirectory( workingDirectory );

            store.removeProject( store.getProject( projectId ) );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing project in database.", ex );
        }
        catch ( IOException e )
        {
            throw logAndCreateException( "Error while deleting project working directory.", e );
        }
    }

    public void checkoutProject( int projectId )
        throws ContinuumException
    {
        Map context = new HashMap();

        context.put( AddProjectToCheckOutQueueAction.KEY_PROJECT_ID, new Integer( projectId ) );

        executeAction( "add-project-to-checkout-queue", context );
    }

    public Project getProject( int projectId )
        throws ContinuumException
    {
        try
        {
            return store.getProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting project '" + projectId + "'.", ex );
        }
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return store.getAllProjectsByName();
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public void buildProjects()
        throws ContinuumException
    {
        buildProjects( ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProjects( int trigger )
        throws ContinuumException
    {
        Collection projectsList = null;

        try
        {
            projectsList = getProjectsInBuildOrder();
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            projectsList = getProjects();
        }

        Map buildDefinitionsIds = store.getDefaultBuildDefinitions();

        for ( Iterator i = projectsList.iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            Integer buildDefId = (Integer) buildDefinitionsIds.get( new Integer( project.getId() ) );

            if ( buildDefId == null )
            {
                throw new ContinuumException( "Project (id=" + project.getId() + " doens't have a default build definition." );
            }

            buildProject( project.getId(), buildDefId.intValue(), trigger );
        }
    }

    public void buildProjects( Schedule schedule )
        throws ContinuumException
    {
        Collection projectsList = null;

        try
        {
            projectsList = getProjectsInBuildOrder();
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            projectsList = getProjects();
        }

        for ( Iterator projectIterator = projectsList.iterator(); projectIterator.hasNext(); )
        {
            Project p = (Project) projectIterator.next();

            if ( !isInBuildingQueue( p.getId() ) && !isInCheckoutQueue( p.getId() ) )
            {
                Project project = getProjectWithAllDetails( p.getId() );

                for ( Iterator bdIterator = project.getBuildDefinitions().iterator(); bdIterator.hasNext(); )
                {
                    BuildDefinition buildDef = (BuildDefinition) bdIterator.next();

                    if ( schedule.getId() == buildDef.getSchedule().getId() )
                    {
                        //TODO: Fix trigger name
                        buildProject( project.getId(), buildDef.getId(), ContinuumProjectState.TRIGGER_UNKNOWN, false );
                    }
                }
            }
        }
    }

    public void buildProject( int projectId )
        throws ContinuumException
    {
        buildProject( projectId, ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProject( int projectId, int trigger )
        throws ContinuumException
    {
        if ( isInBuildingQueue( projectId ) || isInCheckoutQueue( projectId ) )
        {
            return;
        }

        BuildDefinition buildDef = getDefaultBuildDefinition( projectId );

        if ( buildDef == null )
        {
            throw new ContinuumException( "Project (id=" + projectId + " doens't have a default build definition." );
        }

        buildProject( projectId, buildDef.getId(), trigger, false );
    }

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        buildProject( projectId, buildDefinitionId, trigger, true );
    }

    private synchronized void buildProject( int projectId, int buildDefinitionId, int trigger, boolean checkQueues )
        throws ContinuumException
    {
        if ( checkQueues )
        {
            if ( isInBuildingQueue( projectId ) || isInCheckoutQueue( projectId ) )
            {
                return;
            }
        }

        try
        {
            Project project = store.getProject( projectId );

            if ( project.getState() != ContinuumProjectState.NEW &&
                 project.getState() != ContinuumProjectState.OK &&
                 project.getState() != ContinuumProjectState.FAILED &&
                 project.getState() != ContinuumProjectState.ERROR )
            {
                // project is building
                return;
            }

            project.setOldState( project.getState() );

            project.setState( ContinuumProjectState.BUILDING );

            store.updateProject( project );

            getLogger().info( "Enqueuing '" + project.getName() + "' (Build definition id=" + buildDefinitionId + ")." );

            buildQueue.put( new BuildProjectTask( projectId, buildDefinitionId, trigger ) );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while creating build object.", e );
        }
        catch ( TaskQueueException e )
        {
            throw logAndCreateException( "Error while creating enqueuing object.", e );
        }
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumException
    {
        try
        {
            return store.getBuildResult( buildId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Exception while getting build result for project.", e );
        }
    }

    public String getBuildOutput( int projectId, int buildId )
        throws ContinuumException
    {
        try
        {
            return configurationService.getBuildOutput( buildId, projectId );
        }
        catch ( ConfigurationException e )
        {
            throw logAndCreateException( "Exception while getting build result for project.", e );
        }
    }

    public List getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException
    {
        ArrayList buildResults = null;

        try
        {
            buildResults = new ArrayList( store.getProjectWithBuilds( projectId ).getBuildResults() );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Exception while getting build results for project.", e );
        }

        Collections.reverse( buildResults );

        Iterator buildResultsIterator = buildResults.iterator();

        boolean stop = false;

        while( !stop )
        {
            if ( buildResultsIterator.hasNext() )
            {
                BuildResult buildResult = (BuildResult) buildResultsIterator.next();

                if ( buildResult.getId() == buildResultId )
                {
                    stop = true;
                }
            }
            else
            {
                stop = true;
            }
        }

        if ( !buildResultsIterator.hasNext() )
        {
            return null;
        }

        BuildResult buildResult = (BuildResult) buildResultsIterator.next();

        List changes = null;

        while (  buildResult.getState() != ContinuumProjectState.OK )
        {
            if ( changes == null )
            {
                changes = new ArrayList();
            }

            changes.addAll( buildResult.getScmResult().getChanges() );

            if ( !buildResultsIterator.hasNext() )
            {
                return changes;
            }

            buildResult = (BuildResult) buildResultsIterator.next();
        }

        return changes;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException
    {
        return ProjectSorter.getSortedProjects( getProjects() );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenOneContinuumProjectBuilder.ID );
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl, MavenTwoContinuumProjectBuilder.ID );
    }

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    public int addProject( Project project, String executorId )
        throws ContinuumException
    {
        project.setExecutorId( executorId );

        return executeAddProjectFromScmActivity( project );
    }

    // ----------------------------------------------------------------------
    // Activities. These should end up as workflows in werkflow
    // ----------------------------------------------------------------------

    private int executeAddProjectFromScmActivity( Project project )
        throws ContinuumException
    {
        Map context = new HashMap();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        context.put( CreateProjectsFromMetadata.KEY_WORKING_DIRECTORY, getWorkingDirectory() );

        context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT, project );

        try
        {
            context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT_GROUP, store.getDefaultProjectGroup() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error getting the default project group to work with" );
        }

        executeAction( "validate-project", context );

        executeAction( "store-project", context );

        executeAction( "add-project-to-checkout-queue", context );

        return ( (Integer) context.get( StoreProjectAction.KEY_PROJECT_ID ) ).intValue();
    }

    private ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                   String projectBuilderId )
        throws ContinuumException
    {
        Map context = new HashMap();

        context.put( CreateProjectsFromMetadata.KEY_PROJECT_BUILDER_ID, projectBuilderId );

        context.put( CreateProjectsFromMetadata.KEY_URL, metadataUrl );

        context.put( CreateProjectsFromMetadata.KEY_WORKING_DIRECTORY, getWorkingDirectory() );

        // ----------------------------------------------------------------------
        // Create the projects from the URL
        // ----------------------------------------------------------------------

        executeAction( "create-projects-from-metadata", context );

        ContinuumProjectBuildingResult result = (ContinuumProjectBuildingResult) context.get(
            CreateProjectsFromMetadata.KEY_PROJECT_BUILDING_RESULT );

        if ( result.getProjects() != null )
        {
            getLogger().info( "Created " + result.getProjects().size() + " projects." );
        }
        if ( result.getProjectGroups() != null )
        {
            getLogger().info( "Created " + result.getProjectGroups().size() + " project groups." );
        }
        getLogger().info( result.getWarnings().size() + " warnings." );

        // ----------------------------------------------------------------------
        // Look for any warnings.
        // ----------------------------------------------------------------------

        if ( result.getWarnings().size() > 0 )
        {
            for ( Iterator i = result.getWarnings().iterator(); i.hasNext(); )
            {
                getLogger().info( (String) i.next() );
            }

            return result;
        }

        // ----------------------------------------------------------------------
        // Save any new project groups that we've found. Currenly all projects
        // will go into the first project group in the list.
        // ----------------------------------------------------------------------

        if ( result.getProjectGroups().size() != 1 )
        {
            throw new ContinuumException( "The project building result has to contain exactly one project group." );
        }

        ProjectGroup projectGroup = (ProjectGroup) result.getProjectGroups().iterator().next();

        try
        {
            try
            {
                projectGroup = store.getProjectGroupByGroupId( projectGroup.getGroupId() );

                getLogger().info(
                    "Using existing project group with the group id: '" + projectGroup.getGroupId() + "'." );
            }
            catch ( ContinuumObjectNotFoundException e )
            {
                getLogger().info( "Creating project group with the group id: '" + projectGroup.getGroupId() + "'." );

                Map pgContext = new HashMap();

                pgContext.put( CreateProjectsFromMetadata.KEY_WORKING_DIRECTORY, getWorkingDirectory() );

                pgContext.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT_GROUP, projectGroup );

                executeAction( "validate-project-group", pgContext );

                executeAction( "store-project-group", pgContext );

                int projectGroupId = AbstractContinuumAction.getProjectGroupId( pgContext );

                projectGroup = store.getProjectGroup( projectGroupId );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while querying for project group.", e );
        }

        // ----------------------------------------------------------------------
        // Save all the projects
        // TODO: Validate all the projects before saving them
        // ----------------------------------------------------------------------

        List projects = result.getProjects();

        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            projectGroup.addProject( project );
        }

        try
        {
            store.updateProjectGroup( projectGroup );

            for ( Iterator i = projects.iterator(); i.hasNext(); )
            {
                Project project = (Project) i.next();

                context = new HashMap();

                context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT, project );
//
//            executeAction( "validate-project", context );
//
//            executeAction( "store-project", context );
//
                context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( project.getId() ) );

                executeAction( "add-project-to-checkout-queue", context );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error adding projects from modules", e );
        }

        return result;
    }

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    // This whole section needs a scrub but will need to be dealt with generally
    // when we add schedules and profiles to the mix.

    public ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        List notifiers = project.getNotifiers();

        ProjectNotifier notifier = null;

        for ( Iterator i = notifiers.iterator(); i.hasNext(); )
        {
            notifier = (ProjectNotifier) i.next();

            if ( notifier.getId() == notifierId )
            {
                break;
            }
        }

        return notifier;
    }

    public void updateNotifier( int projectId, int notifierId, Map configuration )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        ProjectNotifier notifier = getNotifier( projectId, notifierId );

        String notifierType = notifier.getType();

        // I remove notifier then add it instead of update it due to a ClassCastException in jpox
        project.removeNotifier( notifier );

        updateProject( project );

        addNotifier( projectId, notifierType, configuration );
    }

    private Properties createNotifierProperties( Map configuration )
    {
        Properties notifierProperties = new Properties();

        for ( Iterator i = configuration.keySet().iterator(); i.hasNext(); )
        {
            Object key = i.next();

            Object value = configuration.get( key );

            if ( value instanceof String )
            {
                String val = (String) value;
                if ( !"sendOnSuccess".equals( val ) &&
                     !"sendOnFailure".equals( val ) &&
                     !"sendOnError".equals( val ) &&
                     !"sendOnWarning".equals( val ) )
                {
                    if ( !StringUtils.isEmpty( val ) )
                    {
                        notifierProperties.setProperty( (String) key, val );
                    }
                }
            }
        }

        return notifierProperties;
    }

    public void addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        ProjectNotifier notifier = new ProjectNotifier();

        notifier.setType( notifierType );

        // ----------------------------------------------------------------------
        // Needs to be properties ... but data comes in via a Map
        // ----------------------------------------------------------------------

        Properties notifierProperties = createNotifierProperties( configuration );

        boolean sendOnSuccess = convertBoolean( (String) configuration.get( "sendOnSuccess" ) );

        notifier.setSendOnSuccess( sendOnSuccess );

        boolean sendOnFailure = convertBoolean( (String) configuration.get( "sendOnFailure" ) );

        notifier.setSendOnFailure( sendOnFailure );

        boolean sendOnError = convertBoolean( (String) configuration.get( "sendOnError" ) );

        notifier.setSendOnError( sendOnError );

        boolean sendOnWarning = convertBoolean( (String) configuration.get( "sendOnWarning" ) );

        notifier.setSendOnWarning( sendOnWarning );

        notifier.setConfiguration( notifierProperties );

        notifier.setFrom( ProjectNotifier.FROM_USER );

        Project project = getProjectWithAllDetails( projectId );

        project.addNotifier( notifier );

        updateProject( project );
    }

    public void removeNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        ProjectNotifier n = getNotifier( projectId, notifierId );

        if ( n != null )
        {
            if ( n.isFromProject() )
            {
                n.setEnabled( false );

                storeNotifier( n );
            }
            else
            {
                project.removeNotifier( n );

                updateProject( project );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------

    public List getBuildDefinitions( int projectId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        return project.getBuildDefinitions();
    }

    public BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumException
    {
        return store.getDefaultBuildDefinition( projectId );
    }

    public BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        List buildDefinitions = getBuildDefinitions( projectId );

        BuildDefinition buildDefinition = null;

        for ( Iterator i = buildDefinitions.iterator(); i.hasNext(); )
        {
            buildDefinition = (BuildDefinition) i.next();

            if ( buildDefinition.getId() == buildDefinitionId )
            {
                break;
            }
        }

        return buildDefinition;
    }

    public void updateBuildDefinition( int projectId, int buildDefinitionId, Map configuration )
        throws ContinuumException
    {
        BuildDefinition buildDefinition = getBuildDefinition( projectId, buildDefinitionId );

        buildDefinition.setBuildFile( (String) configuration.get( "buildFile" ) );

        buildDefinition.setGoals( (String) configuration.get( "goals" ) );

        buildDefinition.setArguments( (String) configuration.get( "arguments" ) );

        Schedule schedule = getSchedule( new Integer( (String) configuration.get( "schedule" ) ).intValue() );

        buildDefinition.setSchedule( schedule );

        if ( convertBoolean( (String) configuration.get( "defaultForProject" ) ) && !buildDefinition.isDefaultForProject() )
        {
            buildDefinition.setDefaultForProject( true );
            
            BuildDefinition bd = getDefaultBuildDefinition( projectId );

            if ( bd != null )
            {
                bd.setDefaultForProject( false );

                storeBuildDefinition( bd );
            }
        }

        storeBuildDefinition( buildDefinition );
    }

    public BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumException
    {
        try
        {
            return store.storeBuildDefinition( buildDefinition );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing buildDefinition.", ex );
        }
    }

    public void addBuildDefinition( int projectId, Map configuration )
        throws ContinuumException
    {
        BuildDefinition buildDefinition = new BuildDefinition();

        buildDefinition.setBuildFile( (String) configuration.get( "buildFile" ) );

        buildDefinition.setGoals( (String) configuration.get( "goals" ) );

        buildDefinition.setArguments( (String) configuration.get( "arguments" ) );

        Schedule schedule = getSchedule( new Integer( (String) configuration.get( "schedule" ) ).intValue() );

        if ( convertBoolean( (String) configuration.get( "defaultForProject" ) ) )
        {
            buildDefinition.setDefaultForProject( true );
            
            BuildDefinition bd = getDefaultBuildDefinition( projectId );

            if ( bd != null )
            {
                bd.setDefaultForProject( false );

                storeBuildDefinition( bd );
            }
        }

        buildDefinition.setSchedule( schedule );

        Project project = getProjectWithAllDetails( projectId );

        project.addBuildDefinition( buildDefinition );

        updateProject( project );
    }

    public void removeBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        BuildDefinition buildDefinition = getBuildDefinition( projectId, buildDefinitionId );

        if ( buildDefinition != null )
        {
            project.removeBuildDefinition( buildDefinition );

            updateProject( project );
        }
    }

    public void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumException
    {
        try
        {
            store.removeBuildDefinition( buildDefinition );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing build definition.", ex );
        }
    }

    // ----------------------------------------------------------------------
    // Schedule
    // ----------------------------------------------------------------------

    public Schedule getSchedule( int scheduleId )
        throws ContinuumException
    {
        try
        {
            return store.getSchedule( scheduleId );
        }
        catch ( Exception ex )
        {
            throw logAndCreateException( "Error while getting schedule.", ex );
        }
    }

    public Collection getSchedules()
        throws ContinuumException
    {
        return store.getAllSchedulesByName();
    }

    public void addSchedule( Schedule schedule )
        throws ContinuumException
    {
        Schedule s = null;

        try
        {
            s = store.getScheduleByName( schedule.getName() );
        }
        catch ( ContinuumStoreException e )
        {
        }

        if ( s == null )
        {
            s = store.addSchedule( schedule );

            try
            {
                schedulesActivator.activateSchedule( s, this );
            }
            catch ( SchedulesActivationException e )
            {
                throw new ContinuumException( "Error activating schedule " + s.getName() + ".", e );
            }
        }
        else
        {
            throw logAndCreateException( "Can't create schedule. A schedule with the same name already exists.", null );
        }
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumException
    {
        updateSchedule( schedule, true );
    }

    private void updateSchedule( Schedule schedule, boolean updateScheduler )
        throws ContinuumException
    {
        storeSchedule( schedule );

        if ( updateScheduler )
        {
            try
            {
                if ( schedule.isActive() )
                {
                    // I unactivate it before if it's already active
                    schedulesActivator.unactivateSchedule( schedule, this );

                    schedulesActivator.activateSchedule( schedule, this );
                }
                else
                {
                    schedulesActivator.unactivateSchedule( schedule, this );
                }
            }
            catch ( SchedulesActivationException e )
            {
                getLogger().error( "Can't unactivate schedule. You need to restart Continuum.", e );
            }
        }
    }

    public void updateSchedule( int scheduleId, Map configuration )
        throws ContinuumException
    {
        Schedule schedule = getSchedule( scheduleId );

        schedule.setName( (String) configuration.get( "schedule.name" ) );

        schedule.setDescription( (String) configuration.get( "schedule.description" ) );

        schedule.setCronExpression( (String) configuration.get( "schedule.cronExpression" ) );

        schedule.setDelay( new Integer( (String) configuration.get( "schedule.delay" ) ).intValue() );

        schedule.setActive( new Boolean( (String) configuration.get( "schedule.active" ) ).booleanValue() );

        updateSchedule( schedule, true );
    }

    public void removeSchedule( int scheduleId )
        throws ContinuumException
    {
        Schedule schedule = getSchedule( scheduleId );

        try
        {
            schedulesActivator.unactivateSchedule( schedule, this );
        }
        catch ( SchedulesActivationException e )
        {
            getLogger().error( "Can't unactivate schedule. You need to restart Continuum.", e );
        }

        store.removeSchedule( schedule );
    }

    public Schedule storeSchedule( Schedule schedule )
        throws ContinuumException
    {
        try
        {
            return store.storeSchedule( schedule );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing schedule.", ex );
        }
    }

    // ----------------------------------------------------------------------
    // Working copy
    // ----------------------------------------------------------------------

    public File getWorkingDirectory( int projectId )
        throws ContinuumException
    {
        try
        {
            return workingDirectoryService.getWorkingDirectory( store.getProject( projectId ) );
        }
        catch( ContinuumStoreException e )
        {
            throw new ContinuumException( "Can't get files list.", e );
        }
    }

    public String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException
    {
        File workingDirectory = getWorkingDirectory( projectId );

        File fileDirectory = new File( workingDirectory, directory );

        File userFile = new File( fileDirectory, filename );

        try
        {
            return FileUtils.fileRead( userFile );
        }
        catch( IOException e )
        {
            throw new ContinuumException( "Can't read file " + filename, e );
        }
    }

    public List getFiles( int projectId, String userDirectory )
        throws ContinuumException
    {
        File workingDirectory = getWorkingDirectory( projectId );

        return getFiles( workingDirectory, null, userDirectory );
    }

    private List getFiles( File baseDirectory, String currentSubDirectory, String userDirectory )
    {
        List dirs = new ArrayList();

        File workingDirectory = null;

        if ( currentSubDirectory != null )
        {
            workingDirectory = new File( baseDirectory, currentSubDirectory);
        }
        else
        {
            workingDirectory = baseDirectory;
        }

        String[] files = workingDirectory.list();

        for ( int i = 0; i < files.length; i++ )
        {
            File current = new File( workingDirectory, files[i] );

            String currentFile = null;

            if ( currentSubDirectory == null )
            {
                currentFile = files[i];
            }
            else
            {
                currentFile = currentSubDirectory + "/" + files[i];
            }

            if ( userDirectory != null && current.isDirectory() && userDirectory.startsWith( currentFile ) )
            {
                dirs.add( current );

                dirs.add( getFiles( baseDirectory, currentFile, userDirectory ) );
            }
            else
            {
                dirs.add( current );
            }
        }

        return dirs;
    }

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    public ConfigurationService getConfiguration()
    {
        return configurationService;
    }

    public void updateConfiguration( Map configuration )
        throws ContinuumException
    {
        try
        {
            if ( convertBoolean( (String) configuration.get( "conf.enableGuest" ) ) )
            {
                configurationService.setGuestAccountEnabled( true );
            }
            else
            {
                configurationService.setGuestAccountEnabled( false );
            }

            if ( configuration.get( "conf.workingDirectory" ) != null )
            {
                configurationService.setWorkingDirectory( configurationService.getFile( (String) configuration.get( "conf.workingDirectory" ) ) );
            }
            else
            {
                throw new ContinuumException( "working directory can't be null" );
            }

            if ( configuration.get( "conf.buildOutputDirectory" ) != null )
            {
                configurationService.setBuildOutputDirectory( configurationService.getFile( (String) configuration.get( "conf.buildOutputDirectory" ) ) );
            }
            else
            {
                throw new ContinuumException( "build output directory can't be null" );
            }

            if ( configuration.get( "conf.url" ) != null )
            {
                configurationService.setUrl( (String) configuration.get( "conf.url" ) );
            }
            else
            {
                throw new ContinuumException( "base url can't be null" );
            }

            configurationService.setCompanyName( (String) configuration.get( "conf.companyName" ) );

            configurationService.setCompanyLogo( (String) configuration.get( "conf.companyLogo" ) );

            configurationService.setCompanyUrl( (String) configuration.get( "conf.companyUrl" ) );

            configurationService.store();
        }
        catch ( ConfigurationStoringException e )
        {
            throw new ContinuumException( "Can't store configuration.", e );
        }
    }

    public void reloadConfiguration()
        throws ContinuumException
    {
        try
        {
            configurationService.load();
        }
        catch( ConfigurationLoadingException e )
        {
            throw new ContinuumException( "Can't reload configuration.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Security
    // ----------------------------------------------------------------------

    public ContinuumSecurity getSecurity()
    {
        return security;
    }

    // ----------------------------------------------------------------------
    // User
    // ----------------------------------------------------------------------

    public List getUsers()
        throws ContinuumException
    {
        try
        {
            return store.getUsers();
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while getting users.", ex );
        }
    }

    public void addUser( ContinuumUser user )
    {
        store.addUser( user );
    }

    public void addUser( Map configuration )
        throws ContinuumException
    {
        ContinuumUser user = new ContinuumUser();

        user.setUsername( (String) configuration.get( "user.username" ) );

        user.setFullName( (String) configuration.get( "user.fullName" ) );

        user.setPassword( (String) configuration.get( "user.password" ) );

        user.setEmail( (String) configuration.get( "user.email" ) );

        user.setGroup( getUserGroup( new Integer( (String) configuration.get( "user.group" ) ).intValue() ) );

        addUser( user );
    }

    public void updateUser( ContinuumUser user )
        throws ContinuumException
    {
        try
        {
            store.updateUser( user );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing user.", ex );
        }
    }

    public void updateUser( int userId, Map configuration )
        throws ContinuumException
    {
        ContinuumUser user = getUser( userId );

        user.setUsername( (String) configuration.get( "user.username" ) );

        user.setFullName( (String) configuration.get( "user.fullName" ) );

        user.setPassword( (String) configuration.get( "user.password" ) );

        user.setEmail( (String) configuration.get( "user.email" ) );

        user.setGroup( getUserGroup( new Integer( (String) configuration.get( "user.group" ) ).intValue() ) );

        updateUser( user );
    }

    public ContinuumUser getUser( int userId )
        throws ContinuumException
    {
        try
        {
            return store.getUser( userId);
        }
        catch ( Exception ex )
        {
            throw logAndCreateException( "Error while getting user.", ex );
        }
    }

    public void removeUser( int userId )
        throws ContinuumException
    {
        ContinuumUser user = getUser( userId );

        store.removeUser( user );
    }

    // ----------------------------------------------------------------------
    // User Group
    // ----------------------------------------------------------------------

    public List getUserGroups()
        throws ContinuumException
    {
        try
        {
            return store.getUserGroups();
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while getting user groups.", ex );
        }
    }

    public void addUserGroup( UserGroup userGroup )
    {
        store.addUserGroup( userGroup );
    }

    public void addUserGroup( Map configuration )
        throws ContinuumException
    {
        try
        {
            UserGroup userGroup = new UserGroup();

            userGroup.setName( (String) configuration.get( "group.name" ) );

            userGroup.setDescription( (String) configuration.get( "group.description" ) );

            List perms = parsePermissionConf( configuration );

            userGroup.setPermissions( perms );

            addUserGroup( userGroup );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while storing user group.", e );
        }
    }

    public void updateUserGroup( int userGroupId, Map configuration )
        throws ContinuumException
    {
        try
        {
            UserGroup userGroup = getUserGroup( userGroupId );

            userGroup.setName( (String) configuration.get( "group.name" ) );

            userGroup.setDescription( (String) configuration.get( "group.description" ) );

            List perms = parsePermissionConf( configuration );

            userGroup.setPermissions( perms );

            updateUserGroup( userGroup );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing user group.", ex );
        }
    }

    private List parsePermissionConf( Map configuration )
        throws ContinuumStoreException
    {
        List perms = new ArrayList();

        if ( convertBoolean( (String) configuration.get( "group.permission.addProject" ) ) )
        {
            perms.add( store.getPermission( "addProject" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.editProject" ) ) )
        {
            perms.add( store.getPermission( "editProject" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.deleteProject" ) ) )
        {
            perms.add( store.getPermission( "deleteProject" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.buildProject" ) ) )
        {
            perms.add( store.getPermission( "buildProject" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.showProject" ) ) )
        {
            perms.add( store.getPermission( "showProject" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.addBuildDefinition" ) ) )
        {
            perms.add( store.getPermission( "addBuildDefinition" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.editBuildDefinition" ) ) )
        {
            perms.add( store.getPermission( "editBuildDefinition" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.deleteBuildDefinition" ) ) )
        {
            perms.add( store.getPermission( "deleteBuildDefinition" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.addNotifier" ) ) )
        {
            perms.add( store.getPermission( "addNotifier" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.editNotifier" ) ) )
        {
            perms.add( store.getPermission( "editNotifier" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.deleteNotifier" ) ) )
        {
            perms.add( store.getPermission( "deleteNotifier" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.manageConfiguration" ) ) )
        {
            perms.add( store.getPermission( "manageConfiguration" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.manageSchedule" ) ) )
        {
            perms.add( store.getPermission( "manageSchedule" ) );
        }

        if ( convertBoolean( (String) configuration.get( "group.permission.manageUsers" ) ) )
        {
            perms.add( store.getPermission( "manageUsers" ) );
        }

        return perms;
    }

    public void updateUserGroup( UserGroup userGroup )
        throws ContinuumException
    {
        try
        {
            store.updateUserGroup( userGroup );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while storing user group.", e );
        }
    }

    public UserGroup getUserGroup( int userGroupId )
        throws ContinuumException
    {
        try
        {
            return store.getUserGroup( userGroupId);
        }
        catch ( Exception ex )
        {
            throw logAndCreateException( "Error while getting user group.", ex );
        }
    }

    public void removeUserGroup( int userGroupId )
        throws ContinuumException
    {
        UserGroup group = getUserGroup( userGroupId );

        store.removeUserGroup( group );
    }

    private boolean convertBoolean( String value )
    {
        if ( "true".equalsIgnoreCase( value ) || "on".equalsIgnoreCase( value ) || "yes".equalsIgnoreCase( value ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // Lifecycle Management
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        getLogger().info( "Initializing Continuum." );

        File wdFile = new File( workingDirectory );

        if ( wdFile.exists() )
        {
            if ( !wdFile.isDirectory() )
            {
                throw new InitializationException(
                    "The specified working directory isn't a directory: " + "'" + wdFile.getAbsolutePath() + "'." );
            }
        }
        else
        {
            if ( !wdFile.mkdirs() )
            {
                throw new InitializationException(
                    "Could not making the working directory: " + "'" + wdFile.getAbsolutePath() + "'." );
            }
        }

        getLogger().info( "Showing all projects: " );

        for ( Iterator it = store.getAllProjectsByName().iterator(); it.hasNext(); )
        {
            Project project = (Project) it.next();

            if ( project.getState() != ContinuumProjectState.NEW &&
                 project.getState() != ContinuumProjectState.OK &&
                 project.getState() != ContinuumProjectState.FAILED &&
                 project.getState() != ContinuumProjectState.ERROR )
            {
                project.setState( project.getOldState() );

                project.setOldState( 0 );

                try
                {
                    store.updateProject( project );
                }
                catch( ContinuumStoreException e )
                {
                    throw new InitializationException( "Database is corrupted.", e );
                }
            }
            
            getLogger().info( " " + project.getId() + ":" + project.getName() + ":" + project.getExecutorId() );
        }
    }

    public void start()
        throws StartingException
    {
        startMessage();

        try
        {
            initializer.initialize();

            configurationService.load();

            // ----------------------------------------------------------------------
            // Activate all the Build settings in the system
            // ----------------------------------------------------------------------

            schedulesActivator.activateSchedules( this );
        }
        catch ( SchedulesActivationException e )
        {
            throw new StartingException( "Error activating schedules.", e );
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new StartingException( "Error loading the Continuum configuration.", e );
        }
        catch ( ContinuumInitializationException e )
        {
            throw new StartingException( "Cannot initializing Continuum for the first time.", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        try
        {
            configurationService.store();
        }
        catch ( ConfigurationStoringException e )
        {
            throw new StoppingException( "Error storing the Continuum configuration.", e );
        }

        stopMessage();
    }

    public Collection getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        try
        {
            ArrayList buildResults = new ArrayList( store.getProjectWithBuilds( projectId ).getBuildResults() );

            Collections.reverse( buildResults );

            return buildResults;
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Unable to get builds for project", e );
        }
    }

    // ----------------------------------------------------------------------
    // Workflow
    // ----------------------------------------------------------------------

    private void executeAction( String actionName, Map context )
        throws ContinuumException
    {
        try
        {
            Action action = actionManager.lookup( actionName );

            action.execute( context );
        }
        catch ( ActionNotFoundException e )
        {
            throw new ContinuumException( "Error while executing the action '" + actionName + "'.", e );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while executing the action '" + actionName + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Logging
    // ----------------------------------------------------------------------

    private ContinuumException logAndCreateException( String message, Throwable cause )
    {
        if ( cause instanceof ContinuumObjectNotFoundException )
        {
            return new ContinuumException( "No such object.", cause );
        }

        getLogger().error( message, cause );

        return new ContinuumException( message, cause );
    }

    // ----------------------------------------------------------------------
    // Build settings
    // ----------------------------------------------------------------------

    // core

    public void updateProject( Project project )
        throws ContinuumException
    {
        try
        {
            boolean removeWorkingDirectory = false;

            Project p = store.getProject( project.getId() );

            if ( !p.getScmUrl().equals( project.getScmUrl() ) )
            {
                removeWorkingDirectory = true;
            }

            if ( StringUtils.isEmpty( p.getScmTag() ) && !StringUtils.isEmpty( project.getScmTag() ) )
            {
                removeWorkingDirectory = true;
            }
            else if ( !StringUtils.isEmpty( p.getScmTag() ) && StringUtils.isEmpty( project.getScmTag() ) )
            {
                removeWorkingDirectory = true;
            }
            else if ( !StringUtils.isEmpty( p.getScmTag() ) && !p.getScmTag().equals( project.getScmTag() ) )
            {
                removeWorkingDirectory = true;
            }

            if ( removeWorkingDirectory )
            {
                File workingDirectory = getWorkingDirectory( project.getId() );

                FileUtils.deleteDirectory( workingDirectory );
            }

            if ( StringUtils.isEmpty( project.getScmTag() ) )
            {
                project.setScmTag( null );
            }

            store.updateProject( project );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while updating project.", ex );
        }
        catch ( IOException ex )
        {
            throw logAndCreateException( "Error while updating project.", ex );
        }
    }

    public void removeNotifier( ProjectNotifier notifier )
        throws ContinuumException
    {
        try
        {
            store.removeNotifier( notifier );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing notifier.", ex );
        }
    }

    public ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumException
    {
        try
        {
            return store.storeNotifier( notifier );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while storing notifier.", ex );
        }
    }

    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    private void startMessage()
    {
        getLogger().info( "Starting Continuum." );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String banner = StringUtils.repeat( "-", getVersion().length() );

        getLogger().info( "" );
        getLogger().info( "" );
        getLogger().info( "< Continuum " + getVersion() + " started! >" );
        getLogger().info( "-----------------------" + banner );
        getLogger().info( "       \\   ^__^" );
        getLogger().info( "        \\  (oo)\\_______" );
        getLogger().info( "           (__)\\       )\\/\\" );
        getLogger().info( "               ||----w |" );
        getLogger().info( "               ||     ||" );
        getLogger().info( "" );
        getLogger().info( "" );
    }

    private void stopMessage()
    {
        getLogger().info( "Stopping Continuum." );

        getLogger().info( "Continuum stopped." );
    }

    private String getVersion()
    {
        try
        {
            Properties properties = new Properties();

            String name = "META-INF/maven/org.apache.maven.continuum/continuum-core/pom.properties";

            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream( name );

            if ( resourceAsStream == null )
            {
                return "unknown";
            }

            properties.load( resourceAsStream );

            return properties.getProperty( "version", "unknown" );
        }
        catch ( IOException e )
        {
            return "unknown";
        }
    }

    public Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException
    {
        try
        {
            return store.getProjectWithCheckoutResult( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public List getAllProjectsWithAllDetails( int start, int end )
    {
        return store.getAllProjectsWithAllDetails();
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumException
    {
        try
        {
            return store.getProjectWithAllDetails( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }

    public Project getProjectWithBuilds( int projectId )
        throws ContinuumException
    {
        try
        {
            return store.getProjectWithBuilds( projectId );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            throw new ContinuumException( "Unable to find the requested project", e );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error retrieving the requested project", e );
        }
    }
}
