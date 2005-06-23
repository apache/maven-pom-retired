package org.apache.maven.continuum.scm.queue;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.core.action.CheckOutProjectContinuumAction;

import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckOutTaskExecutor
    extends AbstractLogEnabled
    implements TaskExecutor
{
    /** @plexus.requirement */
    private ActionManager actionManager;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task t )
        throws TaskExecutionException
    {
        CheckOutTask task = (CheckOutTask) t;

        String projectId = task.getProjectId();

        String workingDirectory = task.getWorkingDirectory().getAbsolutePath();

        Map context = new HashMap();

        context.put( CheckOutProjectContinuumAction.KEY_PROJECT_ID, projectId );

        context.put( CheckOutProjectContinuumAction.KEY_WORKING_DIRECTORY, workingDirectory );

        try
        {
            actionManager.lookup( "checkout-project" ).execute( context );
        }
        catch ( Exception e )
        {
            throw new TaskExecutionException( "Error checking out project.", e );
        }

        // TODO: Replace with a exection of the "check out project" action
/*
        ContinuumProject project;

        try
        {
            project = store.getProject( projectId );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error while reading the project from the store.", e );
        }

        CheckOutScmResult result = null;

        String errorMessage = null;

        Throwable exception = null;

        try
        {
            result = scm.checkOut( project, workingDirectory );
        }
        catch( Throwable e )
        {
            // TODO: Dissect the scm exception to be able to give better feedback
            Throwable cause = e.getCause();

            if ( cause instanceof NoSuchScmProviderException )
            {
                errorMessage = cause.getMessage();
            }
            else
            {
                errorMessage = "";

                exception = e;
            }
        }

        try
        {
            project = store.getProject( projectId );

            project.setCheckOutScmResult( result );

            project.setCheckOutErrorMessage( errorMessage );

            project.setCheckOutErrorException( AbstractContinuumStore.throwableToString( exception ) );

            store.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error while storing the check out result.", e );
        }
*/
    }
}
