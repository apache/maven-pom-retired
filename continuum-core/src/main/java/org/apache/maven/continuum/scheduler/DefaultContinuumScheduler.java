package org.apache.maven.continuum.scheduler;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.Job;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

public class DefaultContinuumScheduler
    extends AbstractLogEnabled
    implements ContinuumScheduler, Contextualizable, Initializable, Startable
{
    private Properties properties;

    private StdScheduler scheduler;

    private Continuum continuum;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public boolean jobExists( String jobName, String jobGroup )
        throws ContinuumSchedulerException
    {
        String[] jobNames = null;

        try
        {
             jobNames = scheduler.getJobNames( jobGroup );
        }
        catch ( SchedulerException e )
        {
            throw new ContinuumSchedulerException( "Error getting job.", e );
        }

        for ( int i = 0; i < jobNames.length; i++ )
        {
            String name = jobNames[i];

            if ( jobName.equals( name ) );

            return true;
        }

        return false;
    }

    /**
     * Create job detail for a build job. The detail contains a map of objects that can be utilized
     * by the executing job.
     *
     * @param schedule
     * @return
     */
    protected JobDetail createJobDetail( ContinuumSchedule schedule )
    {
        JobDetail jobDetail = new JobDetail( schedule.getName(), Scheduler.DEFAULT_GROUP, ContinuumBuildJob.class );

        jobDetail.setJobDataMap( createJobDataMap( schedule ) );

        return jobDetail;
    }

    /**
     * Create Job data map for a build job. The map of objects created can be utilized by
     * the executing job.
     *
     * @param schedule
     * @return
     */
    protected JobDataMap createJobDataMap( ContinuumSchedule schedule )
    {
        JobDataMap dataMap = new JobDataMap();

        dataMap.put( ContinuumSchedulerConstants.CONTINUUM, continuum );

        dataMap.put( ContinuumSchedulerConstants.LOGGER, getLogger() );

        dataMap.put( ContinuumSchedulerConstants.SCHEDULE, schedule );

        return dataMap;
    }

    /**
     * Create the trigger for the build job.
     *
     * @param schedule
     * @return
     * @throws ContinuumSchedulerException
     */
    protected Trigger createTrigger( ContinuumSchedule schedule )
        throws ContinuumSchedulerException
    {
        CronTrigger trigger = new CronTrigger();

        trigger.setName( schedule.getName() );

        trigger.setGroup( Scheduler.DEFAULT_GROUP );

        Date startTime = new Date( System.currentTimeMillis() + ( schedule.getDelay() * 1000 ) );

        trigger.setStartTime( startTime );

        trigger.setNextFireTime( startTime );

        try
        {
            trigger.setCronExpression( schedule.getCronExpression() );
        }
        catch ( ParseException e )
        {
            throw new ContinuumSchedulerException( "Error parsing cron expression.", e );
        }

        return trigger;
    }

    public void scheduleJob( JobDetail jobDetail, Trigger trigger )
        throws ContinuumSchedulerException
    {
        if ( jobExists( jobDetail.getName(), jobDetail.getGroup()) )
        {
            getLogger().warn( "Will not schedule this job as a job {" + jobDetail.getName() + ":" + jobDetail.getGroup() + "} already exists." );

            return;
        }

        try
        {
            scheduler.scheduleJob( jobDetail, trigger );
        }
        catch ( SchedulerException e )
        {
            throw new ContinuumSchedulerException( "Error scheduling job.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void addGlobalJobListener( JobListener listener )
    {
        scheduler.addGlobalJobListener( listener );
    }

    public void addGlobalTriggerListener( TriggerListener listener )
    {
        scheduler.addGlobalTriggerListener( listener );
    }

    // ----------------------------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------------------------

    private PlexusContainer container;

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    protected Continuum getContinuum()
    {
        if ( continuum == null )
        {
            try
            {
                continuum = (Continuum) container.lookup( Continuum.ROLE );
            }
            catch ( ComponentLookupException e )
            {
                // Should never happen.
                getLogger().error( "Cannot lookup Continuum component.", e );
            }
        }

        return continuum;
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            SchedulerFactory factory = new StdSchedulerFactory( properties );

            scheduler = (StdScheduler) factory.getScheduler();
        }
        catch ( SchedulerException e )
        {
            throw new InitializationException( "Cannot create scheduler.", e );
        }
    }

    public void start()
        throws StartingException
    {
        try
        {
            scheduler.start();
        }
        catch ( SchedulerException e )
        {
            throw new StartingException( "Cannot start scheduler.", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        scheduler.shutdown();
    }
}
