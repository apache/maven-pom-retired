package org.apache.maven.continuum.store;

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.scm.ScmResult;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import javax.jdo.Extent;
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
 * @version $Id$
 */
public class JdoContinuumStore
    extends AbstractLogEnabled
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
        deletePersistentById( ContinuumProject.class, projectId );
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
            // Work around for bug with M:N relationships
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

        //updateObject( project );
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

            ContinuumProject project = getContinuumProject( pm, projectId );

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

    public ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumProject project = getContinuumProject( pm, projectId );

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

    public ContinuumSchedule getSchedule( String projectId )
        throws ContinuumStoreException
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try
        {
            tx.begin();

            ContinuumSchedule schedule = getContinuumSchedule( pm, projectId );

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

    private ContinuumSchedule getContinuumSchedule( PersistenceManager pm, String projectId )
    {
        pm.getFetchPlan().addGroup( "schedule-detail" );

        Object id = pm.newObjectIdInstance( ContinuumSchedule.class, projectId );

        return (ContinuumSchedule) pm.getObjectById( id );
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

            ContinuumProject project = getContinuumProject( pm, projectId );

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

            ContinuumProject project = getContinuumProject( pm, projectId );

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

            ContinuumBuild build  = getContinuumBuild( pm, buildId );

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

    private ContinuumProject getContinuumProject( PersistenceManager pm, String projectId )
    {
        pm.getFetchPlan().addGroup( "project-detail" );

        Object id = pm.newObjectIdInstance( ContinuumProject.class, projectId );

        ContinuumProject project = (ContinuumProject) pm.getObjectById( id );

        return project;
    }

    private ContinuumBuild getContinuumBuild( PersistenceManager pm, String buildId )
    {
        Object id = pm.newObjectIdInstance( ContinuumBuild.class, buildId );

        ContinuumBuild build = (ContinuumBuild) pm.getObjectById( id );

        return build;
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
