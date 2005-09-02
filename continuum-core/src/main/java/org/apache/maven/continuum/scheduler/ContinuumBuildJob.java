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
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumBuildJob
    implements Job
{
    public void execute( JobExecutionContext context )
    {
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

        /*

        Continuum continuum = (Continuum) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.CONTINUUM );

        ContinuumBuildSettings buildSettings = (ContinuumBuildSettings) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.BUILD_SETTINGS );

        // ----------------------------------------------------------------------
        // Lookup all the project groups that belong to these build settings
        // ----------------------------------------------------------------------

        Set projectGroups = buildSettings.getProjectGroups();

        for ( Iterator iterator = projectGroups.iterator(); iterator.hasNext(); )
        {
            ContinuumProjectGroup projectGroup = (ContinuumProjectGroup) iterator.next();

            try
            {
                continuum.buildProjectGroup( projectGroup, buildSettings );
            }
            catch ( ContinuumException e )
            {
                logger.error( "Error building project group.", e );
            }
        }

        */
    }
}