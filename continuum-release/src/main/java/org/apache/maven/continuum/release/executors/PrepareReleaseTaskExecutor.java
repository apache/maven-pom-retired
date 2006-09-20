package org.apache.maven.continuum.release.executors;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.release.ContinuumReleaseException;
import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.plugins.release.ReleaseManagerListener;
import org.apache.maven.plugins.release.ReleaseResult;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

import java.util.List;

/**
 * @author Edwin Punzalan
 */
public class PrepareReleaseTaskExecutor
    extends AbstractReleaseTaskExecutor
{
    public void executeTask( Task task )
        throws TaskExecutionException
    {
        PrepareReleaseProjectTask prepareTask = (PrepareReleaseProjectTask) task;

        ReleaseManagerListener listener = prepareTask.getListener();

        Settings settings;
        try
        {
            settings = getSettings();
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = new ReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( prepareTask.getReleaseId(), result );

            listener.error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }

        ReleaseDescriptor descriptor = prepareTask.getDescriptor();

        List reactorProjects;
        try
        {
            reactorProjects = getReactorProjects( descriptor );
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = new ReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( prepareTask.getReleaseId(), result );

            listener.error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }

        ReleaseResult result = releasePluginManager.prepareWithResult( descriptor, settings, reactorProjects,
                                                                       false, false, listener );

        if ( result.getResultCode() == ReleaseResult.SUCCESS )
        {
            continuumReleaseManager.getPreparedReleases().put( prepareTask.getReleaseId(), descriptor );
        }

        continuumReleaseManager.getReleaseResults().put( prepareTask.getReleaseId(), result );
    }
}
