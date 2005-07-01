package org.apache.maven.continuum;

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

import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ShellProject;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTest
    extends PlexusTestCase
{
    // ----------------------------------------------------------------------
    // Build Executor
    // ----------------------------------------------------------------------

    protected ContinuumBuildExecutor getBuildExecutor( String id )
        throws Exception
    {
        ContinuumBuildExecutor buildExecutor = (ContinuumBuildExecutor) lookup( ContinuumBuildExecutor.ROLE, id );

        assertNotNull( "Could not look up build executor '" + id + "'", buildExecutor );

        return buildExecutor;
    }

    protected ShellBuildExecutor getShellBuildExecutor()
        throws Exception
    {
        return (ShellBuildExecutor) getBuildExecutor( ShellBuildExecutor.ID );
    }

    // ----------------------------------------------------------------------
    // Maven 2 Project Generators
    // ----------------------------------------------------------------------

    public static MavenTwoProject makeStubMavenTwoProject( String name, String scmUrl )
    {
        return makeMavenTwoProject( name,
                                    scmUrl,
                                    "foo@bar.com",
                                    "1.0",
                                    "",
                                    getTestFile( "plexus-temp" ).getAbsolutePath() );
    }

    public static MavenTwoProject makeMavenTwoProject( String name,
                                                       String scmUrl,
                                                       String emailAddress,
                                                       String version,
                                                       String commandLineArguments,
                                                       String workingDirectory )
    {
        MavenTwoProject project = new MavenTwoProject();

        makeProject( project,
                     name,
                     scmUrl,
                     version,
                     commandLineArguments,
                     workingDirectory,
                     MavenTwoBuildExecutor.ID );

        List notifiers = createMailNotifierList( emailAddress );

        project.setNotifiers( notifiers );

        return project;
    }

    // ----------------------------------------------------------------------
    // Shell Project Generators
    // ----------------------------------------------------------------------

    public static ShellProject makeStubShellProject( String name, String scmUrl )
    {
        ShellProject project = new ShellProject();

        makeProject( project,
                     name,
                     scmUrl,
                     "1.0",
                     "",
                     getTestFile( "plexus-temp" ).getAbsolutePath(),
                     ShellBuildExecutor.ID );

        project.setExecutable( "script.sh" );

        return project;
    }

    public static ContinuumProject makeProject( ContinuumProject project,
                                                String name,
                                                String scmUrl,
                                                String version,
                                                String commandLineArguments,
                                                String workingDirectory,
                                                String executorId )
    {
        project.setName( name );
        project.setScmUrl( scmUrl );
        project.setVersion( version );
        project.setCommandLineArguments( commandLineArguments );
        project.setWorkingDirectory( workingDirectory );
        project.setExecutorId( executorId );

        return project;
    }

    protected static List createMailNotifierList( String emailAddress )
    {
        ContinuumNotifier notifier = new ContinuumNotifier();

        notifier.setType( "mail" );

        Properties props = new Properties();

        props.put( "address", emailAddress );

        notifier.setConfiguration( props );

        List notifiers = new ArrayList();

        notifiers.add( notifier );

        return notifiers;
    }
}
