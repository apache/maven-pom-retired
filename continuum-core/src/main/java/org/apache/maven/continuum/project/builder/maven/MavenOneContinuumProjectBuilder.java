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

import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelper;
import org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelperException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
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

    // ----------------------------------------------------------------------
    // ProjectCreator Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url )
    {
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        File pomFile;

        try
        {
            pomFile = createMetadataFile( url );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not download the URL", e );

            result.addWarning( "Could not download the URL '" + url + "'." );

            return result;
        }

        try
        {
            Project project = new Project();

            metadataHelper.mapMetadata( pomFile, project );

            result.addProject( project, MavenOneBuildExecutor.ID );
        }
        catch ( MavenOneMetadataHelperException e )
        {
            result.addWarning( e.getMessage() );
        }

        // ----------------------------------------------------------------------
        // This is a hack.
        // ----------------------------------------------------------------------

        ProjectGroup projectGroup = new ProjectGroup();

        projectGroup.setName( "Maven 1 group" );

        projectGroup.setGroupId( "dummy" );

        result.addProjectGroup( projectGroup );

        return result;
    }
}
