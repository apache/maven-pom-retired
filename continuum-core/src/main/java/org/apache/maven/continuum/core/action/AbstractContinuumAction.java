package org.apache.maven.continuum.core.action;

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

import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumAction
    extends AbstractLogEnabled
    implements ContinuumAction
{
    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public final static String KEY_PROJECT_ID = "projectId";

    public final static String KEY_BUILD_ID = "buildId";

    public static final String KEY_WORKING_DIRECTORY = "workingDirectory";

    public static final String KEY_CHECKOUT_SCM_RESULT = "checkOutResult";

    protected static final String KEY_UPDATE_SCM_RESULT = "updateResult";

    private static final String KEY_FORCED = "forced";

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumCore core;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    /**
     * @plexus.requirement
     */
    private ContinuumNotificationDispatcher notifier;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static ThreadLocal threadContext = new ThreadLocal();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected abstract void doExecute()
        throws Exception;

    protected abstract void handleException( Throwable throwable )
        throws ContinuumStoreException;

    protected void handleContinuumStoreException( ContinuumStoreException exception )
    {
        getLogger().fatalError( "Error using the store.", exception );
    }

    protected void doFinally()
        throws ContinuumStoreException
    {
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected ContinuumCore getCore()
    {
        return core;
    }

    protected ContinuumStore getStore()
    {
        return store;
    }

    protected ContinuumScm getScm()
    {
        return scm;
    }

    protected ContinuumNotificationDispatcher getNotifier()
    {
        return notifier;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void putContext( String key, Object value )
    {
        getContext().put( key, value );
    }

    protected String getProjectId()
        throws ContinuumStoreException
    {
        return getString( KEY_PROJECT_ID );
    }

    protected String getBuildId()
        throws ContinuumStoreException
    {
        return getString( KEY_BUILD_ID );
    }

    protected boolean isForced()
        throws ContinuumStoreException
    {
        return ((Boolean) getObject( KEY_FORCED )).booleanValue();
    }

    protected ContinuumProject getProject()
        throws ContinuumStoreException
    {
        return getStore().getProject( getProjectId() );
    }

    protected ContinuumBuild getBuild()
        throws ContinuumStoreException
    {
        return getStore().getBuild( getBuildId() );
    }

    protected File getWorkingDirectory()
    {
        return new File( getString( KEY_WORKING_DIRECTORY ) );
    }

    protected CheckOutScmResult getCheckOutResult()
    {
        return (CheckOutScmResult) getObject( KEY_CHECKOUT_SCM_RESULT );
    }

    protected UpdateScmResult getUpdateScmResult()
    {
        return (UpdateScmResult) getObject( KEY_UPDATE_SCM_RESULT );
    }

    protected UpdateScmResult getUpdateScmResult( UpdateScmResult defaultValue )
    {
        return (UpdateScmResult) getObject( KEY_UPDATE_SCM_RESULT, defaultValue );
    }

//    protected void buildCompleted()
//    {
//    }
//
//    protected void buildError( Throwable throwable )
//        throws ContinuumStoreException
//    {
//        UpdateScmResult updateScmResult = getUpdateScmResult( null );
//
//        String buildId = getString( KEY_BUILD_ID, null );
//
//        if ( buildId == null )
//        {
//            createBuild().getId();
//        }
//
//        getStore().setBuildResult( getProjectId(),
//                                   ContinuumProjectState.ERROR,
//                                   null,
//                                   updateScmResult,
//                                   throwable );
//    }

    private String getString( String key, String defaultValue )
    {
        String value = (String) getContext().get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }

    // ----------------------------------------------------------------------
    // ContinuumAction Implementatin
    // ----------------------------------------------------------------------

    public void execute( Map context )
    {
        threadContext.set( context );

        try
        {
            doExecute();
        }
        catch ( ContinuumStoreException e )
        {
            handleContinuumStoreException( e );
        }
        catch ( Exception e )
        {
            try
            {
                handleException( e );
            }
            catch ( ContinuumStoreException e2 )
            {
                handleContinuumStoreException( e2 );
            }
        }
        finally
        {
            try
            {
                doFinally();
            }
            catch ( ContinuumStoreException e )
            {
                handleContinuumStoreException( e );
            }

            threadContext.set( null );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Map getContext()
    {
        return (Map) threadContext.get();
    }

    private String getString( String key )
    {
        return (String) getObject( key );
    }

    private Object getObject( String key )
    {
        Object value = getContext().get( key );

        if ( value == null )
        {
            throw new RuntimeException( "Missing value for key '" + key + "'." );
        }

        return value;
    }

    private Object getObject( String key, Object defaultValue )
    {
        Object value = getContext().get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
