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

import org.apache.maven.continuum.release.tasks.PrepareReleaseProjectTask;
import org.apache.maven.continuum.release.tasks.ReleaseProjectTask;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

import java.util.ArrayList;

/**
 * @author Edwin Punzalan
 */
public class PrepareReleaseTaskExecutor
    extends AbstractReleaseTaskExecutor
{
    protected void execute( ReleaseProjectTask task )
        throws TaskExecutionException
    {
        PrepareReleaseProjectTask prepareTask = (PrepareReleaseProjectTask) task;

        ReleaseDescriptor descriptor = prepareTask.getDescriptor();

        ReleaseResult result = releaseManager.prepareWithResult( descriptor, settings, new ArrayList(),
                                                                 false, false, prepareTask.getListener() );

        //override to show the actual start time
        result.setStartTime( getStartTime() );

        if ( result.getResultCode() == ReleaseResult.SUCCESS )
        {
            continuumReleaseManager.getPreparedReleases().put( prepareTask.getReleaseId(), descriptor );
        }

        continuumReleaseManager.getReleaseResults().put( prepareTask.getReleaseId(), result );
    }
}
