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

import org.codehaus.plexus.taskqueue.Task;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class BuildProjectTask
    implements Task
{
    private int projectId;

    private long timestamp;

    private int trigger;

    public BuildProjectTask( int projectId, int trigger )
    {
        this.projectId = projectId;

        this.timestamp = System.currentTimeMillis();

        this.trigger = trigger;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getTrigger()
    {
        return trigger;
    }
}
