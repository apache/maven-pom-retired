package org.apache.maven.continuum.core;

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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManagerException;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.scm.queue.CheckOutTask;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class DefaultContinuumCore
    extends AbstractLogEnabled
    implements ContinuumCore, Initializable, Startable
{
    private final static String CONTINUUM_VERSION = "1.0-alpha-2-SNAPSHOT";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // TODO: look up these requiremetns in start() to have better control of the
    //       application initialization sequence. The application should make sure
    //       that the database is properly initialized before starting the store.

    /**
     * @requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @requirement
     */
    private ContinuumProjectBuilderManager projectBuilderManager;

    /**
     * @requirement
     */
    private TaskQueue buildQueue;

    /**
     * @requirement
     */
    private TaskQueue checkOutQueue;

    /**
     * @requirement
     */
    private ContinuumStore store;

    /**
     * @requirement
     */
    private ContinuumScm scm;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @configuration
     */
    private String workingDirectory;

    /**
     * @configuration
     */
    private String appHome;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Collection getProjects()
        throws ContinuumException
    {
        try
        {
            return store.getAllProjects();
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while getting all projects.", e );
        }
    }

    public ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumException
    {
        try
        {
            return store.getLatestBuildForProject( id );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while getting the last build for project '" + id + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Collection addProjectsFromUrl( String url, String executorId )
        throws ContinuumException
    {
        URL u;

        try
        {
            u = new URL( url );
        }
        catch ( MalformedURLException e )
        {
            throw logAndCreateException( "Invalid URL.", e );
        }

        return addProjectsFromUrl( u, executorId );
    }

    public Collection addProjectsFromUrl( URL url, String executorId )
        throws ContinuumException
    {
        ContinuumProjectBuilder projectBuilder = getProjectBuilder( executorId );

        ContinuumProjectBuildingResult result;

        try
        {
            result = projectBuilder.buildProjectsFromMetadata( url );
        }
        catch ( ContinuumProjectBuilderException e )
        {
            throw logAndCreateException( "Error while creating projects from URL.", e );
        }

        for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            validateProject( project );
        }

        List ids = new ArrayList( result.getProjects().size() );

        // TODO: Update from metadata in the initial checkout?

        for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            project = addProjectAndCheckOutSources( project, project.getExecutorId() );

            ids.add( project.getId() );
        }

        return ids;
    }

    public String addProjectFromScm( String scmUrl,
                                     String executorId,
                                     String projectName,
                                     String nagEmailAddress,
                                     String version,
                                     String commandLineArugments,
                                     Properties configuration )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Create the stub project
        // ----------------------------------------------------------------------

        ContinuumProject project = new ContinuumProject();

        project.setScmUrl( scmUrl );

        project.setExecutorId( executorId );

        project.setName( projectName );

        project.setNagEmailAddress( nagEmailAddress );

        project.setVersion( version );

        project.setCommandLineArguments( commandLineArugments );

        project.setConfiguration( configuration );

        // ----------------------------------------------------------------------
        // Validate the project
        // ----------------------------------------------------------------------

        validateProject( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        doTempCheckOut( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = addProjectAndCheckOutSources( project, executorId );

        updateProjectFromCheckOut( project );

        return project.getId();
    }

    public void updateProjectFromScm( String projectId )
        throws ContinuumException
    {
        ContinuumProject project;

        try
        {
            project = store.getProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while updating project from SCM.", ex );
        }

        File workingDirectory = new File( project.getWorkingDirectory() );

        if ( !workingDirectory.exists() )
        {
            getLogger().warn( "Creating missing working directory for project '" + project.getName() + "'." );

            if ( !workingDirectory.exists() )
            {
                throw logAndCreateException( "Could not make missing working directory for " +
                                             "project '" + project.getName() + "'." );
            }
        }

        // ----------------------------------------------------------------------
        // Update the source code
        // ----------------------------------------------------------------------

        try
        {
            scm.updateProject( project );
        }
        catch ( ContinuumScmException e )
        {
            throw logAndCreateException( "Error while updating project.", e );
        }

        updateProjectFromCheckOut( project );
    }

    public void updateProject( String projectId,
                               String name,
                               String scmUrl,
                               String nagEmailAddress,
                               String version,
                               String commandLineArguments )
        throws ContinuumException
    {
        try
        {
            commandLineArguments = StringUtils.clean( commandLineArguments );

            store.updateProject( projectId,
                                 name,
                                 scmUrl,
                                 nagEmailAddress,
                                 version,
                                 commandLineArguments );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while updating the project.", e );
        }
    }

    public void updateProjectConfiguration( String projectId, Properties configuration )
        throws ContinuumException
    {
        try
        {
            store.updateProjectConfiguration( projectId, configuration );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while updating project configuration.", ex );
        }
    }

    public void removeProject( String projectId )
        throws ContinuumException
    {
        try
        {
            store.removeProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing project.", ex );
        }
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumException
    {
        try
        {
            ContinuumProject project = store.getProject( projectId );

            return project;
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting all projects.", ex );
        }
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        try
        {
            return store.getAllProjects();
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting all projects.", ex );
        }
    }

    public CheckOutScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumException
    {
        try
        {
            return store.getCheckOutScmResultForProject( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while getting check out scm result for project.", ex );
        }
    }

    public void buildProject( String projectId, boolean force )
        throws ContinuumException
    {
        try
        {
            ContinuumProject project = store.getProject( projectId );

            getLogger().info( "Enqueuing '" + project.getName() + "'." );

            buildQueue.put( new BuildProjectTask( projectId, force ) );
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

    // ----------------------------------------------------------------------
    // Build inforation
    // ----------------------------------------------------------------------

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumException
    {
        try
        {
            return store.getBuild( buildId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Unable to retrieve build with id = " + buildId, e );
        }
    }

    public Collection getBuildsForProject( String projectId )
        throws ContinuumException
    {
        try
        {
            return store.getBuildsForProject( projectId, 0, 0 );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Cannot retrieve builds for project with id = " + projectId, e );
        }
    }

    public ContinuumBuildResult getBuildResultForBuild( String buildId )
        throws ContinuumException
    {
        try
        {
            return store.getBuildResultForBuild( buildId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Cannot retrieve build result for build with id = " + buildId, e );
        }
    }

    public Collection getChangedFilesForBuild( String buildId )
        throws ContinuumException
    {
        try
        {
            return store.getChangedFilesForBuild( buildId );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Cannot retrieve build result for build with id = " + buildId, e );
        }
    }

    // ----------------------------------------------------------------------
    // ContinuumBuildExecutor
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutor getBuildExecutor( String id )
        throws ContinuumException
    {
        return buildExecutorManager.getBuildExecutor( id );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ContinuumProjectBuilder getProjectBuilder( String projectBuilderId )
        throws ContinuumException
    {
        try
        {
            return projectBuilderManager.getProjectBuilder( projectBuilderId );
        }
        catch ( ContinuumProjectBuilderManagerException e )
        {
            throw logAndCreateException( "Error while getting project builder '" + projectBuilderId + "'.", e );
        }
    }

    private void validateProject( ContinuumProject project )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Make sure that the builder id is correct before starting to check
        // stuff out
        // ----------------------------------------------------------------------

        if ( !buildExecutorManager.hasBuildExecutor( project.getExecutorId() ) )
        {
            throw logAndCreateException( "No such executor with id '" + project.getExecutorId() + "'." );
        }

        try
        {
            if ( store.getProjectByName( project.getName() ) != null )
            {
                throw new ContinuumException( "A project with the name '" + project.getName() + "' already exist." );
            }

//            if ( getProjectByScmUrl( scmUrl ) != null )
//            {
//                throw new ContinuumStoreException( "A project with the scm url '" + scmUrl + "' already exist." );
//            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while validating the project.", e );
        }

        // ----------------------------------------------------------------------
        // Validate each field
        // ----------------------------------------------------------------------

        project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );
    }

    private ContinuumProject addProjectAndCheckOutSources( ContinuumProject project, String executorId )
        throws ContinuumException
    {
        String projectId;

        File projectWorkingDirectory;

        try
        {
            // ----------------------------------------------------------------------
            // Store the project
            // ----------------------------------------------------------------------

            projectId = store.addProject( project.getName(),
                                          project.getScmUrl(),
                                          project.getNagEmailAddress(),
                                          project.getVersion(),
                                          project.getCommandLineArguments(),
                                          executorId,
                                          null,
                                          project.getConfiguration() );

            // ----------------------------------------------------------------------
            // Set the working directory
            // ----------------------------------------------------------------------

            projectWorkingDirectory = new File( workingDirectory, projectId );

            if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
            {
                throw logAndCreateException( "Could not make the working directory for the project " +
                                             "'" + projectWorkingDirectory.getAbsolutePath() + "'." );
            }

            project.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );

            // ----------------------------------------------------------------------
            // Check out the project
            // ----------------------------------------------------------------------

            store.setWorkingDirectory( projectId, projectWorkingDirectory.getAbsolutePath() );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Exception while adding project.", ex );
        }

        try
        {
            CheckOutTask checkOutTask = new CheckOutTask( projectId, projectWorkingDirectory );

            checkOutQueue.put( checkOutTask );
        }
        catch ( TaskQueueException e )
        {
            throw logAndCreateException( "Exception while adding the project to the check out queue.", e );
        }

        return getProject( projectId );
    }

    private void doTempCheckOut( ContinuumProject project )
        throws ContinuumException
    {
        File checkoutDirectory = new File( workingDirectory, "temp-project" );

        if ( checkoutDirectory.exists() )
        {
            try
            {
                FileUtils.cleanDirectory( checkoutDirectory );
            }
            catch ( IOException ex )
            {
                throw logAndCreateException( "Error while cleaning out " + checkoutDirectory.getAbsolutePath() );
            }
        }
        else
        {
            if ( !checkoutDirectory.mkdirs() )
            {
                throw logAndCreateException( "Could not make the check out directory " +
                                             "'" + checkoutDirectory.getAbsolutePath() + "'." );
            }
        }

        // TODO: Get the list of files to check out from the builder.
        // Maven 2: pom.xml, Maven 1: project.xml, Ant: all? build.xml?

        try
        {
            scm.checkOut( project, checkoutDirectory );
        }
        catch ( ContinuumScmException e )
        {
            throw logAndCreateException( "Error while checking out the project.", e );
        }
    }

    private void updateProjectFromCheckOut( ContinuumProject project )
        throws ContinuumException
    {
        getLogger().info( "Updating project '" + project.getName() + "'." );

        // Save the ID now in case the builder fucks it up
        String id = project.getId();

        // ----------------------------------------------------------------------
        // Make a new descriptor
        // ----------------------------------------------------------------------

        ContinuumBuildExecutor builder = buildExecutorManager.getBuildExecutor( project.getExecutorId() );

        try
        {
            builder.updateProjectFromCheckOut( new File( project.getWorkingDirectory() ), project );
        }
        catch ( ContinuumBuildExecutorException e )
        {
            throw logAndCreateException( "Error while updating project from check out.", e );
        }

        // ----------------------------------------------------------------------
        // Store the new descriptor
        // ----------------------------------------------------------------------

        try
        {
            store.updateProject( id,
                                 project.getName(),
                                 project.getScmUrl(),
                                 project.getNagEmailAddress(),
                                 project.getVersion(),
                                 project.getCommandLineArguments() );

            store.updateProjectConfiguration( id, project.getConfiguration() );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while storing the updated project.", e );
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
                throw new InitializationException( "The specified working directory isn't a directory: " +
                                                   "'" + wdFile.getAbsolutePath() + "'." );
            }
        }
        else
        {
            if ( !wdFile.mkdirs() )
            {
                throw new InitializationException( "Could not making the working directory: " +
                                                   "'" + wdFile.getAbsolutePath() + "'." );
            }
        }

        getLogger().info( "Showing all projects: " );

        try
        {
            for ( Iterator it = store.getAllProjects().iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                getLogger().info( " " + project.getId() + ":" + project.getName() + ":" + project.getExecutorId() );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new InitializationException( "Couldn't load projects.", e );
        }
    }

    public void start()
        throws StartingException
    {
        getLogger().info( "Starting Continuum." );

        // ----------------------------------------------------------------------
        // Check for projects that's in the "checking out" state and enqueue
        // them to ensure that they're always checked out.
        // ----------------------------------------------------------------------

        getLogger().info( "Checking for projects that has to be checked out." );

        try
        {
            for ( Iterator it = store.getAllProjects().iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                if ( project.getState() != ContinuumProjectState.CHECKING_OUT )
                {
                    continue;
                }

                getLogger().info( "Adding '" + project.getName() + "' to the check out queue." );

                CheckOutTask checkOutTask = new CheckOutTask( project.getId(),
                                                              new File( project.getWorkingDirectory() ) );

                checkOutQueue.put( checkOutTask );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new StartingException( "Error while enqueuing all projects in the 'checking out' state to " +
                                         "the check out queue.", e );
        }
        catch ( TaskQueueException e )
        {
            throw new StartingException( "Error while enqueuing all projects in the 'checking out' state to " +
                                         "the check out queue.", e );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String banner = StringUtils.repeat( "-", CONTINUUM_VERSION.length() );

        getLogger().info( "" );
        getLogger().info( "" );
        getLogger().info( "< Continuum " + CONTINUUM_VERSION + " started! >" );
        getLogger().info( "-----------------------" + banner );
        getLogger().info( "       \\   ^__^" );
        getLogger().info( "        \\  (oo)\\_______" );
        getLogger().info( "           (__)\\       )\\/\\" );
        getLogger().info( "               ||----w |" );
        getLogger().info( "               ||     ||" );
        getLogger().info( "" );
        getLogger().info( "" );
    }

    public void stop()
    {
        getLogger().info( "Stopping Continuum." );

        getLogger().info( "Continuum stopped." );
    }

    private ContinuumException logAndCreateException( String message )
    {
        getLogger().error( message );

        return new ContinuumException( message );
    }

    private ContinuumException logAndCreateException( String message, Throwable cause )
    {
        getLogger().error( message, cause );

        return new ContinuumException( message, cause );
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    public ContinuumStore getStore()
    {
        return store;
    }

    public ContinuumScm getScm()
    {
        return scm;
    }

    public ContinuumProjectBuilderManager getProjectBuilderManager()
    {
        return projectBuilderManager;
    }

    public BuildExecutorManager getBuildExecutorManager()
    {
        return buildExecutorManager;
    }

    public TaskQueue getBuildQueue()
    {
        return buildQueue;
    }

    public TaskQueue getCheckOutQueue()
    {
        return checkOutQueue;
    }

    public String getWorkingDirectory()
    {
        return workingDirectory;
    }
}

