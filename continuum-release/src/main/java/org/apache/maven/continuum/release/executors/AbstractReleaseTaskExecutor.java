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
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.tasks.ReleaseProjectTask;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseManager;
import org.apache.maven.shared.release.ReleaseResult;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;

/**
 * @author Edwin Punzalan
 */
public abstract class AbstractReleaseTaskExecutor
    implements ReleaseTaskExecutor
{
    /**
     * @plexus.requirement
     */
    protected ContinuumReleaseManager continuumReleaseManager;

    /**
     * @plexus.requirement
     */
    protected ReleaseManager releaseManager;

    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder settingsBuilder;

    protected Settings settings;

    private long startTime;

    public void executeTask( Task task )
        throws TaskExecutionException
    {
        ReleaseProjectTask releaseTask = (ReleaseProjectTask) task;

        setUp( releaseTask );

        execute( releaseTask );
    }

    protected void setUp( ReleaseProjectTask releaseTask )
        throws TaskExecutionException
    {
        //actual release execution start time
        setStartTime( System.currentTimeMillis() );

        try
        {
            //make sure settings is re-read each time
            settings = getSettings();
        }
        catch ( ContinuumReleaseException e )
        {
            ReleaseResult result = createReleaseResult();

            result.appendError( e );

            continuumReleaseManager.getReleaseResults().put( releaseTask.getReleaseId(), result );

            releaseTask.getListener().error( e.getMessage() );

            throw new TaskExecutionException( "Failed to build reactor projects.", e );
        }
    }

    protected abstract void execute( ReleaseProjectTask releaseTask )
        throws TaskExecutionException;

    private Settings getSettings()
        throws ContinuumReleaseException
    {
        try
        {
            settings = settingsBuilder.buildSettings( false );
        }
        catch ( IOException e )
        {
            throw new ContinuumReleaseException( "Failed to get Maven Settings.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ContinuumReleaseException( "Failed to get Maven Settings.", e );
        }

        return settings;
    }

    protected ReleaseResult createReleaseResult()
    {
        ReleaseResult result = new ReleaseResult();

        result.setStartTime( getStartTime() );

        result.setEndTime( System.currentTimeMillis() );

        return result;
    }

    protected long getStartTime()
    {
        return startTime;
    }

    protected void setStartTime( long startTime )
    {
        this.startTime = startTime;
    }
}
