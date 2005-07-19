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

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.scm.ScmResult;

import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

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

    public String addProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object object = makePersistent( pm, project );

            project = (ContinuumProject) object;

            commit( tx );

            return project.getId();
        }
        finally
        {
            rollback( tx );
        }
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

            pm.deletePersistent( project );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    public void updateProject( ContinuumProject project )
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

            if ( project.getSchedules() != null && project.getSchedules().size() > 0 )
            {
                makePersistentAll( pm, project.getSchedules() );
            }

            pm.attachCopy( project, true );

            commit( tx );
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

            query.declareParameters( "String name" );

            query.setFilter( "this.name == name" );

            Collection result = ( (Collection) query.execute( name ) );

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

            query.declareParameters( "String scmUrl" );

            query.setFilter( "this.scmUrl == scmUrl" );

            Collection result = ( (Collection) query.execute( scmUrl ) );

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
        catch( JDOObjectNotFoundException e )
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

    public String addSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            schedule = (ContinuumSchedule) makePersistent( pm, schedule );

            commit( tx );

            return schedule.getId();
        }
        finally
        {
            rollback( tx );
        }
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
        catch( JDOObjectNotFoundException e )
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

    public void updateSchedule( ContinuumSchedule schedule )
        throws ContinuumStoreException
    {
        updateObject( schedule );
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

    public String addBuild( String projectId, ContinuumBuild build )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumProject project = getContinuumProject( pm, projectId, false );

            build.setProject( project );

            build = (ContinuumBuild) makePersistent( pm, build );

            project.setLatestBuildId( build.getId() );

            project.setBuildNumber( project.getBuildNumber() + 1 );

            project.getBuilds().add( build );

            commit( tx );

            return build.getId();
        }
        finally
        {
            rollback( tx );
        }
    }

    public void updateBuild( ContinuumBuild build )
        throws ContinuumStoreException
    {
        updateObject( build );
    }

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumBuild build = getContinuumBuild( pm, buildId );

            build = (ContinuumBuild) pm.detachCopy( build );

            commit( tx );

            return build;
        }
        catch ( JDOObjectNotFoundException e )
        {
            throw new ContinuumObjectNotFoundException( ContinuumBuild.class.getName(), buildId );
        }
        finally
        {
            rollback( tx );
        }
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

    public void removeNotifier( Object notifier )
        throws ContinuumStoreException
    {
        attachAndDelete( notifier );
    }

    public void storeNotifier( Object notifier )
        throws ContinuumStoreException
    {
        updateObject( notifier );
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

        ContinuumProject project = (ContinuumProject) pm.getObjectById( id );

        return project;
    }

    private ContinuumBuild getContinuumBuild( PersistenceManager pm,
                                              String buildId )
    {
        Object id = pm.newObjectIdInstance( ContinuumBuild.class, buildId );

        ContinuumBuild build = (ContinuumBuild) pm.getObjectById( id );

        return build;
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

    private Object makePersistent( PersistenceManager pm, Object object )
    {
        pm.makePersistent( object );

        Object id = pm.getObjectId( object );

        return pm.getObjectById( id );
    }

    private void makePersistentAll( PersistenceManager pm, Collection object )
    {
        pm.makePersistentAll( object );
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

    private void deletePersistentById( Class clazz, Object identifier )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            Object id = pm.newObjectIdInstance( clazz, identifier );

            Object object = pm.getObjectById( id );

            pm.deletePersistent( object );

            commit( tx );
        }
        finally
        {
            rollback( tx );
        }
    }

    private void updateObject( Object object )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            pm.attachCopy( object, true );

            commit( tx );
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
