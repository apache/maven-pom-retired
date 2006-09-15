package org.apache.maven.continuum.release.tasks;

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

import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.taskqueue.Task;

import java.util.List;

/**
 * @author Edwin Punzalan
 */
public abstract class AbstractReleaseProjectTask
    implements Task
{
    private int projectId;

    private ReleaseDescriptor descriptor;

    private Settings settings;

    private List reactorProjects;

    public AbstractReleaseProjectTask( int projectId, ReleaseDescriptor descriptor,
                                       Settings settings, List reactorProjects )
    {
        this.projectId = projectId;
        this.descriptor = descriptor;
        this.settings = settings;
        this.reactorProjects = reactorProjects;
    }

    public ReleaseDescriptor getDescriptor()
    {
        return descriptor;
    }

    public void setDescriptor( ReleaseDescriptor descriptor )
    {
        this.descriptor = descriptor;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public void setSettings( Settings settings )
    {
        this.settings = settings;
    }

    public List getReactorProjects()
    {
        return reactorProjects;
    }

    public void setReactorProjects( List reactorProjects )
    {
        this.reactorProjects = reactorProjects;
    }
}
