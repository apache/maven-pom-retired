package org.apache.maven.continuum.release.executors;

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

import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.ReleaseProjectTask;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectSorter;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.settings.Settings;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.File;

/**
 *
 *
 * @author Edwin Punzalan
 */
public class PerformReleaseTaskExecutor
    extends AbstractReleaseTaskExecutor
    implements Contextualizable
{
    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    private ProfileManager profileManager;

    private PlexusContainer container;

    public void execute( ReleaseProjectTask task )
        throws TaskExecutionException
    {
        PerformReleaseProjectTask performTask = (PerformReleaseProjectTask) task;

        ReleaseManagerListener listener = performTask.getListener();

        ReleaseDescriptor descriptor = performTask.getDescriptor();

        List reactorProjects = getReactorProjects( performTask );

        ReleaseResult result =
            releaseManager.performWithResult( descriptor, settings, reactorProjects,
                                                    performTask.getBuildDirectory(), performTask.getGoals(),
                                                    performTask.isUseReleaseProfile(), listener );

        //override to show the actual start time
        result.setStartTime( getStartTime() );

        if ( result.getResultCode() == ReleaseResult.SUCCESS )
        {
            continuumReleaseManager.getPreparedReleases().remove( performTask.getReleaseId() );
        }

        continuumReleaseManager.getReleaseResults().put( performTask.getReleaseId(), result );
    }

    protected List getReactorProjects( ReleaseProjectTask releaseTask )
        throws TaskExecutionException
    {
        List reactorProjects;
        try
        {
            reactorProjects = getReactorProjects( releaseTask.getDescriptor() );
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = createReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( releaseTask.getReleaseId(), result );

            releaseTask.getListener().error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }

        return reactorProjects;
    }

    /**
     * @todo remove and use generate-reactor-projects phase
     */
    protected List getReactorProjects( ReleaseDescriptor descriptor )
        throws ContinuumReleaseException
    {
        List reactorProjects = new ArrayList();

        MavenProject project;
        try
        {
            project = projectBuilder.buildWithDependencies( getProjectDescriptorFile( descriptor ),
                                            getLocalRepository(), getProfileManager( settings ) );

            reactorProjects.add( project );
        }
        catch ( ProjectBuildingException e )
        {
            throw new ContinuumReleaseException( "Failed to build project.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new ContinuumReleaseException( "Failed to build project.", e );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ContinuumReleaseException( "Failed to build project.", e );
        }

        for( Iterator modules = project.getModules().iterator(); modules.hasNext(); )
        {
            String moduleDir = modules.next().toString();

            File pomFile = new File( project.getBasedir(), moduleDir + "/pom.xml" );

            try
            {
                MavenProject reactorProject = projectBuilder.buildWithDependencies( pomFile, getLocalRepository(),
                                                                    getProfileManager( settings ) );

                reactorProjects.add( reactorProject );
            }
            catch ( ProjectBuildingException e )
            {
                throw new ContinuumReleaseException( "Failed to build project.", e );
            }
            catch ( ArtifactNotFoundException e )
            {
                throw new ContinuumReleaseException( "Failed to build project.", e );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new ContinuumReleaseException( "Failed to build project.", e );
            }
        }

        try
        {
            reactorProjects = new ProjectSorter( reactorProjects ).getSortedProjects();
        }
        catch ( CycleDetectedException e )
        {
            throw new ContinuumReleaseException( "Failed to sort projects.", e );
        }
        catch ( DuplicateProjectException e )
        {
            throw new ContinuumReleaseException( "Failed to sort projects.", e );
        }

        return reactorProjects;
    }


    private File getProjectDescriptorFile( ReleaseDescriptor descriptor )
    {
        String parentPath = descriptor.getWorkingDirectory();

        String pomFilename = descriptor.getPomFileName();
        if ( pomFilename == null )
        {
            pomFilename = "pom.xml";
        }

        return new File( parentPath, pomFilename );
    }

    private ArtifactRepository getLocalRepository()
    {
        return new DefaultArtifactRepository( "local-repository", "file://" + settings.getLocalRepository(),
                                                                                   new DefaultRepositoryLayout() );
    }

    private ProfileManager getProfileManager( Settings settings )
    {
        if ( profileManager == null )
        {
            profileManager = new DefaultProfileManager( container, settings );
        }

        return profileManager;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
