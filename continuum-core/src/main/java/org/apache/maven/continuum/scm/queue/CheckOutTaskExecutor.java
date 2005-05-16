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

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.scm.manager.NoSuchScmProviderException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id:$
 */
public class CheckOutTaskExecutor
    extends AbstractLogEnabled
    implements TaskExecutor
{
    /** @requirement */
    private ContinuumScm scm;

    /** @requirement */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task t )
        throws TaskExecutionException
    {
        CheckOutTask task = (CheckOutTask) t;

        String projectId = task.getProjectId();

        File workingDirectory = task.getWorkingDirectory();

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
        catch( Throwable e )
        {
            exception = e;
        }

        try
        {
            store.setCheckoutDone( projectId, result, errorMessage, exception );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error while storing the check out result.", e );
        }
    }
}
