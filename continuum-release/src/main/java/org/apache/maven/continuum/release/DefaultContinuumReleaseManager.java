package org.apache.maven.continuum.release;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugins.release.ReleaseManager;
import org.apache.maven.plugins.release.ReleaseExecutionException;
import org.apache.maven.plugins.release.ReleaseFailureException;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.taskqueue.TaskQueue;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Jason van Zyl
 */
public class DefaultContinuumReleaseManager
    implements ContinuumReleaseManager, Contextualizable
{
    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private ReleaseManager releaseManager;

    /**
     * @plexus.requirement
     */
    private TaskQueue prepareReleaseQueue;

    /**
     * @plexus.requirement
     */
    private TaskQueue performReleaseQueue;

    /**
     * @plexus.configuration
     */
    private String localRepository;

    private PlexusContainer container;

    /**
     * contains previous release:prepare descriptors; one per project
     */
    private Map preparedReleases;

    public void prepare( ReleaseDescriptor descriptor, Settings settings )
        throws ContinuumReleaseException
    {
        try
        {
            releaseManager.prepare( descriptor, settings, getReactorProjects( descriptor, settings ) );
        }
        catch ( ReleaseExecutionException e )
        {
            throw new ContinuumReleaseException( "Release Manager Execution error occurred.", e );
        }
        catch ( ReleaseFailureException e )
        {
            throw new ContinuumReleaseException( "Release Manager failure occurred.", e );
        }
    }

    public void perform( ReleaseDescriptor descriptor, Settings settings, File buildDirectory,
                         String goals, boolean useReleaseProfile )
        throws ContinuumReleaseException
    {
        try
        {
            releaseManager.perform( descriptor, settings, getReactorProjects( descriptor, settings ),
                                    new File( buildDirectory, "checkout" ), goals, useReleaseProfile );
        }
        catch ( ReleaseExecutionException e )
        {
            throw new ContinuumReleaseException( "Release Manager Execution error occurred.", e );
        }
        catch ( ReleaseFailureException e )
        {
            throw new ContinuumReleaseException( "Release Manager failure occurred.", e );
        }
    }

    private List getReactorProjects( ReleaseDescriptor descriptor, Settings settings )
        throws ContinuumReleaseException
    {
        List reactorProjects = new ArrayList();

        MavenProject project;
        try
        {
            project = projectBuilder.build( getProjectDescriptorFile( descriptor ),
                                            getLocalRepository(), getProfileManager( settings ) );

            reactorProjects.add( project );
        }
        catch ( ProjectBuildingException e )
        {
            throw new ContinuumReleaseException( "Failed to build project.", e );
        }

        for( Iterator modules = project.getModules().iterator(); modules.hasNext(); )
        {
            String moduleDir = modules.next().toString();

            File pomFile = new File( project.getBasedir(), moduleDir );

            try
            {
                projectBuilder.build( pomFile, getLocalRepository(), getProfileManager( settings ) );

                reactorProjects.add( projectBuilder );
            }
            catch ( ProjectBuildingException e )
            {
                throw new ContinuumReleaseException( "Failed to build project.", e );
            }
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
        return new DefaultArtifactRepository( "local-repository", "file://" + localRepository,
                                              new DefaultRepositoryLayout() );
    }

    private ProfileManager getProfileManager( Settings settings )
    {
        return new DefaultProfileManager( container, settings );
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
