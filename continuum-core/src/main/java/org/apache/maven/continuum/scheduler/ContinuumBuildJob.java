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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.logging.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumBuildJob
    implements InterruptableJob
{
    private boolean interrupted;

    public void execute( JobExecutionContext context )
    {
        if ( interrupted )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Get the job detail
        // ----------------------------------------------------------------------

        JobDetail jobDetail = context.getJobDetail();

        // ----------------------------------------------------------------------
        // Get data map out of the job detail
        // ----------------------------------------------------------------------

        Logger logger = (Logger) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.LOGGER );

        String jobName = jobDetail.getName();

        logger.info( ">>>>>>>>>>>>>>>>>>>>> Executing build job (" + jobName + ")..." );

        Continuum continuum = (Continuum) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.CONTINUUM );

        Schedule schedule = (Schedule) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.SCHEDULE );

        try
        {
            continuum.buildProjects( schedule );
        }
        catch ( ContinuumException e )
        {
            logger.error( "Error building projects for job" + jobName + ".", e );
        }

        try
        {
            if ( schedule.getDelay() > 0 )
            {
                Thread.currentThread().sleep( schedule.getDelay() * 1000 );
            }
        }
        catch( InterruptedException e )
        {
        }
    }

    public void interrupt()
        throws UnableToInterruptJobException
    {
        interrupted = true;
    }
}