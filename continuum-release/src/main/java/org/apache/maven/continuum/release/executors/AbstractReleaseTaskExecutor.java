package org.apache.maven.continuum.release.executors;

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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.plugins.release.ReleaseManager;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectSorter;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Edwin Punzalan
 */
public abstract class AbstractReleaseTaskExecutor
    implements ReleaseTaskExecutor, Contextualizable
{
    /**
     * @plexus.requirement
     */
    protected ContinuumReleaseManager continuumReleaseManager;

    /**
     * @plexus.requirement
     */
    protected ReleaseManager releasePluginManager;

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder settingsBuilder;

    /**
     * @plexus.configuration
     */
    private String localRepository;

    private ProfileManager profileManager;

    private PlexusContainer container;

    private Settings settings;

    protected List getReactorProjects( ReleaseDescriptor descriptor )
        throws ContinuumReleaseException
    {
        Settings settings = getSettings();

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
                MavenProject reactorProject = projectBuilder.build( pomFile, getLocalRepository(),
                                                                    getProfileManager( settings ) );

                reactorProjects.add( reactorProject );
            }
            catch ( ProjectBuildingException e )
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

    protected Settings getSettings()
        throws ContinuumReleaseException
    {
        if ( settings == null )
        {
            try
            {
                settings = settingsBuilder.buildSettings();
            }
            catch ( IOException e )
            {
                throw new ContinuumReleaseException( "Failed to get Maven Settings.", e );
            }
            catch ( XmlPullParserException e )
            {
                throw new ContinuumReleaseException( "Failed to get Maven Settings.", e );
            }
        }

        return settings;
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
