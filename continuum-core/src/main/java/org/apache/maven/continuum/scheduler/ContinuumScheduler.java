package org.apache.maven.continuum.scheduler;

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

import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

public interface ContinuumScheduler
{
    public static String ROLE = ContinuumScheduler.class.getName();

    void scheduleJob( JobDetail jobDetail, Trigger trigger )
        throws ContinuumSchedulerException;

    void addGlobalJobListener( JobListener listener );

    void addGlobalTriggerListener( TriggerListener listener );

}
