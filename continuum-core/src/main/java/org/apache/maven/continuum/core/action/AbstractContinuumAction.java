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
import org.apache.maven.continuum.execution.manager.BuildExecutorManager;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumAction
    extends AbstractLogEnabled
    implements Action
{
    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public final static String KEY_PROJECT_ID = "project-id";

    public final static String KEY_UNVALIDATED_PROJECT = "unvalidated-project";

    public final static String KEY_BUILD_ID = "build-id";

    public static final String KEY_WORKING_DIRECTORY = "working-directory";

    public static final String KEY_WORKING_DIRECTORY_EXISTS = "working-directory-exists";

    public static final String KEY_CHECKOUT_SCM_RESULT = "checkout-result";

    public static final String KEY_CHECKOUT_ERROR_MESSAGE = "checkout-error-message";

    public static final String KEY_CHECKOUT_ERROR_EXCEPTION = "checkout-error-exception";

    public static final String KEY_UPDATE_SCM_RESULT = "update-result";

    public static final String KEY_FORCED = "forced";

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
    private ContinuumNotificationDispatcher notificationDispatcher;

    // ----------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------

    protected String nullIfEmpty( String string )
    {
        if ( StringUtils.isEmpty( string ) )
        {
            return null;
        }

        return string;
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
        return core.getStore();
    }

    protected ContinuumScm getScm()
    {
        return core.getScm();
    }

    protected ContinuumNotificationDispatcher getNotifier()
    {
        return notificationDispatcher;
    }

    protected ContinuumProjectBuilderManager getProjectBuilderManager()
    {
        return core.getProjectBuilderManager();
    }

    protected TaskQueue getBuildQueue()
    {
        return core.getBuildQueue();
    }

    protected TaskQueue getCheckOutQueue()
    {
        return core.getCheckOutQueue();
    }

    protected BuildExecutorManager getBuildExecutorManager()
    {
        return core.getBuildExecutorManager();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static String getProjectId( Map context )
    {
        return getString( context, KEY_PROJECT_ID );
    }

    public static String getBuildId( Map context )
    {
        return getString( context, KEY_BUILD_ID );
    }

    public static boolean isForced( Map context )
    {
        return getBoolean( context, KEY_FORCED );
    }

    protected ContinuumProject getProject( Map context )
        throws ContinuumStoreException
    {
        return getStore().getProject( getProjectId( context ) );
    }

    public static ContinuumProject getUnvalidatedProject( Map context )
    {
        return ((ContinuumProject) getObject( context, KEY_UNVALIDATED_PROJECT ) );
    }

    protected ContinuumBuild getBuild( Map context )
        throws ContinuumStoreException
    {
        return getStore().getBuild( getBuildId( context ) );
    }

    public static File getWorkingDirectory( Map context )
    {
        return new File( getString( context, KEY_WORKING_DIRECTORY ) );
    }

    public static CheckOutScmResult getCheckoutResult( Map context )
    {
        return (CheckOutScmResult) getObject( context, KEY_CHECKOUT_SCM_RESULT );
    }

    public static String getCheckoutErrorMessage( Map context )
    {
        return getString( context, KEY_CHECKOUT_ERROR_MESSAGE );
    }

    public static String getCheckoutErrorException( Map context )
    {
        return getString( context, KEY_CHECKOUT_ERROR_EXCEPTION );
    }

    public static UpdateScmResult getUpdateScmResult( Map context )
    {
        return (UpdateScmResult) getObject( context, KEY_UPDATE_SCM_RESULT );
    }

    public static UpdateScmResult getUpdateScmResult(  Map context, UpdateScmResult defaultValue )
    {
        return (UpdateScmResult) getObject( context, KEY_UPDATE_SCM_RESULT, defaultValue );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected static String getString( Map context, String key )
    {
        return (String) context.get( key );
    }

    public static boolean getBoolean( Map context, String key )
    {
        return ( (Boolean) getObject( context, key ) ).booleanValue();
    }

    private static Object getObject( Map context, String key )
    {
        Object value = context.get( key );

        if ( value == null )
        {
//            System.err.println( "context" );
//            for ( Iterator it = context.keySet().iterator(); it.hasNext(); )
//            {
//                String s = (String) it.next();
//
//                System.err.println( s );
//            }

            throw new RuntimeException( "Missing value for key '" + key + "'." );
        }

        return value;
    }

    private static Object getObject( Map context, String key, Object defaultValue )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
