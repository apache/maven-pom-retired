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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.continuum.buildcontroller.BuildController;
import org.apache.maven.continuum.builder.ContinuumBuilder;
import org.apache.maven.continuum.builder.maven.m1.Maven1Builder;
import org.apache.maven.continuum.builder.maven.m2.MavenShellBuilder;
import org.apache.maven.continuum.builder.shell.ShellBuilder;
import org.apache.maven.continuum.builder.ant.AntBuilder;
import org.apache.maven.continuum.builder.manager.BuilderManager;
import org.apache.maven.continuum.buildqueue.BuildQueue;
import org.apache.maven.continuum.buildqueue.BuildQueueException;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id: DefaultContinuum.java,v 1.7 2005/04/08 12:47:56 trygvis Exp $
 */
public class DefaultContinuum
    extends AbstractLogEnabled
    implements Continuum, Initializable, Startable
{
    private final static String DATABASE_INITIALIZED = "database.initialized";

    private final static String CONTINUUM_VERSION = "1.0-alpha-1-SNAPSHOT";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // TODO: look up these requiremetns in start() to have better control of the
    //       application initialization sequence. The application should make sure
    //       that the database is properly initialized before starting the store.

    /** @requirement */
    private BuilderManager builderManager;

    /** @requirement */
    private BuildController buildController;

    /** @requirement */
    private BuildQueue buildQueue;

    /** @requirement */
    private ContinuumStore store;

    /** @requirement */
    private ContinuumScm scm;

    /** @configuration */
    private String appHome;

    /** @configuration */
    private String workingDirectory;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private BuilderThread builderThread;

    private Thread builderThreadThread;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Iterator getProjects()
        throws ContinuumStoreException
    {
        return store.getAllProjects();
    }

    public ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumStoreException
    {
        return store.getLatestBuildForProject( id );
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

    public String addProjectFromUrl( String url, String builderType )
        throws ContinuumException
    {
        URL u;

        try
        {
            u = new URL( url );
        }
        catch ( MalformedURLException e )
        {
            throw new ContinuumException( "Invalid URL", e );
        }

        return addProjectFromUrl( u, builderType );
    }

    public String addProjectFromUrl( URL url, String builderType )
        throws ContinuumException
    {
        File pomFile;

        try
        {
            String pom = IOUtil.toString( url.openStream() );

            pomFile = File.createTempFile( "continuum-", "-pom-download" );

            FileUtils.fileWrite( pomFile.getAbsolutePath(), pom );

            getLogger().info( "wrote pom to " + pomFile );
        }
        catch ( IOException ex )
        {
            throw new ContinuumException( "Error while downloading the pom.", ex );
        }

        // ----------------------------------------------------------------------
        // Really what we want to do is encapsulate the all handling to a builderType
        // or project type handler to deal with everything. Take the initial
        // URL which points to the metadata for the project and let the
        // handler deal with everything else.
        // ----------------------------------------------------------------------

        ContinuumBuilder builder = builderManager.getBuilder( builderType );

        getLogger().info( "We have the builder: " + builder );

        ContinuumProject project = builder.createProjectFromMetadata( url );

        getLogger().info( "done creating continuum project" );

        // TODO: Update from metadata in the initial checkout?

        project = addProjectAndCheckOutSources( project, builderType );

        return project.getId();
    }

    public String addProjectFromScm( String scmUrl, String builderType, String projectName, String nagEmailAddress,
                                     String version, Properties configuration )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Create the stub project
        // ----------------------------------------------------------------------

        ContinuumProject project = new ContinuumProject();

        project.setScmUrl( scmUrl );

        project.setBuilderId( builderType );

        project.setName( projectName );

        project.setNagEmailAddress( nagEmailAddress );

        project.setVersion( version );

        project.setConfiguration( configuration );

        // ----------------------------------------------------------------------
        // Make sure that the builder id is correct before starting to check
        // stuff out
        // ----------------------------------------------------------------------

        if( !builderManager.hasBuilder( builderType ) )
        {
            throw new ContinuumException( "No such builder '" + builderType + "'." );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        doTempCheckOut( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        project = addProjectAndCheckOutSources( project, builderType );

        updateProjectFromCheckOut( project );

        return project.getId();
    }

    public void updateProjectFromScm( String projectId )
        throws ContinuumException
    {
        try
        {
            ContinuumProject project = store.getProject( projectId );

            File workingDirectory = new File( project.getWorkingDirectory() );

            if ( !workingDirectory.exists() )
            {
                getLogger().warn( "Creating missing working directory for project '" + project.getName() + "'." );

                if ( !workingDirectory.exists() )
                {
                    throw new ContinuumException( "Could not make missing working directory for project '" + project.getName() + "'." );
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
                throw new ContinuumException( "Error while updating project.", e );
            }

            updateProjectFromCheckOut( project );
        }
        catch ( ContinuumStoreException ex )
        {
            getLogger().error( "Error while updating project.", ex );

            throw new ContinuumException( "Error while updating project from SCM.", ex );
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
            getLogger().error( "Error while updating project configuration.", ex );

            throw new ContinuumException( "Error while updating project configuration.", ex );
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
            getLogger().error( "Error while updating project.", ex );

            throw new ContinuumException( "Error while removing project.", ex );
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
            getLogger().error( "Error while finding all projects.", ex );

            throw new ContinuumException( "Exception while getting all projects.", ex );
        }
    }

    public Iterator getAllProjects( int start, int end )
        throws ContinuumException
    {
        try
        {
            Iterator it = store.getAllProjects();

            return it;
        }
        catch ( ContinuumStoreException ex )
        {
            getLogger().error( "Error while finding all projects.", ex );

            throw new ContinuumException( "Exception while getting all projects.", ex );
        }
    }

    public String buildProject( String projectId )
        throws ContinuumException
    {
        try
        {
            ContinuumProject project = store.getProject( projectId );

            getLogger().info( "Enqueuing '" + project.getName() + "'." );

            String buildId = store.createBuild( project.getId() );

            getLogger().info( "Build id: '" + buildId + "'." );

            buildQueue.enqueue( projectId, buildId );

            return buildId;
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while building project.", e );

            throw new ContinuumException( "Error while creating build object.", e );
        }
        catch ( BuildQueueException e )
        {
            getLogger().error( "Error while enqueuing project.", e );

            throw new ContinuumException( "Error while creating enqueuing object.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    public void addAntProject( AntProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( AntBuilder.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        configuration.setProperty( AntBuilder.CONFIGURATION_TARGETS, project.getTargets() );

        addProjectFromScm( project.getScmUrl(),
                           "ant",
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

        ap.setTargets( p.getConfiguration().getProperty( AntBuilder.CONFIGURATION_TARGETS ) );

        ap.setExecutable( p.getConfiguration().getProperty( AntBuilder.CONFIGURATION_EXECUTABLE ) );

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

        configuration.setProperty( AntBuilder.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        configuration.setProperty( AntBuilder.CONFIGURATION_TARGETS, project.getTargets() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( Maven1Builder.CONFIGURATION_GOALS, project.getGoals() );

        addProjectFromScm( project.getScmUrl(),
                           "maven-1",
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

        mp.setGoals( p.getConfiguration().getProperty( Maven1Builder.CONFIGURATION_GOALS ) );

        return mp;
    }

    public void updateMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( Maven1Builder.CONFIGURATION_GOALS, project.getGoals() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( MavenShellBuilder.CONFIGURATION_GOALS, project.getGoals() );

        addProjectFromScm( project.getScmUrl(),
                           "maven2",
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

        mp.setGoals( p.getConfiguration().getProperty( MavenShellBuilder.CONFIGURATION_GOALS ) );

        return mp;
    }

    public void updateMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( MavenShellBuilder.CONFIGURATION_GOALS, project.getGoals() );
        
        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addShellProject( ShellProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( ShellBuilder.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        if ( project.getArguments() != null )
        {
            configuration.setProperty( ShellBuilder.CONFIGURATION_ARGUMENTS, project.getArguments() );
        }

        addProjectFromScm( project.getScmUrl(),
                           "shell",
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

        sp.setExecutable( p.getConfiguration().getProperty( ShellBuilder.CONFIGURATION_EXECUTABLE ) );

        sp.setArguments( p.getConfiguration().getProperty( ShellBuilder.CONFIGURATION_ARGUMENTS ) );

        return sp;
    }

    public void updateShellProject( ShellProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( ShellBuilder.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        if ( project.getArguments() != null )
        {
            configuration.setProperty( ShellBuilder.CONFIGURATION_ARGUMENTS, project.getArguments() );
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
            throw new ContinuumException( "Error while updating the project.", e );
        }
    }

    private void copyProject( ContinuumProject p1, ContinuumProject p2 )
    {
        p2.setId( p1.getId() );

        p2.setName( p1.getName() );

        p2.setScmUrl( p1.getScmUrl() );

        p2.setNagEmailAddress( p1.getNagEmailAddress() );

        p2.setVersion( p1.getVersion() );

        p2.setBuilderId( p1.getBuilderId() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ContinuumProject addProjectAndCheckOutSources( ContinuumProject project, String builderType )
        throws ContinuumException
    {
        try
        {
            // ----------------------------------------------------------------------
            // Store the project
            // ----------------------------------------------------------------------

            String projectId = store.addProject( project.getName(),
                                                 project.getScmUrl(),
                                                 project.getNagEmailAddress(),
                                                 project.getVersion(),
                                                 builderType,
                                                 null,
                                                 project.getConfiguration() );

            // ----------------------------------------------------------------------
            // Set the working directory
            // ----------------------------------------------------------------------


            File projectWorkingDirectory = new File( workingDirectory, projectId );

            if ( !projectWorkingDirectory.exists() && !projectWorkingDirectory.mkdirs() )
            {
                throw new ContinuumException( "Could not make the working directory for the project (" + projectWorkingDirectory.getAbsolutePath() + ")." );
            }

            project.setWorkingDirectory( projectWorkingDirectory.getAbsolutePath() );

            // ----------------------------------------------------------------------
            // Check out the project
            // ----------------------------------------------------------------------

            store.setWorkingDirectory( projectId, projectWorkingDirectory.getAbsolutePath() );

            scm.checkOutProject( project );

            project = store.getProject( projectId );

            return project;
        }
        catch ( ContinuumScmException ex )
        {
            getLogger().error( "Exception while checking out the project.", ex );

            throw new ContinuumException( "Exception while checking out the project.", ex );
        }
        catch ( ContinuumStoreException ex )
        {
            getLogger().error( "Exception while adding project.", ex );

            throw new ContinuumException( "Exception while adding project.", ex );
        }
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
                throw new ContinuumException( "Error while cleaning out " + checkoutDirectory.getAbsolutePath() );
            }
        }
        else
        {
            if ( !checkoutDirectory.mkdirs() )
            {
                throw new ContinuumException( "Could not make the check out directory (" + checkoutDirectory.getAbsolutePath() + ")." );
            }
        }

        // TODO: Get the list of files to check out from the builder.
        // Maven 2: pom.xml, Maven 1: project.xml, Ant: all? build.xml?

        try
        {
            scm.checkOut( project, checkoutDirectory);
        }
        catch ( ContinuumScmException ex )
        {
            throw new ContinuumException( "Error while checking out the project.", ex );
        }
    }

    private void updateProjectFromCheckOut( ContinuumProject project )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Make a new descriptor
        // ----------------------------------------------------------------------

        ContinuumBuilder builder = builderManager.getBuilder( project.getBuilderId() );

        String id = project.getId();

        builder.updateProjectFromCheckOut( new File( project.getWorkingDirectory() ), project );

        // ----------------------------------------------------------------------
        // Store the new descriptor
        // ----------------------------------------------------------------------

        try
        {
            store.updateProject( id,
                                 project.getName(),
                                 project.getScmUrl(),
                                 project.getNagEmailAddress(),
                                 project.getVersion() );

            store.updateProjectConfiguration( id, project.getConfiguration() );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumException( "Error while storing the updated project.", e );
        }

        getLogger().info( "Updated project: " + project.getName() );
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    public void initialize()
        throws Exception
    {
        getLogger().info( "Initializing Continuum." );

        File wdFile = new File( workingDirectory );

        if ( wdFile.exists() )
        {
            if ( !wdFile.isDirectory() )
            {
                throw new ContinuumException( "The specified working directory isn't a directory: " + wdFile.getAbsolutePath() );
            }
        }
        else
        {
            if ( !wdFile.mkdirs() )
            {
                throw new ContinuumException( "Could not making the working directory: " + wdFile.getAbsolutePath() );
            }
        }

        getLogger().info( "Showing all projects: " );

        for ( Iterator it = store.getAllProjects(); it.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            getLogger().info( " " + project.getId() + ":" + project.getName() + ":" + project.getBuilderId() );
        }
    }

    public void start()
        throws Exception
    {
        getLogger().info( "Starting Continuum." );

        // start the builder thread
        builderThread = new BuilderThread( buildController, buildQueue, getLogger() );

        builderThreadThread = new Thread( builderThread );

        builderThreadThread.setDaemon( true );

        builderThreadThread.start();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        // check to see if the tables exists or not.
        File file = new File( appHome, "continuum.properties" );

        Properties properties = new Properties();

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
        throws Exception
    {
        int maxSleep = 10 * 1000; // 10 seconds
        int interval = 1000;
        int slept = 0;

        getLogger().info( "Stopping Continuum." );

        // signal the thread to stop
        builderThread.shutdown();

        builderThreadThread.interrupt();

        while ( !builderThread.isDone() )
        {
            if ( slept > maxSleep )
            {
                getLogger().warn( "Timeout, stopping Continuum." );

                break;
            }

            getLogger().info( "Waiting until Continuum is idling..." );

            try
            {
                synchronized ( builderThread )
                {
                    builderThread.wait( interval );
                }
            }
            catch ( InterruptedException ex )
            {
                // ignore
            }

            // TODO: should use System.currentTimeMillis()
            slept += interval;
        }

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
}
