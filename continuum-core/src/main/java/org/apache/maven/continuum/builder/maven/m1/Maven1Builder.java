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
import java.util.Properties;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builder.AbstractContinuumBuilder;
import org.apache.maven.continuum.builder.ContinuumBuilder;
import org.apache.maven.continuum.builder.shell.ExecutionResult;
import org.apache.maven.continuum.builder.shell.ShellCommandHelper;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: Maven1Builder.java,v 1.2 2005/04/03 21:27:17 trygvis Exp $
 */
public class Maven1Builder
    extends AbstractContinuumBuilder
    implements ContinuumBuilder
{
    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = "maven-1";

    /** @requirement */
    private ShellCommandHelper shellCommandHelper;

    /** @requirement */
    private MavenOneMetadataHelper metadataHelper;

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

//    public ContinuumProject createProjectFromMetadata( URL metadata )
//        throws ContinuumException
//    {
//        File pomFile = createMetadataFile( metadata );
//
//        ContinuumProject project = new ContinuumProject();
//
//        mapMetadata( pomFile, project );
//
//        return project;
//    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumException
    {
        File projectXmlFile = new File( workingDirectory, "project.xml" );

        if ( !projectXmlFile.isFile() )
        {
            throw new ContinuumException( "Could not find Maven project descriptor." );
        }

        metadataHelper.mapMetadata( projectXmlFile, project );
    }
}
