package org.apache.maven.continuum.release;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.RollbackReleaseProjectTask;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.config.ReleaseDescriptorStore;
import org.apache.maven.shared.release.config.ReleaseDescriptorStoreException;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @author Edwin Punzalan
 */
public class DefaultContinuumReleaseManager
    implements ContinuumReleaseManager
{
    /**
     * @plexus.requirement
     */
    private ReleaseDescriptorStore releaseStore;

    /**
     * @plexus.requirement
     */
    private TaskQueue prepareReleaseQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue performReleaseQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue rollbackReleaseQueue;

    private Map listeners;

    /**
     * contains previous release:prepare descriptors; one per project
     * @todo remove static when singleton strategy is working
     */
    private static Map preparedReleases;

    /**
     * contains results
     * @todo remove static when singleton strategy is working
     */
    private static Map releaseResults;

    public String prepare( Project project, Properties releaseProperties, Map relVersions,
                           Map devVersions, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        String releaseId = project.getGroupId() + ":" + project.getArtifactId();

        ReleaseDescriptor descriptor = getReleaseDescriptor( project, releaseProperties, relVersions, devVersions );

        getListeners().put( releaseId, listener );

        try
        {
            prepareReleaseQueue.put( new PrepareReleaseProjectTask( releaseId, descriptor,
                                                                    (ReleaseManagerListener) listener ) );

        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add prepare release task in queue.", e );
        }

        return releaseId;
    }

    public void perform( String releaseId, File buildDirectory, String goals, boolean useReleaseProfile,
                         ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = (ReleaseDescriptor) getPreparedReleases().get( releaseId );
        if ( descriptor != null )
        {
            perform( releaseId, descriptor, buildDirectory, goals, useReleaseProfile, listener );
        }
    }

    public void perform( String releaseId, String workingDirectory, File buildDirectory,
                         String goals, boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = readReleaseDescriptor( workingDirectory );

        perform( releaseId, descriptor, buildDirectory, goals, useReleaseProfile, listener );
    }

    private void perform( String releaseId, ReleaseDescriptor descriptor, File buildDirectory,
                          String goals, boolean useReleaseProfile, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        try
        {
            getListeners().put( releaseId, listener );

            performReleaseQueue.put( new PerformReleaseProjectTask( releaseId, descriptor, buildDirectory,
                                                                    goals, useReleaseProfile,
                                                                    (ReleaseManagerListener) listener ) );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to add perform release task in queue.", e );
        }
    }

    public void rollback( String releaseId, String workingDirectory, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = readReleaseDescriptor( workingDirectory );

        rollback( releaseId, descriptor, listener );
    }

    private void rollback( String releaseId, ReleaseDescriptor descriptor, ContinuumReleaseManagerListener listener )
        throws ContinuumReleaseException
    {
        Task releaseTask =
            new RollbackReleaseProjectTask( releaseId, descriptor, (ReleaseManagerListener) listener );

        try
        {
            rollbackReleaseQueue.put( releaseTask );
        }
        catch ( TaskQueueException e )
        {
            throw new ContinuumReleaseException( "Failed to rollback release.", e );
        }
    }

    public Map getPreparedReleases()
    {
        if ( preparedReleases == null )
        {
            preparedReleases = new Hashtable();
        }

        return preparedReleases;
    }

    public Map getReleaseResults()
    {
        if ( releaseResults == null )
        {
            releaseResults = new Hashtable();
        }

        return releaseResults;
    }

    private ReleaseDescriptor getReleaseDescriptor( Project project, Properties releaseProperties,
                                                    Map relVersions, Map devVersions )
    {
        ReleaseDescriptor descriptor = new ReleaseDescriptor();

        //release properties from the project
        descriptor.setWorkingDirectory( project.getWorkingDirectory() );
        descriptor.setScmSourceUrl( project.getScmUrl() );

        //required properties
        descriptor.setScmReleaseLabel( releaseProperties.getProperty( "tag" ) );
        descriptor.setScmTagBase( releaseProperties.getProperty( "tagBase" ) );
        descriptor.setReleaseVersions( relVersions );
        descriptor.setDevelopmentVersions( devVersions );
        descriptor.setPreparationGoals( releaseProperties.getProperty( "prepareGoals" ) );

        //other properties
        if ( releaseProperties.containsKey( "username" ) )
        {
            descriptor.setScmUsername( releaseProperties.getProperty( "username" ) );
        }
        if ( releaseProperties.containsKey( "password" ) )
        {
            descriptor.setScmPassword( releaseProperties.getProperty( "password" ) );
        }

        //forced properties
        descriptor.setInteractive( false );

        return descriptor;
    }

    private ReleaseDescriptor readReleaseDescriptor( String workingDirectory )
        throws ContinuumReleaseException
    {
        ReleaseDescriptor descriptor = new ReleaseDescriptor();
        descriptor.setWorkingDirectory( workingDirectory );

        try
        {
            descriptor = releaseStore.read( descriptor );
        }
        catch ( ReleaseDescriptorStoreException e )
        {
            throw new ContinuumReleaseException( "Failed to parse descriptor file.", e );
        }

        return descriptor;
    }

    public Map getListeners()
    {
        if ( listeners == null )
        {
            listeners = new Hashtable();
        }

        return listeners;
    }
}
