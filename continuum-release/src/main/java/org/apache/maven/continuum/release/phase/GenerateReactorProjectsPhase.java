package org.apache.maven.continuum.release.phase;

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

import org.apache.maven.shared.release.phase.AbstractReleasePhase;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectSorter;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.profiles.DefaultProfileManager;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

/**
 * Generate the reactor projects
 *
 * @author Edwin Punzalan
 */
public class GenerateReactorProjectsPhase
    extends AbstractReleasePhase
    implements Contextualizable
{
    private MavenProjectBuilder projectBuilder;

    private MavenSettingsBuilder settingsBuilder;

    private PlexusContainer container;

    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        ReleaseResult result = new ReleaseResult();

        try
        {
            reactorProjects.addAll( getReactorProjects( releaseDescriptor ) );
        }
        catch ( ContinuumReleaseException e )
        {
            throw new ReleaseExecutionException( "Unable to get reactor projects: " + e.getMessage(), e );
        }

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        return execute( releaseDescriptor, settings, reactorProjects );
    }

    private List getReactorProjects( ReleaseDescriptor descriptor )
        throws ContinuumReleaseException
    {
        List reactorProjects = new ArrayList();

        MavenProject project;
        try
        {
            project = projectBuilder.buildWithDependencies( getProjectDescriptorFile( descriptor ),
                                            getLocalRepository(), getProfileManager( getSettings() ) );

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
                                                                    getProfileManager( getSettings() ) );

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
        throws ContinuumReleaseException
    {
        return new DefaultArtifactRepository( "local-repository", "file://" + getSettings().getLocalRepository(),
                                                                                   new DefaultRepositoryLayout() );
    }

    private ProfileManager getProfileManager( Settings settings )
    {
        return new DefaultProfileManager( container, settings );
    }

    private Settings getSettings()
        throws ContinuumReleaseException
    {
        try
        {
            return settingsBuilder.buildSettings();
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

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
