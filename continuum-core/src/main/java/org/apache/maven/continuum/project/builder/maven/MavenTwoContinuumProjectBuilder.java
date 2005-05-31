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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelperException;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;

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

    /** @requirement */
    private MavenBuilderHelper builderHelper;

    /** @configuration */
    private List excludedPackagingTypes;

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

        try
        {
            readModules( url, result );
        }
        catch ( MalformedURLException e )
        {
            throw new ContinuumProjectBuilderException( "Error while building Maven project.", e );
        }

        return result;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void readModules( URL url, ContinuumProjectBuildingResult result )
        throws MalformedURLException, ContinuumProjectBuilderException
    {
        MavenProject mavenProject;

        try
        {
            mavenProject = builderHelper.getMavenProject( createMetadataFile( url ) );
        }
        catch ( MavenBuilderHelperException e )
        {
            throw new ContinuumProjectBuilderException( "Error while building Maven project.", e );
        }

        if ( !excludedPackagingTypes.contains( mavenProject.getPackaging() ) )
        {
            MavenTwoProject continuumProject = new MavenTwoProject();

            builderHelper.mapMavenProjectToContinuumProject( mavenProject, continuumProject );

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

            URL moduleUrl = new URL( prefix + "/" + module + POM_PART + suffix );

            readModules( moduleUrl, result );
        }
    }
}
