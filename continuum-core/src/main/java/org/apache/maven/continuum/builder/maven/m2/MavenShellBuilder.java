package org.apache.maven.continuum.builder.maven.m2;

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
import java.util.Arrays;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builder.shell.ShellBuilder;
import org.apache.maven.continuum.project.ContinuumProject;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: MavenShellBuilder.java,v 1.2 2005/04/07 23:27:39 trygvis Exp $
 */
public class MavenShellBuilder
    extends ShellBuilder
{
    public final static String CONFIGURATION_GOALS = "goals";

    /** @requirement */
    private MavenBuilderHelper builderHelper;

    /** @configuration */
    private String executable;

    /** @configuration */
    private String arguments;

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumProject createProjectFromMetadata( URL metadata )
        throws ContinuumException
    {
        return builderHelper.createProjectFromMetadata( metadata );
    }

    public void updateProjectFromCheckOut( File workingDirectory, ContinuumProject project )
        throws ContinuumException
    {
        builderHelper.updateProjectFromMetadata( workingDirectory, project );
    }

    // ----------------------------------------------------------------------
    // ShellBuilder Implementation
    // ----------------------------------------------------------------------

    protected boolean prependWorkingDirectoryIfMissing()
    {
        return false;
    }

    protected String getExecutable( ContinuumProject project )
        throws ContinuumException
    {
        return executable;
    }

    protected String[] getArguments( ContinuumProject project )
        throws ContinuumException
    {
        String[] a = splitAndTrimString( this.arguments, " " );

        String[] goals = getConfigurationStringArray( project.getConfiguration(), CONFIGURATION_GOALS, "," );

        String[] arguments = new String[ a.length + goals.length ];

        System.arraycopy( a, 0, arguments, 0, a.length );

        System.arraycopy( goals, 0, arguments, a.length, goals.length );

        System.err.println( "arguments: " + Arrays.asList( arguments ) );

        return arguments;
    }
}
