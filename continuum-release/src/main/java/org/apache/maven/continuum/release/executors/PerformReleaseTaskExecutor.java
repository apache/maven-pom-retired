package org.apache.maven.continuum.release.executors;

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

import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.tasks.PerformReleaseProjectTask;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

import java.util.List;

/**
 * @author Edwin Punzalan
 */
public class PerformReleaseTaskExecutor
    extends AbstractReleaseTaskExecutor
{
    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PerformReleaseProjectTask performTask = (PerformReleaseProjectTask) task;

        ReleaseManagerListener listener = performTask.getListener();

        ReleaseDescriptor descriptor = performTask.getDescriptor();

        Settings settings;
        try
        {
            settings = getSettings();
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = new ReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( performTask.getReleaseId(), result );

            listener.error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }

        List reactorProjects;
        try
        {
            reactorProjects = getReactorProjects( descriptor );
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = new ReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( performTask.getReleaseId(), result );

            listener.error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }

        ReleaseResult result =
            releasePluginManager.performWithResult( descriptor, settings, reactorProjects,
                                                    performTask.getBuildDirectory(), performTask.getGoals(),
                                                    performTask.isUseReleaseProfile(), listener );

        if ( result.getResultCode() == ReleaseResult.SUCCESS )
        {
            continuumReleaseManager.getPreparedReleases().remove( performTask.getReleaseId() );
        }

        continuumReleaseManager.getReleaseResults().put( performTask.getReleaseId(), result );
    }
}
