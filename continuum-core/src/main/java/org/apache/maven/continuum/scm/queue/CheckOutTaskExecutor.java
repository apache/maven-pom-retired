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

import org.apache.maven.continuum.core.action.CheckoutProjectContinuumAction;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckOutTaskExecutor
    extends AbstractLogEnabled
    implements TaskExecutor
{
    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task t )
        throws TaskExecutionException
    {
        CheckOutTask task = (CheckOutTask) t;

        int projectId = task.getProjectId();

        String workingDirectory = task.getWorkingDirectory().getAbsolutePath();

        Map context = new HashMap();

        context.put( CheckoutProjectContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        context.put( CheckoutProjectContinuumAction.KEY_WORKING_DIRECTORY, workingDirectory );

        try
        {
            actionManager.lookup( "checkout-project" ).execute( context );

            actionManager.lookup( "store-checkout-scm-result" ).execute( context );
        }
        catch ( Exception e )
        {
            throw new TaskExecutionException( "Error checking out project.", e );
        }
    }
}
