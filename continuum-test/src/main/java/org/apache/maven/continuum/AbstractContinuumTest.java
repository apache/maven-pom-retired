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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumBuildGroup;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.configuration.ConfigurationService;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTest
    extends PlexusTestCase
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void setUp()
        throws Exception
    {
        super.setUp();

        setUpConfigurationService( (ConfigurationService) lookup( ConfigurationService.ROLE ) );
    }

    public static void setUpConfigurationService( ConfigurationService configurationService )
        throws Exception
    {
        configurationService.setBuildOutputDirectory( getTestFile( "target/build-output" ) );
    }

    // ----------------------------------------------------------------------
    // Store
    // ----------------------------------------------------------------------

    protected ContinuumStore getStore()
        throws Exception
    {
        return (ContinuumStore) lookup( ContinuumStore.ROLE );
    }

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
                                    PlexusTestCase.getTestFile( "plexus-temp" ).getAbsolutePath() );
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
                     "maven2" );

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
                     PlexusTestCase.getTestFile( "plexus-temp" ).getAbsolutePath(),
                     "shell" );

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
        if ( emailAddress == null )
        {
            return null;
        }

        ContinuumNotifier notifier = new ContinuumNotifier();

        notifier.setType( "mail" );

        Properties props = new Properties();

        props.put( "address", emailAddress );

        notifier.setConfiguration( props );

        List notifiers = new ArrayList();

        notifiers.add( notifier );

        return notifiers;
    }

    // ----------------------------------------------------------------------
    // Public utility methods
    // ----------------------------------------------------------------------

    public static MavenTwoProject addMavenTwoProject( ContinuumStore store,
                                                      MavenTwoProject project )
    throws Exception
    {
        ContinuumProject addedProject = store.addProject( project );

        assertNotNull( addedProject );

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        scmResult.setCommandOutput( "commandOutput" );

        scmResult.setProviderMessage( "providerMessage" );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        scmResult.addFile( scmFile );

        addedProject = setCheckoutDone( store, addedProject, scmResult, null, null );

        return (MavenTwoProject) addedProject;
    }

    public static MavenTwoProject addMavenTwoProject( ContinuumStore store,
                                                      String name,
                                                      String scmUrl )
    throws Exception
    {
        return addMavenTwoProject( store, makeStubMavenTwoProject( name, scmUrl ) );
    }

    public static MavenTwoProject addMavenTwoProject( ContinuumStore store,
                                                      String name,
                                                      String scmUrl,
                                                      String nagEmailAddress,
                                                      String version,
                                                      String commandLineArguments,
                                                      String workingDirectory )
    throws Exception
    {
        ContinuumProject project = store.addProject(
            makeMavenTwoProject( name,
                                 scmUrl,
                                 nagEmailAddress,
                                 version,
                                 commandLineArguments,
                                 workingDirectory ) );

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        project = setCheckoutDone( store, project, scmResult, null, null );

        assertNotNull( project );

        return (MavenTwoProject) project;
    }

    public static ContinuumBuild createBuild( ContinuumStore store,
                                              String projectId,
                                              boolean forced )
    throws ContinuumStoreException
    {
        ContinuumBuild build = new ContinuumBuild();

        build.setStartTime( System.currentTimeMillis() );

        build.setState( ContinuumProjectState.BUILDING );

        build.setForced( forced );

        return store.addBuild( projectId, build );
    }

    public static ContinuumProject setCheckoutDone( ContinuumStore store,
                                                    ContinuumProject project,
                                                    ScmResult scmResult,
                                                    String errorMessage,
                                                    Throwable exception )
    throws ContinuumStoreException
    {
        project.setScmResult( scmResult );

        project.setCheckOutErrorMessage( errorMessage );

        project.setCheckOutErrorException( ContinuumUtils.throwableToString( exception ) );

        return store.updateProject( project );
    }

    public static void setBuildResult( ContinuumStore store,
                                       ContinuumBuild build,
                                       int state,
                                       ContinuumBuildExecutionResult result,
                                       ScmResult scmResult,
                                       Throwable error )
        throws ContinuumStoreException
    {
        build.setState( state );

        build.setEndTime( new Date().getTime() );

        build.setError( ContinuumUtils.throwableToString( error ) );

        build.setScmResult( scmResult );

        // ----------------------------------------------------------------------
        // Copy over the build result
        // ----------------------------------------------------------------------

        build.setExitCode( result.getExitCode() );

        store.setBuildOutput( build.getId(), result.getOutput() );

        store.updateBuild( build );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertProjectEquals( ContinuumProject expected,
                                     ContinuumProject actual )
    {
        assertProjectEquals( expected.getName(),
                             expected.getScmUrl(),
                             expected.getNotifiers(),
                             expected.getVersion(),
                             expected.getCommandLineArguments(),
                             expected.getExecutorId(),
                             expected.getWorkingDirectory(),
                             actual );
    }

    public void assertProjectEquals( String name,
                                     String scmUrl,
                                     String emailAddress,
                                     String version,
                                     String commandLineArguments,
                                     String builderId,
                                     String workingDirectory,
                                     ContinuumProject actual )
    {
        assertProjectEquals( name,
                             scmUrl,
                             createMailNotifierList( emailAddress ),
                             version,
                             commandLineArguments,
                             builderId,
                             workingDirectory,
                             actual );
    }

    public void assertProjectEquals( String name,
                                     String scmUrl,
                                     List notifiers,
                                     String version,
                                     String commandLineArguments,
                                     String builderId,
                                     String workingDirectory,
                                     ContinuumProject actual )
    {
        assertEquals( "project.name", name, actual.getName() );

        assertEquals( "project.scmUrl", scmUrl, actual.getScmUrl() );

        if ( notifiers != null )
        {
            assertNotNull( "project.notifiers", actual.getNotifiers() );

            assertEquals( "project.notifiers.size", notifiers.size(), actual.getNotifiers().size() );

            for ( int i = 0; i < notifiers.size(); i++ )
            {
                ContinuumNotifier notifier = (ContinuumNotifier) notifiers.get( i );

                ContinuumNotifier actualNotifier = (ContinuumNotifier) actual.getNotifiers().get( i );

                assertEquals( "project.notifiers.notifier.type", notifier.getType(), actualNotifier.getType() );

                assertEquals( "project.notifiers.notifier.configuration.address",
                              notifier.getConfiguration().get( "address" ),
                              actualNotifier.getConfiguration().get( "address" ) );
            }
        }

        assertEquals( "project.version", version, actual.getVersion() );

        assertEquals( "project.commandLineArguments", commandLineArguments, actual.getCommandLineArguments() );

        assertEquals( "project.executorId", builderId, actual.getExecutorId() );

        assertEquals( "project.workingDirectory", workingDirectory, actual.getWorkingDirectory() );
    }

    // ----------------------------------------------------------------------
    // Simple utils
    // ----------------------------------------------------------------------

    public ContinuumBuildSettings createStubBuildSettings( String name, String jdkVersion )
    {
        ContinuumBuildSettings buildSettings = new ContinuumBuildSettings();

        buildSettings.setName( name );

        buildSettings.setJdkVersion( jdkVersion );

        return buildSettings;
    }

    public ContinuumBuildGroup createStubBuildGroup( String name, String description )
    {
        ContinuumBuildGroup buildGroup = new ContinuumBuildGroup();

        buildGroup.setName( name );

        buildGroup.setDescription( description );

        return buildGroup;
    }

    public ContinuumProjectGroup createStubProjectGroup( String name, String description )
    {
        ContinuumProjectGroup projectGroup = new ContinuumProjectGroup();

        projectGroup.setName( name );

        projectGroup.setDescription( description );

        return projectGroup;
    }
}
