package org.apache.maven.continuum.build.settings;

/*
 * Copyright 2005 The Apache Software Foundation.
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
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.scheduler.ContinuumBuildJob;
import org.apache.maven.continuum.scheduler.ContinuumScheduler;
import org.apache.maven.continuum.scheduler.ContinuumSchedulerException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class DefaultBuildSettingsActivator
    extends AbstractLogEnabled
    implements BuildSettingsActivator
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ContinuumScheduler scheduler;

    //private int delay = 3600;
    private int delay = 1;

    public void activateBuildSettings( Continuum continuum )
        throws BuildSettingsActivationException
    {
        getLogger().info( "Activating build settings ..." );

        try
        {
            Collection buildSettingsCollection = store.getBuildSettings();

            for ( Iterator i = buildSettingsCollection.iterator(); i.hasNext(); )
            {
                ContinuumBuildSettings buildSettings = (ContinuumBuildSettings) i.next();

                if ( StringUtils.isEmpty( buildSettings.getCronExpression() ) )
                {
                    // TODO: this can possibly be removed but it's here now to
                    // weed out any bugs
                    getLogger().info( "Not scheduling " + buildSettings.getName() );

                    continue;
                }

                scheduleBuildSettings( buildSettings, continuum );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new BuildSettingsActivationException( "Error activating build settings.", e );
        }
    }

    protected void scheduleBuildSettings( ContinuumBuildSettings buildSettings, Continuum continuum )
        throws BuildSettingsActivationException
    {
        JobDataMap dataMap = new JobDataMap();

        dataMap.put( "continuum", continuum );

        dataMap.put( "logger", getLogger() );

        //the name + group makes the job unique

        JobDetail jobDetail = new JobDetail( buildSettings.getName(), Scheduler.DEFAULT_GROUP, ContinuumBuildJob.class );

        jobDetail.setJobDataMap( dataMap );

        CronTrigger trigger = new CronTrigger();

        trigger.setName( buildSettings.getName() );

        trigger.setGroup( Scheduler.DEFAULT_GROUP );

        Date startTime = new Date( System.currentTimeMillis() + delay * 1000 );

        trigger.setStartTime( startTime );

        trigger.setNextFireTime( startTime );

        try
        {
            trigger.setCronExpression( buildSettings.getCronExpression() );
        }
        catch ( ParseException e )
        {
            throw new BuildSettingsActivationException( "Error parsing cron expression.", e );
        }

        try
        {
            scheduler.scheduleJob( jobDetail, trigger );

            getLogger().info( trigger.getNextFireTime() + "" );
        }
        catch ( ContinuumSchedulerException e )
        {
            throw new BuildSettingsActivationException( "Cannot schedule build job.", e );
        }
    }
}
