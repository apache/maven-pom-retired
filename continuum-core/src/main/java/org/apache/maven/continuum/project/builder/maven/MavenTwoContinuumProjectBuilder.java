package org.apache.maven.continuum.project.builder.maven;

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

import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelperException;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoContinuumProjectBuilder
    extends AbstractContinuumProjectBuilder
    implements ContinuumProjectBuilder
{
    public static final String ID = "maven-two-builder";

    private static final String POM_PART = "/pom.xml";

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper builderHelper;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.configuration
     */
    private List excludedPackagingTypes;

    // ----------------------------------------------------------------------
    // AbstractContinuumProjectBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
        throws ContinuumProjectBuilderException
    {
        // ----------------------------------------------------------------------
        // We need to roll the project data into a file so that we can use it
        // ----------------------------------------------------------------------

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        readModules( url, result, true, username, password );

        return result;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void readModules( URL url, ContinuumProjectBuildingResult result, boolean groupPom, String username, String password )
    {
        MavenProject mavenProject;

        try
        {
            // TODO: this isn't finding parents due to relocating to the URL
            // TODO: the whole modules resolution is funky
            mavenProject = builderHelper.getMavenProject( createMetadataFile( url, username, password ) );
        }
        catch ( MavenBuilderHelperException e )
        {
            result.addWarning( e.getMessage() );

            return;
        }
        catch ( IOException e )
        {
            result.addWarning( "Could not download " + url + ": " + e.getMessage() );

            return;
        }

        if ( groupPom )
        {
            ProjectGroup projectGroup = buildProjectGroup( mavenProject );

            if ( projectGroup != null )
            {
                result.addProjectGroup( projectGroup );
            }
        }

        if ( !excludedPackagingTypes.contains( mavenProject.getPackaging() ) )
        {
            Project continuumProject = new Project();

            BuildDefinition bd = new BuildDefinition();

            bd.setArguments( "--batch-mode --non-recursive" );

            bd.setGoals( "clean:clean install" );

            bd.setBuildFile( "pom.xml" );

            try
            {
                Schedule schedule = store.getScheduleByName( DefaultContinuumInitializer.DEFAULT_SCHEDULE_NAME );

                bd.setSchedule( schedule );
            }
            catch ( ContinuumStoreException e )
            {
                getLogger().warn( "Can't get default schedule.", e );
            }

            continuumProject.addBuildDefinition( bd );

            try
            {
                builderHelper.mapMavenProjectToContinuumProject( mavenProject, continuumProject );
            }
            catch ( MavenBuilderHelperException e )
            {
                result.addWarning( ContinuumUtils.throwableToString( e ) );
            }

            result.addProject( continuumProject, MavenTwoBuildExecutor.ID );
        }

        List modules = mavenProject.getModules();

        String prefix = url.toExternalForm();

        String suffix = "";

        int i = prefix.indexOf( '?' );

        if ( i != -1 )
        {
            suffix = prefix.substring( i );

            prefix = prefix.substring( 0, i - POM_PART.length() );
        }
        else
        {
            prefix = prefix.substring( 0, prefix.length() - POM_PART.length() );
        }

        for ( Iterator it = modules.iterator(); it.hasNext(); )
        {
            String module = (String) it.next();

            String urlString = prefix + "/" + module + POM_PART + suffix;

            URL moduleUrl;

            try
            {
                moduleUrl = new URL( urlString );
            }
            catch ( MalformedURLException e )
            {
                result.addWarning( "Could not download project from '" + urlString + "'." );

                continue;
            }

            readModules( moduleUrl, result, false, username, password );
        }
    }

    private ProjectGroup buildProjectGroup( MavenProject mavenProject )
    {
        ProjectGroup projectGroup = new ProjectGroup();

        // ----------------------------------------------------------------------
        // Group id
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( mavenProject.getGroupId() ) )
        {
            return null;
        }

        projectGroup.setGroupId( mavenProject.getGroupId() );

        // ----------------------------------------------------------------------
        // Name
        // ----------------------------------------------------------------------

        String name = mavenProject.getName();

        if ( StringUtils.isEmpty( name ) )
        {
            name = mavenProject.getGroupId();
        }

        projectGroup.setName( name );

        // ----------------------------------------------------------------------
        // Description
        // ----------------------------------------------------------------------

        projectGroup.setDescription( mavenProject.getDescription() );

        return projectGroup;
    }
}
