package org.apache.maven.continuum.web.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.buildqueue.BuildProjectTask;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskQueueExecutor;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="cancelBuild"
 */
public class CancelBuildAction
    extends ContinuumActionSupport
{
    /** @plexus.requirement role-hint='build-project' */
    private TaskQueueExecutor taskQueueExecutor;

    private int projectId;

    public String execute()
        throws ContinuumException
    {
        Task task = taskQueueExecutor.getCurrentTask();

        getLogger().info("TaskQueueExecutor: " + taskQueueExecutor );

        if ( task != null )
        {
            if ( task instanceof BuildProjectTask )
            {
                if ( ( (BuildProjectTask) task ).getProjectId() == projectId )
                {
                    getLogger().info( "Cancelling task for project " + projectId );
                    taskQueueExecutor.cancelTask( task );
                }
                else
                {
                    getLogger().warn( "Current task is not for the given projectId (" + projectId + "): "
                        + ( (BuildProjectTask) task ).getProjectId() + "; not cancelling" );
                }
            }
            else
            {
                getLogger().warn( "Current task not a BuildProjectTask - not cancelling" );
            }
        }
        else
        {
            getLogger().warn( "No task running - not cancelling" );
        }

        return SUCCESS;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }
}
