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

import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = "maven-1";

    /**
     * @plexus.requirement
     */
    private MavenOneMetadataHelper metadataHelper;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public MavenOneBuildExecutor()
    {
        super( ID, true );
    }

    // ----------------------------------------------------------------------
    // Builder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException
    {
        // TODO: get from installation
//        String executable = project.getExecutable();
        String executable = "maven";

        String arguments = "";

        String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

        if ( !StringUtils.isEmpty( buildFile ) && !"project.xml".equals( buildFile ) )
        {
            arguments = "-p " + buildFile + " ";
        }

        arguments += StringUtils.clean( buildDefinition.getArguments() ) + " " +
            StringUtils.clean( buildDefinition.getGoals() );

        return executeShellCommand( project, executable, arguments, buildOutput );
    }

    public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        File projectXmlFile = null;

        if ( buildDefinition != null )
        {
            String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

            if ( !StringUtils.isEmpty( buildFile ) )
            {
                projectXmlFile = new File( workingDirectory, buildFile );
            }
        }

        if ( projectXmlFile == null )
        {
            projectXmlFile = new File( workingDirectory, "project.xml" );
        }

        if ( !projectXmlFile.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor." );
        }

        try
        {
            metadataHelper.mapMetadata( projectXmlFile, project );
        }
        catch ( MavenOneMetadataHelperException e )
        {
            throw new ContinuumBuildExecutorException( "Error while mapping metadata.", e );
        }
    }
}
