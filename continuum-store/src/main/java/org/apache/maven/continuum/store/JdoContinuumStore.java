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

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildGroup;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.scm.ScmResult;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.jdo.Extent;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
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

    private static final String PROJECT_DETAIL_FG = "project-detail";

    private static final String BUILD_DETAIL_FG = "build-detail";

    private static final String NOTIFIER_DETAIL_FG = "notifier-detail";

    private static final String BUILD_GROUP_DETAIL_FG = "build-group-detail";

    private static final String PROJECT_GROUP_DETAIL_FG = "project-group-detail";

    private static final String SCHEDULE_DETAIL_FG = "schedule-detail";

    private static final String BUILD_SETTINGS_DETAIL_FG = "build-settings-detail";

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

            // ----------------------------------------------------------------------
            // We need to remove this project reference from any schedule in the
            // system. So grab the list of schedules this project belongs to
            // then iterate through the collection of schedules removing the
            // reference to this project. This seems like a bit much but the
            // only thing that works.
            // ----------------------------------------------------------------------

            if ( project.getSchedules() != null && project.getSchedules().size() > 0 )
            {
                Set schedules = project.getSchedules();

                for ( Iterator i = schedules.iterator(); i.hasNext(); )
                {
                    ContinuumSchedule schedule = (ContinuumSchedule) i.next();

                    schedule.getProjects().remove( project );
                }
            }

            if ( project.getBuildGroups() != null && project.getBuildGroups().size() > 0 )
            {
                Set buildGroups = project.getBuildGroups();

                for ( Iterator i = buildGroups.iterator(); i.hasNext(); )
                {
                    ContinuumBuildGroup buildGroup = (ContinuumBuildGroup) i.next();

                    buildGroup.getProjects().remove( project );
                }
            }

            if ( project.getProjectGroup() != null )
            {
                ContinuumProjectGroup pg = project.getProjectGroup();

                pg.getProjects().remove( project );
            }

            pm.deletePersistent( project );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProject updateProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        String checkoutErrorMessage = project.getCheckOutErrorMessage();

        String checkoutErrorException = project.getCheckOutErrorException();

        if ( checkoutErrorMessage != null && checkoutErrorMessage.length() > 255 )
        {
            project.setCheckOutErrorMessage( checkoutErrorMessage.substring( 0, 255 ) );
        }

        if ( checkoutErrorException != null && checkoutErrorException.length() > 255 )
        {
            project.setCheckOutErrorException( checkoutErrorException.substring( 0, 255 ) );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

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

//            if ( project.getBuildGroups() != null && project.getBuildGroups().size() > 0 )
//            {
//                pm.attachCopyAll( project.getBuildGroups(), true );
//            }

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

            commit( tx );

            return (ContinuumProject) getDetailedObject( ContinuumProject.class,
                                                         project.getId(),
                                                         PROJECT_DETAIL_FG );
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

            commit( tx );

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
                commit( tx );

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            commit( tx );

            return (ContinuumProject) object;
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProject getProjectByScmUrl( String scmUrl )
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

            query.declareParameters( "String scmUrl" );

            query.setFilter( "this.scmUrl == scmUrl" );

            query.getFetchPlan().addGroup( PROJECT_DETAIL_FG );

            Collection result = (Collection) query.execute( scmUrl );

            if ( result.size() == 0 )
            {
                commit( tx );

                return null;
            }

            Object object = pm.detachCopy( result.iterator().next() );

            commit( tx );

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

            commit( tx );

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

            commit( tx );

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

            ScmResult scmResult = project.getScmResult();

            if ( scmResult == null )
            {
                commit( tx );

                return null;
            }

            scmResult = (ScmResult) pm.detachCopy( scmResult );

            commit( tx );

            return scmResult;
        }
        finally
        {
            rollback( tx );
        }
    }

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

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

            commit( tx );

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

            commit( tx );

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
        return (ContinuumSchedule) updateObject( schedule, SCHEDULE_DETAIL_FG );
    }

    public void removeSchedule( String scheduleId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( ContinuumSchedule.class, scheduleId );

            ContinuumSchedule schedule = (ContinuumSchedule) pm.getObjectById( id );

            // ----------------------------------------------------------------------
            // We need to remove this schedule reference from any project in the
            // system. So grab the list of projects this schedule belongs to
            // then iterate through the collection of projects removing the
            // reference to this schedule. This seems like a bit much but the
            // only thing that works.
            // ----------------------------------------------------------------------

            if ( schedule.getProjects() != null && schedule.getProjects().size() > 0 )
            {
                Set projects = schedule.getProjects();

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    ContinuumProject project = (ContinuumProject) i.next();

                    project.getSchedules().remove( schedule );
                }
            }

            pm.deletePersistent( schedule );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    // ----------------------------------------------------------------------
    // Builds
    // ----------------------------------------------------------------------

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

            commit( tx );

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
        return (ContinuumBuild) updateObject( build, BUILD_DETAIL_FG );
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
                commit( tx );

                return null;
            }

            Object id = pm.newObjectIdInstance( ContinuumBuild.class, buildId );

            Object object = pm.getObjectById( id );

            ContinuumBuild build = (ContinuumBuild) pm.detachCopy( object );

            commit( tx );

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

            commit( tx );

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
                commit( tx );

                return null;
            }

            List files = (List) pm.detachCopyAll( scmResult.getFiles() );

            commit( tx );

            return files;
        }
        finally
        {
            rollback( tx );
        }
    }

    public void removeNotifier( ContinuumNotifier notifier )
        throws ContinuumStoreException
    {
        attachAndDelete( notifier );
    }

    public ContinuumNotifier storeNotifier( ContinuumNotifier notifier )
        throws ContinuumStoreException
    {
        return (ContinuumNotifier) updateObject( notifier, NOTIFIER_DETAIL_FG );
    }

    // ----------------------------------------------------------------------
    // Project Groups
    // ----------------------------------------------------------------------

    public ContinuumProjectGroup addProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) addObject( projectGroup, PROJECT_GROUP_DETAIL_FG );
    }

    public ContinuumProjectGroup updateProjectGroup( ContinuumProjectGroup projectGroup )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) updateObject( projectGroup, PROJECT_GROUP_DETAIL_FG );
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

            commit( tx );

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

            // ----------------------------------------------------------------------
            // We need to remove this projectGroup reference from any project in the
            // system. So grab the list of projects this projectGroup belongs to
            // then iterate through the collection of projects removing the
            // reference to this projectGroup. This seems like a bit much but the
            // only thing that works.
            // ----------------------------------------------------------------------

            /*
            if ( projectGroup.getProjects() != null && projectGroup.getProjects().size() > 0 )
            {
                Set projects = projectGroup.getProjects();

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    ContinuumProject project = (ContinuumProject) i.next();

                    project.getBuildGroups().remove( projectGroup );
                }
            }
            */

            pm.deletePersistent( projectGroup );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumProjectGroup getProjectGroup( String projectGroupId )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) getDetailedObject( ContinuumProjectGroup.class,
                                                          projectGroupId,
                                                          PROJECT_GROUP_DETAIL_FG );
    }

    public ContinuumProjectGroup getProjectGroupByName( String name )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) getObjectFromQuery( ContinuumProjectGroup.class,
                                                           "name",
                                                           name,
                                                           PROJECT_GROUP_DETAIL_FG );
    }

    public ContinuumProjectGroup getProjectGroupByGroupId( String groupId )
        throws ContinuumStoreException
    {
        return (ContinuumProjectGroup) getObjectFromQuery( ContinuumProjectGroup.class,
                                                           "groupId",
                                                           groupId,
                                                           PROJECT_GROUP_DETAIL_FG );

    }

    // ----------------------------------------------------------------------
    // Build Group
    // ----------------------------------------------------------------------

    public ContinuumBuildGroup addBuildGroup( ContinuumBuildGroup buildGroup )
        throws ContinuumStoreException
    {
        return (ContinuumBuildGroup) addObject( buildGroup, BUILD_GROUP_DETAIL_FG );
    }

    public ContinuumBuildGroup updateBuildGroup( ContinuumBuildGroup schedule )
        throws ContinuumStoreException
    {
        return (ContinuumBuildGroup) updateObject( schedule, BUILD_GROUP_DETAIL_FG );
    }

    public void removeBuildGroup( String buildGroupId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( ContinuumBuildGroup.class, buildGroupId );

            ContinuumBuildGroup buildGroup = (ContinuumBuildGroup) pm.getObjectById( id );

            // ----------------------------------------------------------------------
            // We need to remove this buildGroup reference from any project in the
            // system. So grab the list of projects this buildGroup belongs to
            // then iterate through the collection of projects removing the
            // reference to this buildGroup. This seems like a bit much but the
            // only thing that works.
            // ----------------------------------------------------------------------

            if ( buildGroup.getProjects() != null && buildGroup.getProjects().size() > 0 )
            {
                Set projects = buildGroup.getProjects();

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    ContinuumProject project = (ContinuumProject) i.next();

                    project.getBuildGroups().remove( buildGroup );
                }
            }

            pm.deletePersistent( buildGroup );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }


    public ContinuumBuildGroup getBuildGroup( String buildGroupId )
        throws ContinuumStoreException
    {
        return (ContinuumBuildGroup) getDetailedObject( ContinuumBuildGroup.class,
                                                        buildGroupId,
                                                        "build-group-detail" );
    }

    public Collection getBuildGroups()
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Extent extent = pm.getExtent( ContinuumBuildGroup.class, true );

            Query query = pm.newQuery( extent );

            query.setOrdering( "name ascending" );

            Collection result = (Collection) query.execute();

            result = pm.detachCopyAll( result );

            commit( tx );

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    // ----------------------------------------------------------------------
    // Build Settings
    // ----------------------------------------------------------------------

    public ContinuumBuildSettings addBuildSettings( ContinuumBuildSettings buildSettings )
        throws ContinuumStoreException
    {
        return (ContinuumBuildSettings) addObject( buildSettings, BUILD_SETTINGS_DETAIL_FG );
    }

    public ContinuumBuildSettings updateBuildSettings( ContinuumBuildSettings buildSettings )
        throws ContinuumStoreException
    {
        return (ContinuumBuildSettings) updateObject( buildSettings, BUILD_SETTINGS_DETAIL_FG );
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

            // remove references of this buildSettings object in the build groups
            if ( buildSettings.getBuildGroups() != null && buildSettings.getBuildGroups().size() > 0 )
            {
                Set projects = buildSettings.getBuildGroups();

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    ContinuumBuildGroup buildGroup = (ContinuumBuildGroup) i.next();

                    buildGroup.getBuildSettings().remove( buildSettings );
                }
            }

            // remove references of this buildSettings object in the project groups
            if ( buildSettings.getProjectGroups() != null && buildSettings.getProjectGroups().size() > 0 )
            {
                Set projects = buildSettings.getProjectGroups();

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    ContinuumProjectGroup project = (ContinuumProjectGroup) i.next();

                    project.getBuildSettings().remove( buildSettings );
                }
            }

            pm.deletePersistent( buildSettings );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    public ContinuumBuildSettings getBuildSettings( String buildSettingsId )
        throws ContinuumStoreException
    {
        return (ContinuumBuildSettings) getDetailedObject( ContinuumBuildSettings.class,
                                                           buildSettingsId,
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

            commit( tx );

            return result;
        }
        finally
        {
            rollback( tx );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ContinuumProject getContinuumProject( PersistenceManager pm,
                                                  String projectId,
                                                  boolean details )
    {
        if ( details )
        {
            pm.getFetchPlan().addGroup( "project-detail" );
        }

        Object id = pm.newObjectIdInstance( ContinuumProject.class, projectId );

        return (ContinuumProject) pm.getObjectById( id );
    }

    private ContinuumBuild getContinuumBuild( PersistenceManager pm,
                                              String buildId )
    {
        Object id = pm.newObjectIdInstance( ContinuumBuild.class, buildId );

        return (ContinuumBuild) pm.getObjectById( id );
    }

    private ContinuumSchedule getContinuumSchedule( PersistenceManager pm,
                                                    String projectId,
                                                    boolean details )
    {
        if ( details )
        {
            pm.getFetchPlan().addGroup( "schedule-detail" );
        }

        Object id = pm.newObjectIdInstance( ContinuumSchedule.class, projectId );

        return (ContinuumSchedule) pm.getObjectById( id );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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

            commit( tx );

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

            commit( tx );

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

    private Object getObjectFromQuery( Class clazz,
                                       String idField,
                                       String id,
                                       String fetchGroup )
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
                throw new ContinuumStoreException( "A query for object of " +
                                                   "type " + clazz.getName() + " on the " +
                                                   "field '" + idField + "' returned more than one object." );
            }

            pm.getFetchPlan().addGroup( fetchGroup );

            Object object = pm.detachCopy( result.iterator().next() );

            commit( tx );

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

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    private Object updateObject( Object object, String detailedFetchGroup )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.attachCopy( object, true );

            commit( tx );

            tx.begin();

            tx = pm.currentTransaction();

            Object id = pm.getObjectId( object );

            pm.getFetchPlan().addGroup( detailedFetchGroup );

            object = pm.getObjectById( id, true );

            object = pm.detachCopy( object );

            commit( tx );

            return object;
        }
        finally
        {
            rollback( tx );
        }
    }

    // ----------------------------------------------------------------------
    // Transaction Management
    // ----------------------------------------------------------------------

    private void commit( Transaction tx )
    {
        tx.commit();
    }

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
}
