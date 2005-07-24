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

import org.apache.maven.continuum.build.settings.BuildSettingsActivationException;
import org.apache.maven.continuum.build.settings.BuildSettingsActivator;
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.AddProjectToCheckOutQueueAction;
import org.apache.maven.continuum.core.action.CreateProjectsFromMetadata;
import org.apache.maven.continuum.core.action.StoreProjectAction;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.initialization.ContinuumInitializationException;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.maven.MavenOneContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.scheduler.ContinuumScheduler;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ProjectSorter;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.action.ActionNotFoundException;
import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class DefaultContinuum
    extends AbstractLogEnabled
    implements Continuum, Startable
{
    /**
     * @plexus.requirement
     */
    private ContinuumCore core;

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private ContinuumScheduler scheduler;

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
    private BuildSettingsActivator buildSettingsActivator;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static final String DEFAULT_PROJECT_GROUP_NAME = "Default Project";

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public Collection getProjects()
        throws ContinuumException
    {
        return core.getProjects();
    }

    public ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumException
    {
        return core.getLatestBuildForProject( id );
    }

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    public boolean isInBuildingQueue( String id )
        throws ContinuumException
    {
        return core.isBuilding( id );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void removeProject( String projectId )
        throws ContinuumException
    {
        core.removeProject( projectId );
    }

    public void checkoutProject( String id )
        throws ContinuumException
    {
        Map context = new HashMap();

        context.put( AddProjectToCheckOutQueueAction.KEY_PROJECT_ID, id );

        executeAction( "add-project-to-checkout-queue", context );
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumException
    {
        return core.getProject( projectId );
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return core.getAllProjects( start, end );
    }

    public ScmResult getScmResultForProject( String projectId )
        throws ContinuumException
    {
        return core.getScmResultForProject( projectId );
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public void buildProjects()
        throws ContinuumException
    {
        buildProjects( true );
    }

    public void buildProjects( boolean force )
        throws ContinuumException
    {
        for ( Iterator i = getProjects().iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            buildProject( project.getId(), force );
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

    public void buildProject( String projectId )
        throws ContinuumException
    {
        core.buildProject( projectId, true );
    }

    public void buildProject( String projectId, boolean force )
        throws ContinuumException
    {
        core.buildProject( projectId, force );
    }

    public void buildProjectGroup( ContinuumProjectGroup projectGroup, ContinuumBuildSettings buildSettings )
        throws ContinuumException
    {
        Set projects = projectGroup.getProjects();

        for ( Iterator j = projects.iterator(); j.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) j.next();

            try
            {
                buildProject( project.getId(), false );
            }
            catch ( ContinuumException ex )
            {
                getLogger().error( "Could not enqueue project: " + project.getId() + " ('" + project.getName() + "').", ex );
            }
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
    // Build inforation
    // ----------------------------------------------------------------------

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumException
    {
        return core.getBuild( buildId );
    }

    public Collection getBuildsForProject( String projectId )
        throws ContinuumException
    {
        return core.getBuildsForProject( projectId );
    }

    public Collection getChangedFilesForBuild( String buildId )
        throws ContinuumException
    {
        return core.getChangedFilesForBuild( buildId );
    }

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    public String addAntProject( AntProject project )
        throws ContinuumException
    {
        project.setExecutorId( AntBuildExecutor.ID );

        return executeAddProjectFromScmActivity( project );
    }

    public AntProject getAntProject( String projectId )
        throws ContinuumException
    {
        return (AntProject) core.getProject( projectId );
    }

    public void updateAntProject( AntProject project )
        throws ContinuumException
    {
        executeUpdateProjectActivity( project );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl,
                                                       MavenOneContinuumProjectBuilder.ID,
                                                       MavenOneBuildExecutor.ID );
    }

    public String addMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        project.setExecutorId( MavenOneBuildExecutor.ID );

        return executeAddProjectFromScmActivity( project );
    }

    public MavenOneProject getMavenOneProject( String projectId )
        throws ContinuumException
    {
        return (MavenOneProject) core.getProject( projectId );
    }

    public void updateMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        executeUpdateProjectActivity( project );
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        return executeAddProjectsFromMetadataActivity( metadataUrl,
                                                       MavenTwoContinuumProjectBuilder.ID,
                                                       MavenTwoBuildExecutor.ID );
    }

    public String addMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        project.setExecutorId( MavenTwoBuildExecutor.ID );

        return executeAddProjectFromScmActivity( project );
    }

    public MavenTwoProject getMavenTwoProject( String projectId )
        throws ContinuumException
    {
        return (MavenTwoProject) core.getProject( projectId );
    }

    public void updateMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        executeUpdateProjectActivity( project );
    }

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    public String addShellProject( ShellProject project )
        throws ContinuumException
    {
        project.setExecutorId( ShellBuildExecutor.ID );

        return executeAddProjectFromScmActivity( project );
    }

    public ShellProject getShellProject( String projectId )
        throws ContinuumException
    {
        return (ShellProject) core.getProject( projectId );
    }

    public void updateShellProject( ShellProject project )
        throws ContinuumException
    {
        executeUpdateProjectActivity( project );
    }

    // ----------------------------------------------------------------------
    // Activities. These should end up as workflows in werkflow
    // ----------------------------------------------------------------------

    private void executeUpdateProjectActivity( ContinuumProject project )
        throws ContinuumException
    {
        core.updateProject( project );
    }

    private String executeAddProjectFromScmActivity( ContinuumProject project )
        throws ContinuumException
    {
        Map context = new HashMap();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT, project );

        executeAction( "validate-project", context );

        executeAction( "store-project", context );

        executeAction( "add-project-to-checkout-queue", context );

        return (String) context.get( StoreProjectAction.KEY_PROJECT_ID );
    }

    private ContinuumProjectBuildingResult executeAddProjectsFromMetadataActivity( String metadataUrl,
                                                                                   String projectBuilderId,
                                                                                   String buildExecutorId )
        throws ContinuumException
    {
        Map context = new HashMap();

        context.put( CreateProjectsFromMetadata.KEY_PROJECT_BUILDER_ID, projectBuilderId );

        context.put( CreateProjectsFromMetadata.KEY_URL, metadataUrl );

        context.put( CreateProjectsFromMetadata.KEY_WORKING_DIRECTORY, core.getWorkingDirectory() );

        // ----------------------------------------------------------------------
        // Create the projects from the URL
        // ----------------------------------------------------------------------

        executeAction( "create-projects-from-metadata", context );

        ContinuumProjectBuildingResult result = (ContinuumProjectBuildingResult)
            context.get( CreateProjectsFromMetadata.KEY_PROJECT_BUILDING_RESULT );

        // ----------------------------------------------------------------------
        // Look for any warnings.
        // ----------------------------------------------------------------------

        if ( result.getWarnings().size() > 0 )
        {
            return result;
        }

        // ----------------------------------------------------------------------
        // Save any new project groups that we've found. Currenly all projects
        // will go into the first project group in the list.
        // ----------------------------------------------------------------------

        ContinuumProjectGroup projectGroup = null;

        for ( Iterator it = result.getProjectGroups().iterator(); it.hasNext(); )
        {
            projectGroup = (ContinuumProjectGroup) it.next();

            try
            {
                try
                {
                    projectGroup = store.getProjectGroupByGroupId( projectGroup.getGroupId() );
                }
                catch ( ContinuumObjectNotFoundException e )
                {
                    Map pgContext = new HashMap();

                    pgContext.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT_GROUP, projectGroup );

                    executeAction( "validate-project-group", pgContext );

                    executeAction( "store-project-group", pgContext );
                }
            }
            catch ( ContinuumStoreException e )
            {
                throw new ContinuumException( "Error while querying for project group.", e );
            }
        }

        // ----------------------------------------------------------------------
        // Save all the projects
        // TODO: Validate all the projects before saving them
        // ----------------------------------------------------------------------

        List projects = result.getProjects();

        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            project.setExecutorId( buildExecutorId );

            project.setProjectGroup( projectGroup );

            context.put( AbstractContinuumAction.KEY_UNVALIDATED_PROJECT, project );

            executeAction( "validate-project", context );

            executeAction( "store-project", context );

            project.setId( (String) context.get( StoreProjectAction.KEY_PROJECT_ID ) );

            executeAction( "add-project-to-checkout-queue", context );
        }

        return result;
    }

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    // This whole section needs a scrub but will need to be dealt with generally
    // when we add schedules and profiles to the mix.

    public ContinuumNotifier getNotifier( String projectId, String notifierType )
        throws ContinuumException
    {
        ContinuumProject project = core.getProject( projectId );

        List notifiers = project.getNotifiers();

        ContinuumNotifier notifier = null;

        for ( Iterator i = notifiers.iterator(); i.hasNext(); )
        {
            notifier = (ContinuumNotifier) i.next();

            if ( notifier.getType().equals( notifierType ) )
            {
                break;
            }
        }

        return notifier;
    }

    public void updateNotifier( String projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        ContinuumNotifier notifier = getNotifier( projectId, notifierType );

        Properties notifierProperties = createNotifierProperties( configuration );

        notifier.setConfiguration( notifierProperties );

        core.storeNotifier( notifier );
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

    public void addNotifier( String projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        ContinuumNotifier notifier = new ContinuumNotifier();

        notifier.setType( notifierType );

        // ----------------------------------------------------------------------
        // Needs to be properties ... but data comes in via a Map
        // ----------------------------------------------------------------------

        Properties notifierProperties = createNotifierProperties( configuration );

        notifier.setConfiguration( notifierProperties );

        ContinuumProject project = core.getProject( projectId );

        project.addNotifier( notifier );

        core.updateProject( project );
    }

    public void removeNotifier( String projectId, String notifierType )
        throws ContinuumException
    {
        ContinuumNotifier n = getNotifier( projectId, notifierType );

        if ( n != null )
        {
            core.removeNotifier( n );
        }
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    public void start()
        throws StartingException
    {
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

            try
            {
                buildSettingsActivator.activateBuildSettings( this );
            }
            catch ( BuildSettingsActivationException e )
            {
                throw new StartingException( "Error activating build settings.", e );
            }

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
    }

    // ----------------------------------------------------------------------
    // Build Scheduling
    // ----------------------------------------------------------------------

    public ContinuumSchedule getSchedule( String scheduleId )
        throws ContinuumException
    {
        try
        {
            return store.getSchedule( scheduleId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting schedule '" + scheduleId + "'.", ex );
        }
    }

    public Collection getSchedules()
        throws ContinuumException
    {
        try
        {
            return store.getSchedules();
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting schedules.", ex );
        }
    }

    public ContinuumSchedule addSchedule( ContinuumSchedule schedule )
        throws ContinuumException
    {
        try
        {
            return store.addSchedule( schedule );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing schedule.", ex );
        }
    }

    public ContinuumSchedule updateSchedule( ContinuumSchedule schedule )
        throws ContinuumException
    {
        try
        {
            return store.updateSchedule( schedule );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing schedule.", ex );
        }
    }

    public void removeSchedule( String scheduleId )
        throws ContinuumException
    {
        try
        {
            store.removeSchedule( scheduleId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing schedule.", ex );
        }
    }

    // ----------------------------------------------------------------------
    // Project scheduling
    // ----------------------------------------------------------------------

    public ContinuumSchedule addProjectToSchedule( ContinuumProject project, ContinuumSchedule schedule )
        throws ContinuumException
    {
        schedule.addProject( project );

        try
        {
            return store.updateSchedule( schedule );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while adding project to schedule.", e );
        }
    }

    public void removeProjectFromSchedule( ContinuumProject project, ContinuumSchedule schedule )
        throws ContinuumException
    {
        schedule.removeProject( project );

        try
        {
            store.updateSchedule( schedule );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while removing project from schedule.", e );
        }
    }

    public ContinuumBuildSettings getDefaultBuildSettings()
    {
        return initializer.getDefaultBuildSettings();
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
        getLogger().error( message, cause );

        return new ContinuumException( message, cause );
    }

    // ----------------------------------------------------------------------
    // Build settings
    // ----------------------------------------------------------------------
}
