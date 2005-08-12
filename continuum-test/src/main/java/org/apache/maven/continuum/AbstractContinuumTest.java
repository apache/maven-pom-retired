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
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.jdo.ConfigurableJdoFactory;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    private static ProjectGroup defaultProjectGroup;

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

    public static ProjectGroup getDefaultProjectGroup( ContinuumStore store )
    {
        if ( defaultProjectGroup == null )
        {
            ProjectGroup projectGroup = new ProjectGroup();

            projectGroup.setName( "Test Project Group" );

            projectGroup.setGroupId( "foo.test" );

            projectGroup.setDescription(
                "This is the default group that all projects will be " + "added to when using addProject()." );

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

        jdoFactory.setUrl( "jdbc:hsqldb:mem:" + getClass().getName() + "." + getName() );

        jdoFactory.setUserName( "sa" );

        jdoFactory.setPassword( "" );

        jdoFactory.setProperty( "org.jpox.transactionIsolation", "READ_UNCOMMITTED" );

        jdoFactory.setProperty( "org.jpox.poid.transactionIsolation", "READ_UNCOMMITTED" );

        jdoFactory.setProperty( "org.jpox.autoCreateTables", "true" );

        jdoFactory.setProperty( "org.jpox.autoCreateColumns", "true" );

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

    public static Project makeStubProject( String name )
    {
        return makeProject( name, "foo@bar.com", "1.0" );
    }

    public static Project makeProject( String name, String emailAddress, String version )
    {
        Project project = new Project();

        makeProject( project, name, version );

        List notifiers = createMailNotifierList( emailAddress );

        project.setNotifiers( notifiers );

        return project;
    }

    // ----------------------------------------------------------------------
    // Shell Project Generators
    // ----------------------------------------------------------------------

    public static Project makeStubShellProject( String name )
    {
        Project project = new Project();

        makeProject( project, name, "1.0" );

        return project;
    }

    public static Project makeProject( Project project, String name, String version )
    {
        project.setName( name );
        project.setVersion( version );

        return project;
    }

    protected static List createMailNotifierList( String emailAddress )
    {
        if ( emailAddress == null )
        {
            return null;
        }

        ProjectNotifier notifier = new ProjectNotifier();

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

    public static Project addProject( ContinuumStore store, Project project )
        throws Exception
    {
        if ( project.getProjectGroup() == null )
        {
            project.setProjectGroup( getDefaultProjectGroup( store ) );
        }

        assertNotNull( "project group == null", project.getProjectGroup() );

        assertTrue( "!JDOHelper.isDetached( project.getProjectGroup() )",
                    JDOHelper.isDetached( project.getProjectGroup() ) );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        getDefaultProjectGroup( store ).addProject( project );
        store.updateProjectGroup( getDefaultProjectGroup( store ) );

        assertNotNull( "project group == null", project.getProjectGroup() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        scmResult.setCommandOutput( "commandOutput" );

        scmResult.setProviderMessage( "providerMessage" );

        return project;
    }

    public static Project addProject( ContinuumStore store, String name )
        throws Exception
    {
        return addProject( store, makeStubProject( name ) );
    }

    public static Project addProject( ContinuumStore store, String name, String nagEmailAddress, String version )
        throws Exception
    {
        Project project = makeProject( name, nagEmailAddress, version );

        ProjectGroup defaultProjectGroup = getDefaultProjectGroup( store );
        defaultProjectGroup.addProject( project );
        store.updateProjectGroup( defaultProjectGroup );

        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( true );

        setCheckoutDone( store, project, scmResult );

        assertNotNull( project );

        return project;
    }

    public static void setCheckoutDone( ContinuumStore store, Project project, ScmResult scmResult )
        throws ContinuumStoreException
    {
        project.setCheckoutResult( scmResult );

        store.updateProject( project );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertProjectEquals( Project expected, Project actual )
    {
        assertProjectEquals( expected.getName(), expected.getNotifiers(), expected.getVersion(), actual );
    }

    public void assertProjectEquals( String name, String emailAddress, String version, Project actual )
    {
        assertProjectEquals( name, createMailNotifierList( emailAddress ), version, actual );
    }

    public void assertProjectEquals( String name, List notifiers, String version, Project actual )
    {
        assertEquals( "project.name", name, actual.getName() );

//        assertEquals( "project.scmUrl", scmUrl, actual.getScmUrl() );

        if ( notifiers != null )
        {
            assertNotNull( "project.notifiers", actual.getNotifiers() );

            assertEquals( "project.notifiers.size", notifiers.size(), actual.getNotifiers().size() );

            for ( int i = 0; i < notifiers.size(); i++ )
            {
                ProjectNotifier notifier = (ProjectNotifier) notifiers.get( i );

                ProjectNotifier actualNotifier = (ProjectNotifier) actual.getNotifiers().get( i );

                assertEquals( "project.notifiers.notifier.type", notifier.getType(), actualNotifier.getType() );

                assertEquals( "project.notifiers.notifier.configuration.address",
                              notifier.getConfiguration().get( "address" ),
                              actualNotifier.getConfiguration().get( "address" ) );
            }
        }

        assertEquals( "project.version", version, actual.getVersion() );
    }

    // ----------------------------------------------------------------------
    // Simple utils
    // ----------------------------------------------------------------------

    public ProjectGroup createStubProjectGroup( String name, String description )
    {
        ProjectGroup projectGroup = new ProjectGroup();

        projectGroup.setName( name );

        projectGroup.setDescription( description );

        return projectGroup;
    }
}
