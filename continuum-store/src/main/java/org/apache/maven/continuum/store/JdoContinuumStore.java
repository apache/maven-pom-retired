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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import javax.jdo.Extent;
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
 * @todo cleanup old stuff
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

    private static final String PROJECT_DETAIL_FG = "project-detail";

    private static final String BUILD_DETAIL_FG = "build-detail";

    private static final String PROJECT_GROUP_DETAIL_FG = "project-group-detail";

    private static final String SCHEDULE_DETAIL_FG = "schedule-detail";

    private static final String BUILD_SETTINGS_DETAIL_FG = "build-settings-detail";

    private static final String PROJECT_WITH_BUILDS_FETCH_GROUP = "project-with-builds";

    private static final String PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP = "project-with-checkout-result";

    private static final String BUILD_RESULT_WITH_DETAILS_FETCH_GROUP = "build-result-with-details";

    private static final String PROJECT_BUILD_DETAILS_FETCH_GROUP = "project-build-details";

    private static final String PROJECT_ALL_DETAILS_FETCH_GROUP = "project-all-details";

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

    public ContinuumProject addProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        return (ContinuumProject) addObject( project, PROJECT_DETAIL_FG );
    }

    public void removeProject( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( ContinuumProject.class, projectId );

            ContinuumProject project = (ContinuumProject) pm.getObjectById( id );

            if ( project.getProjectGroup() != null )
            {
                ContinuumProjectGroup pg = project.getProjectGroup();

                pg.getProjects().remove( project );
            }

            pm.deletePersistent( project );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProject updateProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            // ----------------------------------------------------------------------
            // Work around for bug with M:N relationships. We must persist the list
            // of schedules or they don't get saved.
            // ----------------------------------------------------------------------

//            if ( project.getSchedules() != null && project.getSchedules().size() > 0 )
//            {
//                pm.attachCopyAll( project.getSchedules(), true );
//            }
//
//            if ( project.getProjectGroup() != null )
//            {
//                pm.attachCopy( project.getProjectGroup(), true );
//            }
//
//            if ( project.getScmResult() != null )
//            {
//                pm.attachCopy( project.getScmResult(), true );
//            }

            pm.attachCopy( project, true );

            tx.commit();

            return (ContinuumProject) getDetailedObject( ContinuumProject.class, project.getId(), PROJECT_DETAIL_FG );
        }
        finally
        {
            rollback( tx );
        }
    }

    public Collection getAllProjects()
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumProject.class, true );

            Query query = pm.newQuery( extent );

            query.setOrdering( "name ascending" );

            Collection result = (Collection) query.execute();

            result = pm.detachCopyAll( result );

            for ( Iterator it = result.iterator(); it.hasNext(); )
            {
                setProjectState( (ContinuumProject) it.next() );
            }

            tx.commit();

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProject getProjectByName( String name )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumProject.class, true );

            Query query = pm.newQuery( extent );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            query.getFetchPlan().addGroup( PROJECT_DETAIL_FG );

            Collection result = (Collection) query.execute( name );

            if ( result.size() == 0 )
            {
                tx.commit();

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            tx.commit();

            return (ContinuumProject) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumProject project = getContinuumProject( pm, projectId, true );

            project = (ContinuumProject) pm.detachCopy( project );

            tx.commit();

            return project;
        }
        catch ( JDOObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( ContinuumProject.class.getName(), projectId );
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProject getProjectForBuild( String buildId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumBuild build = getContinuumBuild( pm, buildId );

            String projectId = build.getProject().getId();

            ContinuumProject project = getContinuumProject( pm, projectId, true );

            tx.commit();

            return project;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumProject project = getContinuumProject( pm, projectId, false );

            ScmResult scmResult = project.getCheckoutResult();

            if ( scmResult == null )
            {
                tx.commit();

                return null;
            }

            scmResult = (ScmResult) pm.detachCopy( scmResult );

            tx.commit();

            return scmResult;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumSchedule addSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException
    {
        return (ContinuumSchedule) addObject( schedule, SCHEDULE_DETAIL_FG );
    }

    public ContinuumSchedule getSchedule( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumSchedule schedule = getContinuumSchedule( pm, projectId, true );

            schedule = (ContinuumSchedule) pm.detachCopy( schedule );

            tx.commit();

            return schedule;
        }
        catch ( JDOObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( ContinuumProject.class.getName(), projectId );
        }
        finally
        {
            rollback( tx );
        }
    }

    public Collection getSchedules()
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumSchedule.class, true );

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

    public ContinuumSchedule updateSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );
        return schedule;
    }

    public ContinuumBuild addBuild( String projectId, ContinuumBuild build )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumProject project = getContinuumProject( pm, projectId, false );

            build.setProject( project );

            build = (ContinuumBuild) makePersistent( pm, build, false );

            project.setLatestBuildId( build.getId() );

            project.setBuildNumber( project.getBuildNumber() + 1 );

            project.getBuilds().add( build );

            tx.commit();

            return (ContinuumBuild) getDetailedObject( ContinuumBuild.class, build.getId(), BUILD_DETAIL_FG );
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumBuild updateBuild( ContinuumBuild build )
        throws ContinuumStoreException
    {
        updateObject( build );
        return build;
    }

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException
    {
        return (ContinuumBuild) getDetailedObject( ContinuumBuild.class, buildId, BUILD_DETAIL_FG );
    }

    public ContinuumBuild getLatestBuildForProject( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumProject project = getContinuumProject( pm, projectId, false );

            String buildId = project.getLatestBuildId();

            if ( buildId == null )
            {
                tx.commit();

                return null;
            }

            Object id = pm.newObjectIdInstance( ContinuumBuild.class, buildId );

            Object object = pm.getObjectById( id );

            ContinuumBuild build = (ContinuumBuild) pm.detachCopy( object );

            tx.commit();

            return build;
        }
        finally
        {
            rollback( tx );
        }
    }

    public Collection getBuildsForProject( String projectId, int start, int end )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumBuild.class, true );

            Query query = pm.newQuery( extent );

            query.setFilter( "this.project.id == id" );

            query.declareImports( "import java.lang.String" );

            query.declareParameters( "String id" );

            query.setOrdering( "startTime descending" );

            Collection builds = (Collection) query.execute( projectId );

            builds = pm.detachCopyAll( builds );

            tx.commit();

            return builds;
        }
        finally
        {
            rollback( tx );
        }
    }

    public List getChangedFilesForBuild( String buildId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumBuild build = getContinuumBuild( pm, buildId );

            ScmResult scmResult = build.getScmResult();

            if ( scmResult == null )
            {
                tx.commit();

                return null;
            }

            List files = (List) pm.detachCopyAll( scmResult.getChanges() );

            tx.commit();

            return files;
        }
        finally
        {
            rollback( tx );
        }
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

    public ContinuumProjectGroup addProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) addObject( projectGroup, PROJECT_GROUP_DETAIL_FG );
    }

    public ContinuumProjectGroup updateProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException
    {
        updateObject( projectGroup );
        return projectGroup;
    }

    public Collection getProjectGroups()
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumProjectGroup.class, true );

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

    public void removeProjectGroup( String projectGroupId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( ContinuumProjectGroup.class, projectGroupId );

            ContinuumProjectGroup projectGroup = (ContinuumProjectGroup) pm.getObjectById( id );

            pm.deletePersistent( projectGroup );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProjectGroup getProjectGroup( String projectGroupId )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) getDetailedObject( ContinuumProjectGroup.class, projectGroupId,
                                                          PROJECT_GROUP_DETAIL_FG );
    }

    public ContinuumProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) getObjectFromQuery( ContinuumProjectGroup.class, "groupId", groupId,
                                                           PROJECT_GROUP_DETAIL_FG );

    }

    public ContinuumBuildSettings addBuildSettings( ContinuumBuildSettings buildSettings )
        throws ContinuumStoreException
    {
        return (ContinuumBuildSettings) addObject( buildSettings, BUILD_SETTINGS_DETAIL_FG );
    }

    public ContinuumBuildSettings updateBuildSettings( ContinuumBuildSettings buildSettings )
        throws ContinuumStoreException
    {
        updateObject( buildSettings );
        return buildSettings;
    }

    public void removeBuildSettings( String buildSettingsId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( ContinuumBuildSettings.class, buildSettingsId );

            ContinuumBuildSettings buildSettings = (ContinuumBuildSettings) pm.getObjectById( id );

            pm.deletePersistent( buildSettings );

            tx.commit();
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumBuildSettings getBuildSettings( String buildSettingsId )
        throws ContinuumStoreException
    {
        return (ContinuumBuildSettings) getDetailedObject( ContinuumBuildSettings.class, buildSettingsId,
                                                           "build-settings-detail" );
    }

    public Collection getBuildSettings()
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumBuildSettings.class, true );

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

    private ContinuumProject setProjectState( ContinuumProject project )
        throws ContinuumStoreException
    {
        ContinuumBuild build = getLatestBuildForProject( project.getId() );

        if ( build == null )
        {
            project.setState( ContinuumProjectState.NEW );
        }
        else
        {
            project.setState( build.getState() );
        }

        return project;
    }

    private ContinuumProject getContinuumProject( PersistenceManager pm, String projectId, boolean details )
    {
        if ( details )
        {
            pm.getFetchPlan().addGroup( "project-detail" );
        }

        Object id = pm.newObjectIdInstance( ContinuumProject.class, projectId );

        return (ContinuumProject) pm.getObjectById( id );
    }

    private ContinuumBuild getContinuumBuild( PersistenceManager pm, String buildId )
    {
        Object id = pm.newObjectIdInstance( ContinuumBuild.class, buildId );

        return (ContinuumBuild) pm.getObjectById( id );
    }

    private ContinuumSchedule getContinuumSchedule( PersistenceManager pm, String projectId, boolean details )
    {
        if ( details )
        {
            pm.getFetchPlan().addGroup( "schedule-detail" );
        }

        Object id = pm.newObjectIdInstance( ContinuumSchedule.class, projectId );

        return (ContinuumSchedule) pm.getObjectById( id );
    }

    private Object addObject( Object object, String detailedFetchGroup )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            // ----------------------------------------------------------------------
            // Store the object
            // ----------------------------------------------------------------------

            pm.makePersistent( object );

            Object id = pm.getObjectId( object );

            // ----------------------------------------------------------------------
            // Fetch the object again and return it
            // ----------------------------------------------------------------------

            pm.getFetchPlan().addGroup( detailedFetchGroup );

            Object addedObject = pm.getObjectById( id );

            addedObject = pm.detachCopy( addedObject );

            tx.commit();

            return addedObject;
        }
        finally
        {
            rollback( tx );
        }
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

    private Object getDetailedObject( Class clazz, String id, String fetchGroup )
        throws ContinuumObjectNotFoundException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.getFetchPlan().addGroup( fetchGroup );

            Object objectId = pm.newObjectIdInstance( clazz, id );

            Object object = pm.getObjectById( objectId );

            object = pm.detachCopy( object );

            tx.commit();

            return object;
        }
        catch ( JDOObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( clazz.getName(), id );
        }
        finally
        {
            rollback( tx );
        }
    }

    private Object getObjectFromQuery( Class clazz, String idField, String id, String fetchGroup )
        throws ContinuumStoreException
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

    // TODO ^^^^ REMOVE ^^^^

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
        throws ContinuumObjectNotFoundException
    {
        return (ProjectGroup) getObjectById( ProjectGroup.class, projectGroupId );
    }

    private Object getObjectById( Class clazz, int id )
        throws ContinuumObjectNotFoundException
    {
        return getObjectById( clazz, id, null );
    }

    private Object getObjectById( Class clazz, int id, String fetchGroup )
        throws ContinuumObjectNotFoundException
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
        throws ContinuumObjectNotFoundException
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
        throws ContinuumObjectNotFoundException
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
        throws ContinuumObjectNotFoundException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_WITH_CHECKOUT_RESULT_FETCH_GROUP );
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumObjectNotFoundException
    {
        return (BuildResult) getObjectById( BuildResult.class, buildId, BUILD_RESULT_WITH_DETAILS_FETCH_GROUP );
    }

    public void removeProject( Project project )
    {
        removeObject( project );
    }

    public void removeProjectGroup( ProjectGroup projectGroup )
    {
        removeObject( projectGroup );
    }

    public ProjectGroup getProjectGroupWithBuildDetails( int projectGroupId )
        throws ContinuumObjectNotFoundException
    {
        return (ProjectGroup) getObjectById( ProjectGroup.class, projectGroupId, PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public List getAllProjectGroupsWithBuildDetails()
    {
        return getAllObjectsDetached( ProjectGroup.class, "name ascending", PROJECT_BUILD_DETAILS_FETCH_GROUP );
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumObjectNotFoundException
    {
        return (Project) getObjectById( Project.class, projectId, PROJECT_ALL_DETAILS_FETCH_GROUP );
    }

    public Schedule getSchedule( int scheduleId )
        throws ContinuumObjectNotFoundException
    {
        return (Schedule) getObjectById( Schedule.class, scheduleId );
    }

    public Profile getProfile( int profileId )
        throws ContinuumObjectNotFoundException
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

    private List getAllObjectsDetached( Class clazz, String ordering, String fetchGroup )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( clazz, true );

            Query query = pm.newQuery( extent );

            query.setOrdering( ordering );

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

}
