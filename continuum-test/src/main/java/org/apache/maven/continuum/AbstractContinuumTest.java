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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildGroup;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.ConfigurableJdoFactory;
import org.codehaus.plexus.util.FileUtils;
import org.jpox.SchemaTool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.PersistenceManager;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTest
    extends PlexusTestCase
{
    /**
     * When adding projects using addProject( project ) the project will be
     * put in this group. All project has to belong to a group.
     */
    private static ContinuumProjectGroup defaultProjectGroup;

    private ContinuumStore store;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void setUp()
        throws Exception
    {
        super.setUp();

        setUpConfigurationService( (ConfigurationService) lookup( ConfigurationService.ROLE ) );

        getStore();
    }

    public static void setUpConfigurationService( ConfigurationService configurationService )
        throws Exception
    {
        configurationService.setInMemoryMode( true );

        configurationService.setBuildOutputDirectory( getTestFile( "target/build-output" ) );

        configurationService.setWorkingDirectory( getTestFile( "target/working-directory" ) );
    }

    public static ContinuumProjectGroup getDefaultProjectGroup( ContinuumStore store )
        throws ContinuumStoreException
    {
        if ( defaultProjectGroup == null )
        {
            ContinuumProjectGroup projectGroup = new ContinuumProjectGroup();

            projectGroup.setName( "Test Project Group" );

            projectGroup.setGroupId( "foo.test" );

            projectGroup.setDescription( "This is the default group that all projects will be " +
                                         "added to when using addProject()." );

            defaultProjectGroup = store.addProjectGroup( projectGroup );
        }

        return defaultProjectGroup;
    }

    // ----------------------------------------------------------------------
    // Store
    // ----------------------------------------------------------------------

    protected ContinuumStore getStore()
        throws Exception
    {
        if ( store != null )
        {
            return store;
        }

        // ----------------------------------------------------------------------
        // Set up the JDO factory
        // ----------------------------------------------------------------------

        Object o = lookup( JdoFactory.ROLE );

        assertEquals( DefaultConfigurableJdoFactory.class.getName(), o.getClass().getName() );

        ConfigurableJdoFactory jdoFactory = (ConfigurableJdoFactory) o;

        jdoFactory.setPersistenceManagerFactoryClass( "org.jpox.PersistenceManagerFactoryImpl" );

        jdoFactory.setDriverName( "org.hsqldb.jdbcDriver" );

        jdoFactory.setUrl( "jdbc:hsqldb:mem:foo" );

        jdoFactory.setUserName( "sa" );

        jdoFactory.setPassword( "" );

        jdoFactory.setProperty( "org.jpox.transactionIsolation", "READ_UNCOMMITTED" );

        jdoFactory.setProperty( "org.jpox.poid.transactionIsolation", "READ_UNCOMMITTED" );

        // ----------------------------------------------------------------------
        // Create the tables
        // ----------------------------------------------------------------------

        Properties properties = jdoFactory.getProperties();

        for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }

        String[] files = new String[]{
            getTestPath( "../continuum-model/src/main/resources/META-INF/package.jdo" ),
        };

        boolean verbose = false;

        SchemaTool.createSchemaTables( files, verbose );

        // ----------------------------------------------------------------------
        // Check the configuration
        // ----------------------------------------------------------------------

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        assertNotNull( pmf );

        PersistenceManager pm = pmf.getPersistenceManager();

        pm.close();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        return store;
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

    public static MavenTwoProject makeStubMavenTwoProject( String name )
    {
        return makeMavenTwoProject( name,
                                    "foo@bar.com",
                                    "1.0",
                                    "" );
    }

    public static MavenTwoProject makeMavenTwoProject( String name,
                                                       String emailAddress,
                                                       String version,
                                                       String commandLineArguments )
    {
        MavenTwoProject project = new MavenTwoProject();

        makeProject( project,
                     name,
                     version,
                     commandLineArguments,
                     "maven2" );

        List notifiers = createMailNotifierList( emailAddress );

        project.setNotifiers( notifiers );

        return project;
    }

    // ----------------------------------------------------------------------
    // Shell Project Generators
    // ----------------------------------------------------------------------

    public static ShellProject makeStubShellProject( String name )
    {
        ShellProject project = new ShellProject();

        makeProject( project,
                     name,
                     "1.0",
                     "",
                     "shell" );

        project.setExecutable( "script.sh" );

        return project;
    }

    public static ContinuumProject makeProject( ContinuumProject project,
                                                String name,
                                                String version,
                                                String commandLineArguments,
                                                String executorId )
    {
        project.setName( name );
        project.setVersion( version );
        project.setCommandLineArguments( commandLineArguments );
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
        if ( project.getProjectGroup() == null )
        {
            project.setProjectGroup( getDefaultProjectGroup( store ) );
        }

        assertNotNull( "project group == null", project.getProjectGroup() );

        assertTrue( "!JDOHelper.isDetached( project.getProjectGroup() )", JDOHelper.isDetached( project.getProjectGroup() ) );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumProject addedProject = store.addProject( project );

        assertNotNull( addedProject );

        assertNotNull( "project group == null", addedProject.getProjectGroup() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        scmResult.setCommandOutput( "commandOutput" );

        scmResult.setProviderMessage( "providerMessage" );

        ScmFile scmFile = new ScmFile();

        scmFile.setPath( "/foo" );

        scmResult.addFile( scmFile );

//        addedProject = setCheckoutDone( store, addedProject, scmResult, null, null );

        assertNotNull( "project group == null", addedProject.getProjectGroup() );

        return (MavenTwoProject) addedProject;
    }

    public static MavenTwoProject addMavenTwoProject( ContinuumStore store,
                                                      String name )
        throws Exception
    {
        return addMavenTwoProject( store, makeStubMavenTwoProject( name ) );
    }

    public static MavenTwoProject addMavenTwoProject( ContinuumStore store,
                                                      String name,
                                                      String nagEmailAddress,
                                                      String version,
                                                      String commandLineArguments )
        throws Exception
    {
        ContinuumProject project = store.addProject(
            makeMavenTwoProject( name,
                                 nagEmailAddress,
                                 version,
                                 commandLineArguments ) );

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

        project.setCheckOutErrorException( ContinuumUtils.throwableMessagesToString( exception ) );

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

        String outputFile = store.getBuildOutputFile( build.getId() ).getAbsolutePath();

        try
        {
            FileUtils.fileWrite( outputFile, result.getOutput() );
        }
        catch ( IOException e )
        {
            // do nothing
        }

        store.updateBuild( build );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertProjectEquals( ContinuumProject expected,
                                     ContinuumProject actual )
    {
        assertProjectEquals( expected.getName(),
                             expected.getNotifiers(),
                             expected.getVersion(),
                             expected.getCommandLineArguments(),
                             expected.getExecutorId(),
                             actual );
    }

    public void assertProjectEquals( String name,
                                     String emailAddress,
                                     String version,
                                     String commandLineArguments,
                                     String builderId,
                                     ContinuumProject actual )
    {
        assertProjectEquals( name,
                             createMailNotifierList( emailAddress ),
                             version,
                             commandLineArguments,
                             builderId,
                             actual );
    }

    public void assertProjectEquals( String name,
                                     List notifiers,
                                     String version,
                                     String commandLineArguments,
                                     String builderId,
                                     ContinuumProject actual )
    {
        assertEquals( "project.name", name, actual.getName() );

//        assertEquals( "project.scmUrl", scmUrl, actual.getScmUrl() );

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

//        assertEquals( "project.workingDirectory", workingDirectory, actual.getWorkingDirectory() );
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
