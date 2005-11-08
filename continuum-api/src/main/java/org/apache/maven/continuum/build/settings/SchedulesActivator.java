package org.apache.maven.continuum.build.settings;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Schedule;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public interface SchedulesActivator
{
    String ROLE = SchedulesActivator.class.getName();

    /**
     * Grab all the stored {@link org.apache.maven.continuum.model.project.Schedule} objects
     * and activate them by looking at the scheduling information contained within and submitting a
     * Job to the scheduler.
     *
     * @throws SchedulesActivationException
     */
    void activateSchedules( Continuum continuum )
        throws SchedulesActivationException;

    /**
     * Activate schedule by looking at the scheduling information contained within and submitting a
     * Job to the scheduler.
     *
     * @throws SchedulesActivationException
     */
    void activateSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException;

    /**
     * Unactivate schedule by looking at the scheduling information contained within.
     *
     * @throws SchedulesActivationException
     */
    void unactivateSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException;
}
