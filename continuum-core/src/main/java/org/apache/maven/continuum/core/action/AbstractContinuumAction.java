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

import java.util.Map;
import java.io.File;

import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.CheckOutScmResult;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumAction
    extends AbstractLogEnabled
    implements ContinuumAction
{
    public final static String KEY_PROJECT_ID = "projectId";

    public static final String KEY_WORKING_DIRECTORY = "workingDirectory";

    public static final String KEY_CHECKOUT_RESULT = "checkOutResult";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumScm scm;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected abstract void doExecute( Map context )
        throws Exception;

    protected abstract void handleException( Map context, Throwable throwable )
        throws ContinuumStoreException;

    protected void handleContinuumStoreException( ContinuumStoreException exception )
    {
        getLogger().fatalError( "Error using the store.", exception );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected ContinuumStore getStore()
    {
        return store;
    }

    protected ContinuumScm getContinuumScm()
    {
        return scm;
    }

    protected String getProjectId( Map context )
        throws ContinuumStoreException
    {
        return getString( context, KEY_PROJECT_ID );
    }

    protected File getWorkingDirectory( Map context )
    {
        return new File( getString( context, KEY_WORKING_DIRECTORY ) );
    }

    protected CheckOutScmResult getCheckOutResult( Map context )
    {
        return (CheckOutScmResult) getObject( context, KEY_CHECKOUT_RESULT );
    }

    // ----------------------------------------------------------------------
    // ContinuumAction Implementatin
    // ----------------------------------------------------------------------

    public void execute( Map context )
    {
        try
        {
            doExecute( context );
        }
        catch ( ContinuumStoreException e )
        {
            handleContinuumStoreException( e );
        }
        catch ( Exception e )
        {
            try
            {
                handleException( context, e );
            }
            catch ( ContinuumStoreException e2 )
            {
                handleContinuumStoreException( e2 );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String getString( Map context, String key )
    {
        return (String) getObject( context, key );
    }

    private Object getObject( Map context, String key )
    {
        Object value = context.get( key );

        if ( value == null )
        {
            throw new RuntimeException( "Missing value for key '" + key + "'." );
        }

        return value;
    }
}
