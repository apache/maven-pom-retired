package org.apache.maven.continuum.buildqueue;

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

import org.codehaus.plexus.taskqueue.TaskQueue;
import org.codehaus.plexus.taskqueue.TaskQueueException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: DefaultBuildQueue.java,v 1.1.1.1 2005/03/29 20:42:01 trygvis Exp $
 */
public class DefaultBuildQueue
    extends AbstractBuildQueue
    implements BuildQueue
{
    /** @requirement */
    private TaskQueue taskQueue;

    // ----------------------------------------------------------------------
    // BuildQueue Implementation
    // ----------------------------------------------------------------------

    public String dequeue()
        throws BuildQueueException
    {
        try
        {
            BuildProjectTask task = (BuildProjectTask) taskQueue.take();

            if ( task == null )
            {
                return null;
            }

            return task.getBuildId();
        }
        catch ( TaskQueueException e )
        {
            throw new BuildQueueException( "Error while getting a task from the task queue.", e );
        }
    }

    public void enqueue( String projectId, String buildId )
        throws BuildQueueException
    {
        BuildProjectTask task = new BuildProjectTask( projectId, buildId );

        try
        {
            taskQueue.put( task );
        }
        catch ( TaskQueueException e )
        {
            throw new BuildQueueException( "Error while enqueing project.", e );
        }
    }
}
