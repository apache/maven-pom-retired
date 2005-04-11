package org.apache.maven.continuum.builder.maven.m1;

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
import java.net.URL;
import java.util.Properties;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builder.AbstractContinuumBuilder;
import org.apache.maven.continuum.builder.ContinuumBuilder;
import org.apache.maven.continuum.builder.shell.ExecutionResult;
import org.apache.maven.continuum.builder.shell.ShellCommandHelper;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: Maven1Builder.java,v 1.2 2005/04/03 21:27:17 trygvis Exp $
 */
public class Maven1Builder
    extends AbstractContinuumBuilder
    implements ContinuumBuilder
{
    public final static String CONFIGURATION_GOALS = "goals";

    /** @requirement */
    private ShellCommandHelper shellCommandHelper;

    /** @configuration */
    private String mavenCommand;

    // ----------------------------------------------------------------------
    // Builder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildResult build( ContinuumProject project )
        throws ContinuumException
    {
        Properties configuration = project.getConfiguration();

        File workingDirectory = new File( project.getWorkingDirectory() );

        String[] goals = getConfigurationStringArray( configuration, CONFIGURATION_GOALS, "," );

        ExecutionResult executionResult;

        try
        {
            executionResult = shellCommandHelper.executeShellCommand( workingDirectory, mavenCommand, goals );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while executing shell command.", e );
        }

        boolean success = executionResult.getExitCode() == 0;

        Maven1BuildResult result = new Maven1BuildResult();

        result.setSuccess( success );

        result.setStandardOutput( executionResult.getStandardOutput() );

        result.setStandardError( executionResult.getStandardError() );

        result.setExitCode( executionResult.getExitCode() );

        return result;
    }

    public ContinuumProject createProjectFromMetadata( URL metadata )
        throws ContinuumException
    {
        File pomFile = createMetadataFile( metadata );

        ContinuumProject project = new ContinuumProject();

        mapMetadata( pomFile, project );

        return project;
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumException
    {
        File projectXmlFile = getProjectXmlFile( workingDirectory );

        mapMetadata( projectXmlFile, project );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void mapMetadata( File metadata, ContinuumProject project )
        throws ContinuumException
    {
        Xpp3Dom mavenProject;

        try
        {
            mavenProject = Xpp3DomBuilder.build( new FileReader( metadata ) );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while reading maven POM.", e );
        }

        // ----------------------------------------------------------------------
        // Populating the descriptor
        // ----------------------------------------------------------------------

        // Name
        String name = getValue( mavenProject, "name", project.getName() );

        if ( StringUtils.isEmpty( name ) )
        {
            throw new ContinuumException( "Missing <name> from the project descriptor." );
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
                throw new ContinuumException( "The project descriptor is missing the SCM information." );
            }
        }
        else
        {
            scmConnection = getValue( repository, "developerConnection", project.getScmUrl() );

            scmConnection = getValue( repository, "connection", scmConnection );

            if ( StringUtils.isEmpty( scmConnection ) )
            {
                throw new ContinuumException( "Missing both anonymous and developer scm connection urls." );
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
                throw new ContinuumException( "Missing build section." );
            }
        }
        else
        {
            nagEmailAddress = getValue( build, "nagEmailAddress", project.getNagEmailAddress() );
        }

        if ( StringUtils.isEmpty( nagEmailAddress ) )
        {
            throw new ContinuumException( "Missing nag email address from the project descriptor." );
        }

        // Version
        String version = getValue( mavenProject, "currentVersion", project.getVersion() );

        if ( StringUtils.isEmpty( version ) )
        {
            throw new ContinuumException( "Missing version from the project descriptor." );
        }

        // Goals
        Properties configuration = new Properties();

        if ( !configuration.containsKey( CONFIGURATION_GOALS ) )
        {
            configuration.setProperty( CONFIGURATION_GOALS, "clean:clean, jar:install" );
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

    private File getProjectXmlFile( File basedir )
        throws ContinuumException
    {
        File projectXmlFile = new File( basedir, "project.xml" );

        if ( !projectXmlFile.isFile() )
        {
            throw new ContinuumException( "Could not find Maven project descriptor." );
        }

        return projectXmlFile;
    }

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
