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

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.scm.ScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckoutProjectContinuumAction
    extends AbstractContinuumAction
{
    private ContinuumScm scm;

    private ContinuumStore store;

    public void execute( Map context )
        throws Exception
    {
        ContinuumProject project = store.getProject( getProjectId( context ) );

        File workingDirectory = getWorkingDirectory( context );

        // ----------------------------------------------------------------------
        // Check out the project
        // ----------------------------------------------------------------------

        try
        {
            ScmResult result = scm.checkOut( project, workingDirectory );

            context.put( KEY_CHECKOUT_SCM_RESULT, result );
        }
        catch ( Throwable e )
        {
            handleThrowable( e, context );
        }
    }

    public static void handleThrowable( Throwable e, Map context )
    {
        String errorMessage;

        Throwable exception;

        if ( e instanceof ContinuumScmException )
        {
            // TODO: Dissect the scm exception to be able to give better feedback
            Throwable cause = e.getCause();

            if ( cause instanceof NoSuchScmProviderException )
            {
                errorMessage = cause.getMessage();

                exception = null;
            }
            else
            {
                ContinuumScmException ex = (ContinuumScmException) e;

                ScmResult result = ex.getResult();

                if ( result != null )
                {
                    errorMessage = "";
                    errorMessage += "Provider message: "  + StringUtils.clean( result.getProviderMessage() ) + System.getProperty( "line.separator" );
                    errorMessage += "Command output: " + System.getProperty( "line.separator" );
                    errorMessage += "-------------------------------------------------------------------------------" + System.getProperty( "line.separator" );
                    errorMessage += StringUtils.clean( result.getCommandOutput() );
                    errorMessage += "-------------------------------------------------------------------------------" + System.getProperty( "line.separator" );
                }
                else
                {
                    errorMessage = "";
                }

                exception = e;
            }
        }
        else
        {
            errorMessage = "Unknown exception, type: " + e.getClass().getName();

            exception = e;
        }

        context.put( KEY_CHECKOUT_ERROR_MESSAGE, errorMessage );

        context.put( KEY_CHECKOUT_ERROR_EXCEPTION, ContinuumUtils.throwableToString( exception ) );
    }
}
