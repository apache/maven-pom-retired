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
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;
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
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    // TODO: look up these requiremetns in start() to have better control of the
    //       application initialization sequence. The application should make sure
    //       that the database is properly initialized before starting the store.

    /**
     * @plexus.requirement
     */
    private BuildExecutorManager buildExecutorManager;

    /**
     * @plexus.requirement
     */
    private ContinuumProjectBuilderManager projectBuilderManager;

    /**
     * @plexus.requirement
     */
    private TaskQueue buildQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue checkOutQueue;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @plexus.configuration
     */
    private String workingDirectory;

    /**
     * @plexus.configuration
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

    public boolean isBuilding( String id )
        throws ContinuumException
    {
        List queue;

        try
        {
            queue = buildQueue.getQueueSnapshot();
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumException( "Error while getting the queue snapshot." );
        }

        for ( Iterator it = queue.iterator(); it.hasNext(); )
        {
            BuildProjectTask task = (BuildProjectTask) it.next();

            if ( task.getProjectId().equals( id ) )
            {
                return true;
            }
        }

        return false;
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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void removeNotifier( Object oid )
        throws ContinuumException
    {
        try
        {
            store.removeNotifier( oid );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing notifier.", ex );
        }
    }

    public void storeNotifier( Object oid )
        throws ContinuumException
    {
        try
        {
            store.storeNotifier( oid );
        }
        catch ( ContinuumStoreException ex )
        {
            throw logAndCreateException( "Error while removing notifier.", ex );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void updateProject( ContinuumProject project )
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
            throw logAndCreateException( "Exception while getting project '" + projectId + "'.", ex );
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
            throw logAndCreateException( "Exception while getting projects.", ex );
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
            ContinuumBuild b = store.getBuild( buildId );

            return b;
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

    public void stop()
    {
        getLogger().info( "Stopping Continuum." );

        getLogger().info( "Continuum stopped." );
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

    private String getVersion()
    {
        InputStream resourceAsStream;
        try
        {
            Properties properties = new Properties();
            resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(
                "META-INF/maven/org.apache.maven.continuum/continuum-core/pom.properties" );
            properties.load( resourceAsStream );

            return properties.getProperty( "version", "unknown" );
        }
        catch ( Exception e )
        {
            return "unknown";
        }
    }
}

