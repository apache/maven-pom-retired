package org.apache.maven.continuum.project.v1_0_alpha_3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOUserException;
import javax.jdo.Transaction;

// Model class imports
import org.apache.maven.continuum.scm.v1_0_alpha_3.CheckOutScmResult;
import org.apache.maven.continuum.scm.v1_0_alpha_3.UpdateScmResult;

/**
 * Generated JPox storage mechanism for Continuum.
 *
 * @author Mr Modello
 */
public class ContinuumJPoxStore
{
        public final static String ContinuumProject_DETAIL_FETCH_GROUP = "ContinuumProject_detail";
        public final static String ContinuumNotifier_DETAIL_FETCH_GROUP = "ContinuumNotifier_detail";
        public final static String ContinuumDeveloper_DETAIL_FETCH_GROUP = "ContinuumDeveloper_detail";
        public final static String MavenTwoProject_DETAIL_FETCH_GROUP = "MavenTwoProject_detail";
        public final static String MavenOneProject_DETAIL_FETCH_GROUP = "MavenOneProject_detail";
        public final static String AntProject_DETAIL_FETCH_GROUP = "AntProject_detail";
        public final static String ShellProject_DETAIL_FETCH_GROUP = "ShellProject_detail";
        public final static String ContinuumBuild_DETAIL_FETCH_GROUP = "ContinuumBuild_detail";
        public final static String CheckOutScmResult_DETAIL_FETCH_GROUP = "CheckOutScmResult_detail";
        public final static String UpdateScmResult_DETAIL_FETCH_GROUP = "UpdateScmResult_detail";
    private static ThreadLocal threadState = new ThreadLocal();

    private PersistenceManagerFactory pmf;

    public ContinuumJPoxStore( PersistenceManagerFactory pmf )
    {
        this.pmf = pmf;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static class ThreadState
    {
        private PersistenceManager pm;

        private Transaction tx;

        private int depth;

        public PersistenceManager getPersistenceManager()
        {
            return pm;
        }

        public Transaction getTransaction()
        {
            return tx;
        }

        public int getDepth()
        {
            return depth;
        }
    }

    // ----------------------------------------------------------------------
    // Transaction Management Methods
    // ----------------------------------------------------------------------

    public ThreadState getThreadState()
    {
        return (ThreadState) threadState.get();
    }

    public PersistenceManager begin()
        throws Exception
    {
        ThreadState state = (ThreadState) threadState.get();

        if ( state == null )
        {
            state = new ThreadState();

            state.pm = pmf.getPersistenceManager();

            state.tx = state.pm.currentTransaction();

            state.tx.begin();

            threadState.set( state );

            return state.pm;
        }
        else
        {
            state.depth++;

            return state.pm;
        }
    }

    public void commit()
        throws Exception
    {
        ThreadState state = (ThreadState) threadState.get();

        if ( state == null )
        {
            throw new Exception( "commit() must only be called after begin()." );
        }

        if ( state.depth > 0 )
        {
            state.depth--;

            return;
        }

        threadState.set( null );

        try
        {
            state.tx.commit();
        }
        catch( Exception ex )
        {
            if ( state.tx.isActive() )
            {
                state.tx.rollback();
            }

            throw ex;
        }
        finally
        {
            closePersistenceManager( state.pm );
        }
    }

    public void rollback()
        throws Exception
    {
        ThreadState state = (ThreadState) threadState.get();

        if ( state == null )
        {
            // The tx is not active because it has already been committed or rolled back

            return;
        }

        threadState.set( null );

        try
        {
            if ( state.tx.isActive() )
            {
                state.tx.rollback();
            }
        }
        finally
        {
            closePersistenceManager( state.pm );
        }
    }

    // ----------------------------------------------------------------------
    // ContinuumProject CRUD
    // ----------------------------------------------------------------------

    public Object addContinuumProject( ContinuumProject o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Object storeContinuumProject( ContinuumProject o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Object id;

            // New instance so store it
            if ( o.getId() == null )
            {
                pm.makePersistent( o );

                id = pm.getObjectId( o );
            }
            // exists, so update it
            else
            {
                pm.attachCopy( o, true );

                id = o.getId();
            }

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public void deleteContinuumProject( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( ContinuumProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumProject\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumProject getContinuumProject( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumProject_detail" );

            Query query = pm.newQuery( ContinuumProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumProject\" with id: \"" + id + "\"." );
            }

            ContinuumProject object = (ContinuumProject) result.iterator().next();

            if ( detach )
            {
                object = (ContinuumProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumProject getContinuumProjectByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumProject_detail" );

            ContinuumProject object = (ContinuumProject) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (ContinuumProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getContinuumProjectCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( ContinuumProject.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // ContinuumNotifier CRUD
    // ----------------------------------------------------------------------

    public Object addContinuumNotifier( ContinuumNotifier o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteContinuumNotifier( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( ContinuumNotifier.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumNotifier\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumNotifier getContinuumNotifier( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumNotifier_detail" );

            Query query = pm.newQuery( ContinuumNotifier.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumNotifier\" with id: \"" + id + "\"." );
            }

            ContinuumNotifier object = (ContinuumNotifier) result.iterator().next();

            if ( detach )
            {
                object = (ContinuumNotifier) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumNotifier getContinuumNotifierByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumNotifier_detail" );

            ContinuumNotifier object = (ContinuumNotifier) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (ContinuumNotifier) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getContinuumNotifierCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( ContinuumNotifier.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // ContinuumDeveloper CRUD
    // ----------------------------------------------------------------------

    public Object addContinuumDeveloper( ContinuumDeveloper o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Object storeContinuumDeveloper( ContinuumDeveloper o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Object id;

            // New instance so store it
            if ( o.getId() == null )
            {
                pm.makePersistent( o );

                id = pm.getObjectId( o );
            }
            // exists, so update it
            else
            {
                pm.attachCopy( o, true );

                id = o.getId();
            }

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public void deleteContinuumDeveloper( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( ContinuumDeveloper.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumDeveloper\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumDeveloper getContinuumDeveloper( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumDeveloper_detail" );

            Query query = pm.newQuery( ContinuumDeveloper.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumDeveloper\" with id: \"" + id + "\"." );
            }

            ContinuumDeveloper object = (ContinuumDeveloper) result.iterator().next();

            if ( detach )
            {
                object = (ContinuumDeveloper) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumDeveloper getContinuumDeveloperByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumDeveloper_detail" );

            ContinuumDeveloper object = (ContinuumDeveloper) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (ContinuumDeveloper) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getContinuumDeveloperCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( ContinuumDeveloper.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // MavenTwoProject CRUD
    // ----------------------------------------------------------------------

    public Object addMavenTwoProject( MavenTwoProject o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteMavenTwoProject( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( MavenTwoProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"MavenTwoProject\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public MavenTwoProject getMavenTwoProject( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "MavenTwoProject_detail" );

            Query query = pm.newQuery( MavenTwoProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"MavenTwoProject\" with id: \"" + id + "\"." );
            }

            MavenTwoProject object = (MavenTwoProject) result.iterator().next();

            if ( detach )
            {
                object = (MavenTwoProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public MavenTwoProject getMavenTwoProjectByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "MavenTwoProject_detail" );

            MavenTwoProject object = (MavenTwoProject) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (MavenTwoProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getMavenTwoProjectCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( MavenTwoProject.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // MavenOneProject CRUD
    // ----------------------------------------------------------------------

    public Object addMavenOneProject( MavenOneProject o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteMavenOneProject( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( MavenOneProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"MavenOneProject\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public MavenOneProject getMavenOneProject( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "MavenOneProject_detail" );

            Query query = pm.newQuery( MavenOneProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"MavenOneProject\" with id: \"" + id + "\"." );
            }

            MavenOneProject object = (MavenOneProject) result.iterator().next();

            if ( detach )
            {
                object = (MavenOneProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public MavenOneProject getMavenOneProjectByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "MavenOneProject_detail" );

            MavenOneProject object = (MavenOneProject) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (MavenOneProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getMavenOneProjectCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( MavenOneProject.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // AntProject CRUD
    // ----------------------------------------------------------------------

    public Object addAntProject( AntProject o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteAntProject( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( AntProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"AntProject\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public AntProject getAntProject( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "AntProject_detail" );

            Query query = pm.newQuery( AntProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"AntProject\" with id: \"" + id + "\"." );
            }

            AntProject object = (AntProject) result.iterator().next();

            if ( detach )
            {
                object = (AntProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public AntProject getAntProjectByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "AntProject_detail" );

            AntProject object = (AntProject) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (AntProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getAntProjectCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( AntProject.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // ShellProject CRUD
    // ----------------------------------------------------------------------

    public Object addShellProject( ShellProject o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteShellProject( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( ShellProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ShellProject\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ShellProject getShellProject( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ShellProject_detail" );

            Query query = pm.newQuery( ShellProject.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ShellProject\" with id: \"" + id + "\"." );
            }

            ShellProject object = (ShellProject) result.iterator().next();

            if ( detach )
            {
                object = (ShellProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ShellProject getShellProjectByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ShellProject_detail" );

            ShellProject object = (ShellProject) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (ShellProject) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getShellProjectCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( ShellProject.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // ContinuumBuild CRUD
    // ----------------------------------------------------------------------

    public Object addContinuumBuild( ContinuumBuild o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Object storeContinuumBuild( ContinuumBuild o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Object id;

            // New instance so store it
            if ( o.getId() == null )
            {
                pm.makePersistent( o );

                id = pm.getObjectId( o );
            }
            // exists, so update it
            else
            {
                pm.attachCopy( o, true );

                id = o.getId();
            }

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public void deleteContinuumBuild( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( ContinuumBuild.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumBuild\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumBuild getContinuumBuild( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumBuild_detail" );

            Query query = pm.newQuery( ContinuumBuild.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"ContinuumBuild\" with id: \"" + id + "\"." );
            }

            ContinuumBuild object = (ContinuumBuild) result.iterator().next();

            if ( detach )
            {
                object = (ContinuumBuild) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public ContinuumBuild getContinuumBuildByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "ContinuumBuild_detail" );

            ContinuumBuild object = (ContinuumBuild) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (ContinuumBuild) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getContinuumBuildCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( ContinuumBuild.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }



    // ----------------------------------------------------------------------
    // CheckOutScmResult CRUD
    // ----------------------------------------------------------------------

    public Object addCheckOutScmResult( CheckOutScmResult o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteCheckOutScmResult( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( CheckOutScmResult.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"CheckOutScmResult\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public CheckOutScmResult getCheckOutScmResult( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "CheckOutScmResult_detail" );

            Query query = pm.newQuery( CheckOutScmResult.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"CheckOutScmResult\" with id: \"" + id + "\"." );
            }

            CheckOutScmResult object = (CheckOutScmResult) result.iterator().next();

            if ( detach )
            {
                object = (CheckOutScmResult) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public CheckOutScmResult getCheckOutScmResultByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "CheckOutScmResult_detail" );

            CheckOutScmResult object = (CheckOutScmResult) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (CheckOutScmResult) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getCheckOutScmResultCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( CheckOutScmResult.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    // ----------------------------------------------------------------------
    // UpdateScmResult CRUD
    // ----------------------------------------------------------------------

    public Object addUpdateScmResult( UpdateScmResult o )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.makePersistent( o );

            Object id = pm.getObjectId( o );

            commit();

            return id;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    public void deleteUpdateScmResult( String id )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Query query = pm.newQuery( UpdateScmResult.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"UpdateScmResult\" with id: \"" + id + "\"." );
            }

            pm.deletePersistentAll( result );

            commit();
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public UpdateScmResult getUpdateScmResult( String id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "UpdateScmResult_detail" );

            Query query = pm.newQuery( UpdateScmResult.class );

            query.setIgnoreCache( true );

            query.setFilter( "this.id == \"" + id + "\"" );

            Collection result = (Collection) query.execute();

            if ( result.isEmpty() )
            {
                throw new RuntimeException( "No such object of type \"UpdateScmResult\" with id: \"" + id + "\"." );
            }

            UpdateScmResult object = (UpdateScmResult) result.iterator().next();

            if ( detach )
            {
                object = (UpdateScmResult) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public UpdateScmResult getUpdateScmResultByJdoId( Object id, boolean detach )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            pm.getFetchPlan().setGroup( FetchPlan.DEFAULT );

            pm.getFetchPlan().addGroup( "UpdateScmResult_detail" );

            UpdateScmResult object = (UpdateScmResult) pm.getObjectById( id, true );

            if ( detach )
            {
                object = (UpdateScmResult) pm.detachCopy( object );
            }

            commit();

            return object;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }

    public Collection getUpdateScmResultCollection( boolean detach, String filter, String ordering )
        throws Exception
    {
        try
        {
            PersistenceManager pm = begin();

            Extent extent = pm.getExtent( UpdateScmResult.class, true );

            Query query = pm.newQuery( extent );

            if ( ordering != null )
            {
                query.setOrdering( ordering );
            }

            if ( filter != null )
            {
                query.setFilter( filter );
            }

            Collection result = ((Collection) query.execute() );

            Collection collection;

            // TODO: Why did was this done with a iterator and not makeTransientAll()? -- trygve
            //       I would guess I had to do it this way to make sure JPOX fetched all fields
            //       so this should probably go away now if detach works like expected.
            //       Also; why refresh()?
            if ( detach )
            {
                collection = new ArrayList();

                for ( Iterator it = result.iterator(); it.hasNext(); )
                {
                    Object object = it.next();

                    pm.refresh( object );

                    object = pm.detachCopy( object );

                    collection.add( object );
                }
            }
            else
            {
                collection = result;
            }

            commit();

            return collection;
        }
        catch( Exception ex )
        {
            rollback();

            throw ex;
        }
    }


    // ----------------------------------------------------------------------
    // Utility Methods
    // ----------------------------------------------------------------------

    private void closePersistenceManager( PersistenceManager pm )
    {
        try
        {
            pm.close();
        }
        catch( JDOUserException ex )
        {
            ex.printStackTrace();
        }
    }
}
