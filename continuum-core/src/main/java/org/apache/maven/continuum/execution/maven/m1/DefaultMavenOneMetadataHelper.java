package org.apache.maven.continuum.execution.maven.m1;

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
import java.io.FileReader;
import java.util.Properties;

import org.apache.maven.continuum.project.ContinuumProject;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultMavenOneMetadataHelper
    extends AbstractLogEnabled
    implements MavenOneMetadataHelper
{
    // ----------------------------------------------------------------------
    // MavenOneMetadataHelper Implementation
    // ----------------------------------------------------------------------

    public void mapMetadata( File metadata, ContinuumProject project )
        throws MavenOneMetadataHelperException
    {
        Xpp3Dom mavenProject;

        try
        {
            mavenProject = Xpp3DomBuilder.build( new FileReader( metadata ) );
        }
        catch ( Exception e )
        {
            throw new MavenOneMetadataHelperException( "Error while reading maven POM.", e );
        }

        // ----------------------------------------------------------------------
        // Populating the descriptor
        // ----------------------------------------------------------------------

        // Name
        String name = getValue( mavenProject, "name", project.getName() );

        if ( StringUtils.isEmpty( name ) )
        {
            throw new MavenOneMetadataHelperException( "Missing <name> from the project descriptor." );
        }

        // Scm
        Xpp3Dom repository = mavenProject.getChild( "repository" );

        String scmConnection;

        if ( repository == null )
        {
            if ( !StringUtils.isEmpty( project.getScmUrl() ) )
            {
                scmConnection = project.getScmUrl();
            }
            else
            {
                throw new MavenOneMetadataHelperException( "The project descriptor is missing the SCM information." );
            }
        }
        else
        {
            scmConnection = getValue( repository, "developerConnection", project.getScmUrl() );

            scmConnection = getValue( repository, "connection", scmConnection );

            if ( StringUtils.isEmpty( scmConnection ) )
            {
                throw new MavenOneMetadataHelperException( "Missing both anonymous and developer scm connection urls." );
            }
        }

        // Nag email address
        Xpp3Dom build = mavenProject.getChild( "build" );

        String nagEmailAddress;

        if ( build == null )
        {
            if ( !StringUtils.isEmpty( project.getNagEmailAddress() ) )
            {
                nagEmailAddress = project.getNagEmailAddress();
            }
            else
            {
                throw new MavenOneMetadataHelperException( "Missing build section." );
            }
        }
        else
        {
            nagEmailAddress = getValue( build, "nagEmailAddress", project.getNagEmailAddress() );
        }

        if ( StringUtils.isEmpty( nagEmailAddress ) )
        {
            throw new MavenOneMetadataHelperException( "Missing nag email address from the project descriptor." );
        }

        // Version
        String version = getValue( mavenProject, "currentVersion", project.getVersion() );

        if ( StringUtils.isEmpty( version ) )
        {
            throw new MavenOneMetadataHelperException( "Missing version from the project descriptor." );
        }

        // Goals
        Properties configuration = new Properties();

        if ( StringUtils.isEmpty( configuration.getProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS ) ) )
        {
            configuration.setProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS, "clean:clean jar:install" );
        }

        // ----------------------------------------------------------------------
        // Make the project
        // ----------------------------------------------------------------------

        project.setName( name );

        project.setScmUrl( scmConnection );

        project.setNagEmailAddress( nagEmailAddress );

        project.setVersion( version );

        project.setConfiguration( configuration );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String getValue( Xpp3Dom dom, String key, String defaultValue )
    {
        Xpp3Dom child = dom.getChild( key );

        if ( child == null )
        {
            return defaultValue;
        }

        return child.getValue();
    }
}
