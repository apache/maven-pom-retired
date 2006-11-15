package org.apache.maven.continuum.build.settings;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import org.apache.maven.continuum.scheduler.ContinuumBuildJob;
import org.apache.maven.continuum.scheduler.ContinuumSchedulerConstants;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.scheduler.AbstractJob;
import org.codehaus.plexus.scheduler.Scheduler;
import org.codehaus.plexus.util.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.apache.maven.continuum.build.settings.SchedulesActivator"
 */
public class DefaultSchedulesActivator
    extends AbstractLogEnabled
    implements SchedulesActivator
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private Scheduler scheduler;

    //private int delay = 3600;
    private int delay = 1;

    public void activateSchedules( Continuum continuum )
        throws SchedulesActivationException
    {
        getLogger().info( "Activating schedules ..." );

        Collection schedules = store.getAllSchedulesByName();

        for ( Iterator i = schedules.iterator(); i.hasNext(); )
        {
            Schedule schedule = (Schedule) i.next();

            if ( StringUtils.isEmpty( schedule.getCronExpression() ) )
            {
                // TODO: this can possibly be removed but it's here now to
                // weed out any bugs
                getLogger().info( "Not scheduling " + schedule.getName() );

                continue;
            }

            try
            {
                schedule( schedule, continuum );
            }
            catch ( SchedulesActivationException e )
            {
                getLogger().error( "Can't activate schedule '" + schedule.getName() + "'", e );

                schedule.setActive( false );

                try
                {
                    store.storeSchedule( schedule );
                }
                catch ( ContinuumStoreException e1 )
                {
                    throw new SchedulesActivationException( "Can't desactivate schedule '" + schedule.getName() + "'",
                                                            e );
                }
            }
        }
    }

    public void activateSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        getLogger().info( "Activating schedule " + schedule.getName() );

        schedule( schedule, continuum );
    }

    public void unactivateSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        getLogger().info( "Unactivating schedule " + schedule.getName() );

        unschedule( schedule, continuum );
    }

    protected void schedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        if ( !schedule.isActive() )
        {
            getLogger().info( "Schedule \"" + schedule.getName() + "\" is disabled." );

            return;
        }

        JobDataMap dataMap = new JobDataMap();

        dataMap.put( "continuum", continuum );

        dataMap.put( AbstractJob.LOGGER, getLogger() );

        dataMap.put( ContinuumSchedulerConstants.SCHEDULE, schedule );

        //the name + group makes the job unique

        JobDetail jobDetail =
            new JobDetail( schedule.getName(), org.quartz.Scheduler.DEFAULT_GROUP, ContinuumBuildJob.class );

        jobDetail.setJobDataMap( dataMap );

        jobDetail.setDescription( schedule.getDescription() );

        CronTrigger trigger = new CronTrigger();

        trigger.setName( schedule.getName() );

        trigger.setGroup( org.quartz.Scheduler.DEFAULT_GROUP );

        Date startTime = new Date( System.currentTimeMillis() + delay * 1000 );

        trigger.setStartTime( startTime );

        trigger.setNextFireTime( startTime );

        try
        {
            trigger.setCronExpression( schedule.getCronExpression() );
        }
        catch ( ParseException e )
        {
            throw new SchedulesActivationException( "Error parsing cron expression.", e );
        }

        try
        {
            scheduler.scheduleJob( jobDetail, trigger );

            getLogger().info( trigger.getName() + ": next fire time ->" + trigger.getNextFireTime() );
        }
        catch ( SchedulerException e )
        {
            throw new SchedulesActivationException( "Cannot schedule build job.", e );
        }
    }

    protected void unschedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        try
        {
            if ( schedule.isActive() )
            {
                getLogger().info( "Stopping active schedule \"" + schedule.getName() + "\"." );

                scheduler.interruptSchedule( schedule.getName(), org.quartz.Scheduler.DEFAULT_GROUP );
            }

            scheduler.unscheduleJob( schedule.getName(), org.quartz.Scheduler.DEFAULT_GROUP );
        }
        catch ( SchedulerException e )
        {
            throw new SchedulesActivationException( "Cannot unschedule build job \"" + schedule.getName() + "\".", e );
        }
    }
}
