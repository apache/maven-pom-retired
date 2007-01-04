package org.apache.maven.continuum.project.builder.maven;

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

import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelper;
import org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelperException;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.apache.maven.continuum.project.builder.ContinuumProjectBuilder"
 *   role-hint="maven-one-builder"
 */
public class MavenOneContinuumProjectBuilder
    extends AbstractContinuumProjectBuilder
    implements ContinuumProjectBuilder
{
    public static final String ID = "maven-one-builder";

    /**
     * @plexus.requirement
     */
    private MavenOneMetadataHelper metadataHelper;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // ProjectCreator Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
    {
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        File pomFile;

        pomFile = createMetadataFile( result, url, username, password );
        
        if ( pomFile == null )
        {
            return result;
        }

        Project project = new Project();

        try
        {
            metadataHelper.mapMetadata( result, pomFile, project );
            
            if ( result.hasErrors() )
            {
                return result;
            }

            BuildDefinition bd = new BuildDefinition();

            bd.setDefaultForProject( true );

            bd.setArguments( "" );

            bd.setGoals( "clean:clean jar:install" );

            bd.setBuildFile( "project.xml" );

            try
            {
                Schedule schedule = store.getScheduleByName( DefaultContinuumInitializer.DEFAULT_SCHEDULE_NAME );

                bd.setSchedule( schedule );
            }
            catch ( ContinuumStoreException e )
            {
                getLogger().error( "Can't get default schedule.", e );
            }

            project.addBuildDefinition( bd );

            result.addProject( project, MavenOneBuildExecutor.ID );
        }
        catch ( MavenOneMetadataHelperException e )
        {
            getLogger().error( "Unknown error while processing metadata", e );
            
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
        }

        ProjectGroup projectGroup = new ProjectGroup();

        // ----------------------------------------------------------------------
        // Group id
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( project.getGroupId() ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_GROUPID );
        }

        projectGroup.setGroupId( project.getGroupId() );

        // ----------------------------------------------------------------------
        // Name
        // ----------------------------------------------------------------------

        String name = project.getName();

        if ( StringUtils.isEmpty( name ) )
        {
            name = project.getGroupId();
        }

        projectGroup.setName( name );

        // ----------------------------------------------------------------------
        // Description
        // ----------------------------------------------------------------------

        projectGroup.setDescription( project.getDescription() );

        result.addProjectGroup( projectGroup );

        return result;
    }
}
