package org.apache.maven.continuum.store;

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

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import javax.jdo.Extent;
import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class JdoContinuumStore
    extends AbstractContinuumStore
    implements ContinuumStore, Initializable
{
    /**
     * @plexus.requirement
     */
    private JdoFactory jdoFactory;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private PersistenceManagerFactory pmf;

    // ----------------------------------------------------------------------
    // Fetch Groups
    // ----------------------------------------------------------------------

    private static final String PROJECT_WITH_BUILDS_FETCH_GROUP = "project-with-builds";

    private static final String PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP = "project-with-checkout-result";

    private static final String BUILD_RESULT_WITH_DETAILS_FETCH_GROUP = "build-result-with-details";

    private static final String PROJECT_BUILD_DETAILS_FETCH_GROUP = "project-build-details";

    private static final String PROJECT_ALL_DETAILS_FETCH_GROUP = "project-all-details";

    private static final String DEFAULT_GROUP_ID = "default";

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        pmf = jdoFactory.getPersistenceManagerFactory();
    }

    // ----------------------------------------------------------------------
    // ContinuumStore Implementation
    // ----------------------------------------------------------------------

    public Project getProjectByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Project.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = (Collection) query.execute( name );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (Project) object;
        }
        finally
        {
            rollback( tx );
        }
    }


    public void updateBuildResult( BuildResult build )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        Project project = build.getProject();
        try
        {
            tx.begin();

            if ( !JDOHelper.isDetached( build ) )
            {
                throw new ContinuumStoreException( "Not detached: " + build );
            }

            pm.attachCopy( build, true );

            if ( !JDOHelper.isDetached( project ) )
            {
                throw new ContinuumStoreException( "Not detached: " + project );
            }

            project.setState( build.getState() );

            pm.attachCopy( project, true );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public void addBuildResult( Project project, BuildResult build )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.getFetchPlan().addGroup( PROJECT_WITH_BUILDS_FETCH_GROUP );

            Object objectId = pm.newObjectIdInstance( Project.class, new Integer( project.getId() ) );

            project = (Project) pm.getObjectById( objectId );

            build = (BuildResult) makePersistent( pm, build, false );

            // TODO: these are in the wrong spot - set them on success (though currently some depend on latest build being the one in progress)
            project.setLatestBuildId( build.getId() );
            project.setBuildNumber( project.getBuildNumber() + 1 );
            project.setState( ContinuumProjectState.BUILDING );

            project.addBuildResult( build );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( Project.class, new Integer( projectId ) );

            Project project = (Project) pm.getObjectById( id );

            int buildId = project.getLatestBuildId();

            if ( buildId > 0 )
            {
                id = pm.newObjectIdInstance( BuildResult.class, new Integer( buildId ) );

                Object object = pm.getObjectById( id );

                BuildResult build = (BuildResult) pm.detachCopy( object );

                tx.commit();

                return build;
            }
        }
        finally
        {
            rollback( tx );
        }
        return null;
    }

    public void removeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException
    {
        attachAndDelete( notifier );
    }

    public ProjectNotifier storeNotifier( ProjectNotifier notifier )
        throws ContinuumStoreException
    {
        updateObject( notifier );

        return notifier;
    }

    public void removeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException
    {
        attachAndDelete( buildDefinition );
    }

    public BuildDefinition storeBuildDefinition( BuildDefinition buildDefinition )
        throws ContinuumStoreException
    {
        updateObject( buildDefinition );

        return buildDefinition;
    }

    private Object makePersistent( PersistenceManager pm, Object object, boolean detach )
    {
        pm.makePersistent( object );

        Object id = pm.getObjectId( object );

        Object persistentObject = pm.getObjectById( id );

        if ( detach )
        {
            persistentObject = pm.detachCopy( persistentObject );
        }

        return persistentObject;
    }

    private Object getObjectFromQuery( Class clazz, String idField, String id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( clazz, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String " + idField );

            query.setFilter( "this." + idField + " == " + idField );

            Collection result = (Collection) query.execute( id );

            if ( result.size() == 0 )
            {
                throw new ContinuumObjectNotFoundException( clazz.getName(), id );
            }

            if ( result.size() > 1 )
            {
                throw new ContinuumStoreException( "A query for object of " + "type " + clazz.getName() + " on the " +
                    "field '" + idField + "' returned more than one object." );
            }

            pm.getFetchPlan().addGroup( fetchGroup );

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return object;
        }
        finally
        {
            rollback( tx );
        }
    }

    private void attachAndDelete( Object object )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.attachCopy( object, true );

            pm.deletePersistent( object );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    // ----------------------------------------------------------------------
    // Transaction Management
    // ----------------------------------------------------------------------

    private void rollback( Transaction tx )
    {
        PersistenceManager pm = tx.getPersistenceManager();

        try
        {
            if ( tx.isActive() )
            {
                tx.rollback();
            }
        }
        finally
        {
            closePersistenceManager( pm );
        }
    }

    private void closePersistenceManager( PersistenceManager pm )
    {
        try
        {
            pm.close();
        }
        catch ( JDOUserException e )
        {
            getLogger().warn( "Error while closing the persistence manager.", e );
        }
    }

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (ProjectGroup) getObjectById( ProjectGroup.class, projectGroupId );
    }

    private Object getObjectById( Class clazz, int id )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return getObjectById( clazz, id, null );
    }

    private Object getObjectById( Class clazz, int id, String fetchGroup )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            if ( fetchGroup != null )
            {
                pm.getFetchPlan().addGroup( fetchGroup );
            }

            Object objectId = pm.newObjectIdInstance( clazz, new Integer( id ) );

            Object object = pm.getObjectById( objectId );

            object = pm.detachCopy( object );

            tx.commit();

            return object;
        }
        catch ( JDOObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( clazz.getName(), Integer.toString( id ) );
        }
        catch ( JDOException e )
        {
            throw new ContinuumStoreException( "Error handling JDO", e );
        }
        finally
        {
            rollback( tx );
        }
    }

    public void updateProjectGroup( ProjectGroup group )
        throws ContinuumStoreException
    {
        updateObject( group );
    }

    private void updateObject( Object object )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            if ( !JDOHelper.isDetached( object ) )
            {
                throw new ContinuumStoreException( "Not detached: " + object );
            }

            pm.attachCopy( object, true );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public Collection getAllProjectGroupsWithProjects()
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ProjectGroup.class, true );

            Query query = pm.newQuery( extent );

            query.setOrdering( "name ascending" );

            Collection result = (Collection) query.execute();

            result = pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List getAllProjectsByName()
    {
        return getAllObjectsDetached( Project.class, "name ascending", null );
    }

    public List getAllSchedulesByName()
    {
        return getAllObjectsDetached( Schedule.class, "name ascending", null );
    }

    public Schedule addSchedule( Schedule schedule )
    {
        return (Schedule) addObject( schedule );
    }

    public Schedule getScheduleByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Schedule.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = (Collection) query.execute( name );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (Schedule) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Schedule storeSchedule( Schedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );

        return schedule;
    }

    public List getAllProfilesByName()
    {
        return getAllObjectsDetached( Profile.class, "name ascending", null );
    }

    public Profile addProfile( Profile profile )
    {
        return (Profile) addObject( profile );
    }

    public Installation addInstallation( Installation installation )
    {
        return (Installation) addObject( installation );
    }

    public List getAllInstallations()
    {
        return getAllObjectsDetached( Installation.class, "name ascending, version ascending", null );
    }

    public List getAllBuildsForAProjectByDate( int projectId )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Query q = pm.newQuery( "SELECT FROM " + BuildResult.class.getName() +
                " WHERE project.id == :projectId PARAMETERS int projectId ORDER BY endTime DESC" );

            List result = (List) q.execute( new Integer( projectId ) );

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Project getProject( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (Project) getObjectById( Project.class, projectId );
    }

    public void updateProject( Project project )
        throws ContinuumStoreException
    {
        updateObject( project );
    }

    public void updateProfile( Profile profile )
        throws ContinuumStoreException
    {
        updateObject( profile );
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );
    }

    public Project getProjectWithBuilds( int projectId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_WITH_BUILDS_FETCH_GROUP );
    }

    public void removeProfile( Profile profile )
    {
        removeObject( profile );
    }

    public void removeSchedule( Schedule schedule )
    {
        removeObject( schedule );
    }

    public Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP );
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (BuildResult) getObjectById( BuildResult.class, buildId, BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );
    }

    public void removeProject( Project project )
    {
        removeObject( project );
    }

    public void removeProjectGroup( ProjectGroup projectGroup )
    {
        // TODO: why do we need to do this? if not - build results are not removed and a integrity constraint is violated. I assume its because of the fetch groups
        for ( Iterator i = projectGroup.getProjects().iterator(); i.hasNext(); )
        {
            removeProject( (Project) i.next() );
        }
        removeObject( projectGroup );
    }

    public ProjectGroup getProjectGroupWithBuildDetails( int projectGroupId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (ProjectGroup) getObjectById( ProjectGroup.class, projectGroupId, PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public List getAllProjectGroupsWithBuildDetails()
    {
        return getAllObjectsDetached( ProjectGroup.class, "name ascending", PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_ALL_DETAILS_FETCH_GROUP );
    }

    public Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Schedule) getObjectById( Schedule.class, scheduleId );
    }

    public Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Profile) getObjectById( Profile.class, profileId );
    }

    private void removeObject( Object o )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            o = pm.getObjectById( pm.getObjectId( o ) );

            pm.deletePersistent( o );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    private List getAllObjectsDetached( Class clazz )
    {
        return getAllObjectsDetached( clazz, null, null );
    }

    private List getAllObjectsDetached( Class clazz, String fetchGroup )
    {
        return getAllObjectsDetached( clazz, null, fetchGroup );
    }

    private List getAllObjectsDetached( Class clazz, String ordering, String fetchGroup )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( clazz, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( fetchGroup != null )
            {
                pm.getFetchPlan().addGroup( fetchGroup );
            }

            List result = (List) query.execute();

            result = (List) pm.detachCopyAll( result );

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ProjectGroup addProjectGroup( ProjectGroup group )
    {
        return (ProjectGroup) addObject( group );
    }

    private Object addObject( Object object )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.makePersistent( object );

            object = pm.detachCopy( object );

            tx.commit();

            return object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException, ContinuumObjectNotFoundException
    {
        return (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "groupId", groupId, null );
    }

    public Project getProjectWithBuildDetails( int projectId )
        throws ContinuumObjectNotFoundException, ContinuumStoreException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public ProjectGroup getDefaultProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup group;
        try
        {
            group = (ProjectGroup) getObjectFromQuery( ProjectGroup.class, "groupId", DEFAULT_GROUP_ID, null );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            group = new ProjectGroup();
            group.setName( "Default Project Group" );
            group.setGroupId( DEFAULT_GROUP_ID );
            group.setDescription( "Contains all projects that do not have a group of their own" );
            group = addProjectGroup( group );
        }
        return group;
    }

    public SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf )
    {
        return (SystemConfiguration) addObject( systemConf );
    }

    public void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException
    {
        updateObject( systemConf );
    }

    public SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException
    {
        List systemConfs = getAllObjectsDetached( SystemConfiguration.class );

        if ( systemConfs == null || systemConfs.isEmpty() )
        {
            return null;
        }
        else if ( systemConfs.size() > 1 )
        {
            throw new ContinuumStoreException( "Database is corrupted. There are more than one systemConfiguration object." );
        }
        else
        {
            return (SystemConfiguration) systemConfs.get( 0 );
        }
    }

    public ContinuumUser addUser( ContinuumUser user )
    {
        return (ContinuumUser) addObject( user );
    }

    public ContinuumUser getGuestUser()
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumUser.class, true );

            Query query = pm.newQuery( extent );

            query.setFilter( "this.guest == true" );

            Collection result = (Collection) query.execute();

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (ContinuumUser) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumUser getUserByUsername( String username )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumUser.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String username" );

            query.setFilter( "this.username == username" );

            Collection result = (Collection) query.execute( username );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (ContinuumUser) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List getPermissions()
        throws ContinuumStoreException
    {
        return getAllObjectsDetached( Permission.class );
    }

    public Permission getPermission( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( Permission.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = (Collection) query.execute( name );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (Permission) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Permission addPermission( Permission perm )
    {
        return (Permission) addObject( perm );
    }

    public UserGroup addUserGroup( UserGroup group)
    {
        return (UserGroup) addObject( group );
    }

    public void updateUserGroup( UserGroup group )
        throws ContinuumStoreException
    {
        updateObject( group );
    }

    public UserGroup getUserGroup( String name )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( UserGroup.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = (Collection) query.execute( name );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (UserGroup) object;
        }
        finally
        {
            rollback( tx );
        }
    }
}
