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

import java.io.File;
import java.net.URL;

import org.apache.maven.continuum.builder.AbstractContinuumBuilder;
import org.apache.maven.continuum.builder.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.builder.maven.m2.MavenShellBuilder;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id:$
 */
public class MavenTwoContinuumProjectBuilder
    extends AbstractLogEnabled
    implements ContinuumProjectBuilder
{
    public static final String ID = "maven-two-builder";

    /** @requirement */
    private MavenBuilderHelper builderHelper;

    // ----------------------------------------------------------------------
    // ProjectCreator Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult createProjectsFromMetadata( URL url )
        throws ContinuumProjectBuilderException
    {
        // ----------------------------------------------------------------------
        // We need to roll the project data into a file so that we can use it
        // ----------------------------------------------------------------------

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        MavenTwoProject project = new MavenTwoProject();

        try
        {
            File file = AbstractContinuumBuilder.createMetadataFile( url );

            builderHelper.mapMetadataToProject( file, project );
        }
        catch ( Exception e )
        {
            throw new ContinuumProjectBuilderException( "Cannot create continuum project.", e );
        }

        project.setExecutorId( MavenShellBuilder.ID );

        result.addProject( project );

        return result;
    }
}
