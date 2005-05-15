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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
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
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id: DefaultContinuum.java,v 1.7 2005/04/08 12:47:56 trygvis Exp $
 */
public class DefaultContinuumCore
    extends AbstractLogEnabled
    implements ContinuumCore, Initializable, Startable
{
    private final static String DATABASE_INITIALIZED = "database.initialized";

    private final static String CONTINUUM_VERSION = "1.0-alpha-2-SNAPSHOT";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // TODO: look up these requiremetns in start() to have better control of the
    //       application initialization sequence. The application should make sure
    //       that the database is properly initialized before starting the store.

    /** @requirement */
    private BuildExecutorManager buildExecutorManager;

    /** @requirement */
    private ContinuumProjectBuilderManager projectBuilderManager;

    /** @requirement */
    private TaskQueue buildQueue;

    /** @requirement */
    private TaskQueue checkOutQueue;

    /** @requirement */
    private ContinuumStore store;

    /** @requirement */
    private ContinuumScm scm;

    /** @requirement */
    private String appHome;

    /** @configuration */
    private String workingDirectory;

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

    // ----------------------------------------------------------------------
    // Here it would probably be possible to tell from looking at the meta
    // data what type of project handler would be required. We could
    // definitely tell if we were looking at a Maven POM, So for the various
    // POM versions we would know what builder to use, and for an arbitrary
    // ----------------------------------------------------------------------

    // add project meta data
    // create continuum project from project metadata
    // add continuum project to the store
    // setup the project
    // -> check out from scm
    // -> update the project metadata

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
        File pomFile;

        try
        {
            String pom = IOUtil.toString( url.openStream() );

            pomFile = File.createTempFile( "continuum-", "-pom-download" );

            pomFile.deleteOnExit();

            FileUtils.fileWrite( pomFile.getAbsolutePath(), pom );
        }
        catch ( IOException ex )
        {
            logAndCreateException( "Error while downloading the pom.", ex );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumProjectBuilder projectBuilder = getProjectBuilder( executorId );

        ContinuumProjectBuildingResult result;

        try
        {
            result = projectBuilder.createProjectsFromMetadata( url );
        }
        catch ( ContinuumProjectBuilderException e )
        {
            throw logAndCreateException( "Error while creating projects from URL.", e );
        }

        // TODO: Update from metadata in the initial checkout?

        List ids = new ArrayList( result.getProjects().size() );

        for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
        {
            ContinuumProject project = ( ContinuumProject ) it.next();

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

        project.setConfiguration( configuration );

        // ----------------------------------------------------------------------
        // Make sure that the builder id is correct before starting to check
        // stuff out
        // ----------------------------------------------------------------------

        if ( !buildExecutorManager.hasBuilder( executorId ) )
        {
            logAndCreateException( "No such executor with id '" + executorId + "'." );
        }

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
                logAndCreateException( "Could not make missing working directory for " +
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
            logAndCreateException( "Error while updating project.", e );
        }

        updateProjectFromCheckOut( project );
    }

    public void updateProject( String projectId, String name, String scmUrl, String nagEmailAddress, String version )
        throws ContinuumException
    {
        try
        {
            store.updateProject( projectId,
                                 name,
                                 scmUrl,
                                 nagEmailAddress,
                                 version );
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
            logAndCreateException( "Error while removing project.", ex );
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
    // Ant Projects
    // ----------------------------------------------------------------------

    public void addAntProject( AntProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_TARGETS, project.getTargets() );

        addProjectFromScm( project.getScmUrl(),
                           AntBuildExecutor.ID,
                           project.getName(),
                           project.getNagEmailAddress(),
                           project.getVersion(),
                           configuration );
    }

    public AntProject getAntProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        AntProject ap = new AntProject();

        copyProject( p, ap );

        ap.setTargets( p.getConfiguration().getProperty( AntBuildExecutor.CONFIGURATION_TARGETS ) );

        ap.setExecutable( p.getConfiguration().getProperty( AntBuildExecutor.CONFIGURATION_EXECUTABLE ) );

        return ap;
    }

    public void updateAntProject( AntProject project )
        throws ContinuumException
    {
        updateProject( project );

        // ----------------------------------------------------------------------
        // The configuration will be null here because the "executable" and
        // "targets" fields in the AntProject are used to create the
        // configuration. We probably don't even need the configuration.
        // ----------------------------------------------------------------------

        Properties configuration = new Properties();

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_TARGETS, project.getTargets() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        addProjectFromScm( project.getScmUrl(),
                           MavenOneBuildExecutor.ID,
                           project.getName(),
                           project.getNagEmailAddress(),
                           project.getVersion(),
                           configuration );
    }

    public MavenOneProject getMavenOneProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        MavenOneProject mp = new MavenOneProject();

        copyProject( p, mp );

        mp.setGoals( p.getConfiguration().getProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS ) );

        return mp;
    }

    public void updateMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        addProjectFromScm( project.getScmUrl(),
                           MavenTwoBuildExecutor.ID,
                           project.getName(),
                           project.getNagEmailAddress(),
                           project.getVersion(),
                           configuration );
    }

    public MavenTwoProject getMavenTwoProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        MavenTwoProject mp = new MavenTwoProject();

        copyProject( p, mp );

        mp.setGoals( p.getConfiguration().getProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS ) );

        return mp;
    }

    public void updateMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addShellProject( ShellProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( ShellBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        if ( project.getArguments() != null )
        {
            configuration.setProperty( ShellBuildExecutor.CONFIGURATION_ARGUMENTS, project.getArguments() );
        }

        addProjectFromScm( project.getScmUrl(),
                           ShellBuildExecutor.ID,
                           project.getName(),
                           project.getNagEmailAddress(),
                           project.getVersion(),
                           configuration );
    }

    public ShellProject getShellProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        ShellProject sp = new ShellProject();

        copyProject( p, sp );

        sp.setExecutable( p.getConfiguration().getProperty( ShellBuildExecutor.CONFIGURATION_EXECUTABLE ) );

        sp.setArguments( p.getConfiguration().getProperty( ShellBuildExecutor.CONFIGURATION_ARGUMENTS ) );

        return sp;
    }

    public void updateShellProject( ShellProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( ShellBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        if ( project.getArguments() != null )
        {
            configuration.setProperty( ShellBuildExecutor.CONFIGURATION_ARGUMENTS, project.getArguments() );
        }

        updateProjectConfiguration( project.getId(), configuration );
    }

    private void updateProject( ContinuumProject project )
        throws ContinuumException
    {
        try
        {
            store.updateProject( project.getId(),
                                 project.getName(),
                                 project.getScmUrl(),
                                 project.getNagEmailAddress(),
                                 project.getVersion() );
        }
        catch ( ContinuumStoreException e )
        {
            throw logAndCreateException( "Error while updating the project.", e );
        }
    }

    private void copyProject( ContinuumProject p1, ContinuumProject p2 )
    {
        p2.setId( p1.getId() );

        p2.setName( p1.getName() );

        p2.setScmUrl( p1.getScmUrl() );

        p2.setNagEmailAddress( p1.getNagEmailAddress() );

        p2.setVersion( p1.getVersion() );

        p2.setExecutorId( p1.getExecutorId() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ContinuumProjectBuilder getProjectBuilder( String projectBuilderId )
        throws ContinuumException
    {
        try
        {
            return projectBuilderManager.getProjectCreator( projectBuilderId );
        }
        catch ( ContinuumProjectBuilderManagerException e )
        {
            throw logAndCreateException( "Error while getting project builder '" + projectBuilderId + "'.", e );
        }
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
                                          executorId,
                                          null,
                                          project.getConfiguration() );

            // ----------------------------------------------------------------------
            // Set the working directory
            // ----------------------------------------------------------------------

            projectWorkingDirectory = new File( workingDirectory, projectId );

            if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
            {
                logAndCreateException( "Could not make the working directory for the project " +
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
        getLogger().info( "Updating project '" + project.getName() + "'.");

        // ----------------------------------------------------------------------
        // Make a new descriptor
        // ----------------------------------------------------------------------

        ContinuumBuildExecutor builder = buildExecutorManager.getBuilder( project.getExecutorId() );

        try
        {
            builder.updateProjectFromCheckOut( new File( project.getWorkingDirectory() ), project );
        }
        catch ( ContinuumBuildExecutorException e )
        {
            logAndCreateException( "Error while updating project from check out.", e);
        }

        // ----------------------------------------------------------------------
        // Store the new descriptor
        // ----------------------------------------------------------------------

        try
        {
            String id = project.getId();

            store.updateProject( id,
                                 project.getName(),
                                 project.getScmUrl(),
                                 project.getNagEmailAddress(),
                                 project.getVersion() );

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

    public void initialize() throws InitializationException
    {
        getLogger().info( "Initializing Continuum." );

        File wdFile = new File( workingDirectory );

        if ( wdFile.exists() )
        {
            if ( !wdFile.isDirectory() )
            {
                String msg = "The specified working directory isn't a directory: " + "'" + 
                    wdFile.getAbsolutePath() + "'.";
                
                getLogger().error( msg );
                throw new InitializationException( msg );
            }
        }
        else
        {
            if ( !wdFile.mkdirs() )
            {
                String msg = "Could not making the working directory: " + "'" + 
                    wdFile.getAbsolutePath() + "'." ;

                getLogger().error( msg );
                throw new InitializationException( msg );
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
        catch (ContinuumStoreException e)
        {
           throw new InitializationException( "Couldn't load projects.", e );
        }
    }

    public void start() throws StartingException
    {
        getLogger().info( "Starting Continuum." );

        // check to see if the tables exists or not.
        File file = new File( appHome, "continuum.properties" );

        Properties properties = new Properties();

        try
        {
            if ( !file.exists() )
            {
                
                    initializeStore( file );                
            }
            else
            {
                properties.load( new FileInputStream( file ) );
    
                String state = properties.getProperty( DATABASE_INITIALIZED );
    
                if ( !state.equals( "true" ) )
                {
                    initializeStore( file );
                }
            }
        }
        catch( Exception e )
        {
            throw new StartingException( "Couldn't initialize store,", e );
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

    private void initializeStore( File file )
        throws Exception
    {
        Properties properties = new Properties();

        getLogger().warn( "This system isn't configured. Configuring." );

        store.createDatabase();

        properties.setProperty( DATABASE_INITIALIZED, "true" );

        properties.store( new FileOutputStream( file ), null );
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
}
