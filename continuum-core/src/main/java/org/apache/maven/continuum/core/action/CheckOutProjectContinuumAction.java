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
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.store.AbstractContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.scm.manager.NoSuchScmProviderException;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckOutProjectContinuumAction
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws Exception
    {
        String projectId = getProjectId( context );

        // TODO: just make this get project and hide the store
        ContinuumProject project = getStore().getProject( projectId );

        File workingDirectory = getWorkingDirectory( context );

        CheckOutScmResult result = null;

        String errorMessage = null;

        Throwable exception = null;

        // ----------------------------------------------------------------------
        // Check out the project
        // ----------------------------------------------------------------------

        try
        {
            result = getScm().checkOut( project, workingDirectory );
        }
        catch ( ContinuumScmException e )
        {
            // TODO: Dissect the scm exception to be able to give better feedback
            Throwable cause = e.getCause();

            if ( cause instanceof NoSuchScmProviderException )
            {
                errorMessage = cause.getMessage();
            }
            else
            {
                exception = e;
            }
        }
        catch ( Throwable e )
        {
            exception = e;
        }

        // ----------------------------------------------------------------------
        // Store the check out result or error
        // ----------------------------------------------------------------------

        try
        {
            project = getStore().getProject( projectId );

            project.setCheckOutScmResult( result );

            project.setCheckOutErrorMessage( nullIfEmpty( errorMessage ) );

            project.setCheckOutErrorException( nullIfEmpty( AbstractContinuumStore.throwableToString( exception ) ) );

            getStore().updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error while storing the check out result.", e );
        }

        // ----------------------------------------------------------------------
        // Safe the result in the context
        // ----------------------------------------------------------------------

        context.put( KEY_CHECKOUT_SCM_RESULT, result );
    }
}
