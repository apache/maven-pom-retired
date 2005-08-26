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
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenOneContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ProjectSorter;
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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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

    // ----------------------------------------------------------------------
    // Moved from core
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.configuration
     */
    private String workingDirectory;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public Collection getProjects()
        throws ContinuumException
    {
        return store.getAllProjectsByName();
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
        throws ContinuumException
    {
        try
        {
            return store.getLatestBuildResultForProject( projectId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while getting the last build for project '" + projectId + "'.", e );
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
            throw new ContinuumException( "Error while getting the queue snapshot.", e );
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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void removeProject( int projectId )
        throws ContinuumException
    {
        try
        {
            store.removeProject( store.getProject( projectId ) );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing project.", ex );
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
        for ( Iterator i = getProjects().iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            buildProject( project.getId(), trigger );
        }

        /*
        try
        {
            for ( Iterator i = getProjectsInBuildOrder().iterator(); i.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) i.next();

                buildProject( project.getId(), force );
            }
        }
        catch ( CycleDetectedException e )
        {
            getLogger().warn( "Cycle detected while sorting projects for building, falling back to unsorted build." );

            for ( Iterator i = getProjects().iterator(); i.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) i.next();

                buildProject( project.getId(), force );
            }
        }
        */
    }

    public void buildProject( int projectId )
        throws ContinuumException
    {
        buildProject( projectId, ContinuumProjectState.TRIGGER_FORCED );
    }

    public void buildProject( int projectId, int trigger )
        throws ContinuumException
    {
        try
        {
            Project project = store.getProject( projectId );

            getLogger().info( "Enqueuing '" + project.getName() + "'." );

            buildQueue.put( new BuildProjectTask( projectId, trigger ) );
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
            return store.getBuildOutput( buildId, projectId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Exception while getting build result for project.", e );
        }
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

        getLogger().info( "Created " + result.getProjects().size() + " projects." );
        getLogger().info( "Created " + result.getProjectGroups().size() + " project groups." );
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
        ProjectNotifier notifier = getNotifier( projectId, notifierId );

        Properties notifierProperties = createNotifierProperties( configuration );

        notifier.setConfiguration( notifierProperties );

        storeNotifier( notifier );
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
                notifierProperties.setProperty( (String) key, (String) value );
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

        notifier.setConfiguration( notifierProperties );

        Project project = getProject( projectId );

        project.addNotifier( notifier );

        updateProject( project );
    }

    public void removeNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        ProjectNotifier n = getNotifier( projectId, notifierId );

        if ( n != null )
        {
            removeNotifier( n );
        }
    }

    // ----------------------------------------------------------------------
    // Build Definition
    // ----------------------------------------------------------------------

    public BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        Project project = getProjectWithAllDetails( projectId );

        List buildDefinitions = project.getBuildDefinitions();

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

    // ----------------------------------------------------------------------
    // Lifecylce Management
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

            getLogger().info( " " + project.getId() + ":" + project.getName() + ":" + project.getExecutorId() );
        }
    }

    public void start()
        throws StartingException
    {
        startMessage();

        try
        {
            configurationService.load();

            if ( !configurationService.isInitialized() )
            {
                initializer.initialize();

                configurationService.setInitialized( true );
            }

            // ----------------------------------------------------------------------
            // Activate all the Build settings in the system
            // ----------------------------------------------------------------------

            schedulesActivator.activateSchedules( this );
        }
        catch ( SchedulesActivationException e )
        {
            throw new StartingException( "Error activating build settings.", e );
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
            return store.getProjectWithBuilds( projectId ).getBuildResults();
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
            store.updateProject( project );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing project.", ex );
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
            throw logAndCreateException( "Error while removing notifier.", ex );
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
