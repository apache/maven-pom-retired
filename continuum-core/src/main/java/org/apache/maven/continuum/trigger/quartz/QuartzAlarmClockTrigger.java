package org.apache.maven.continuum.trigger.quartz;

import org.apache.maven.continuum.scheduler.ContinuumBuildJob;
import org.apache.maven.continuum.scheduler.ContinuumScheduler;
import org.apache.maven.continuum.trigger.AbstractContinuumTrigger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.text.ParseException;
import java.util.Date;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class QuartzAlarmClockTrigger
    extends AbstractContinuumTrigger
    implements Initializable, Startable
{
    /**
     * @plexus.configuration
     */
    private int interval;

    /**
     * @plexus.configuration
     */
    private int delay;

    /** @plexus.requirement */
    private ContinuumScheduler scheduler;

    // ----------------------------------------------------------------------
    // Plexus Component Implementation
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        if ( interval <= 0 )
        {
            throw new InitializationException( "Invalid value for 'interval': the interval must be bigger that 0." );
        }

        if ( delay <= 0 )
        {
            throw new InitializationException( "Invalid value for 'delay': the delay must be bigger that 0." );
        }
    }

    public void start()
        throws StartingException
    {
        getLogger().info( "Build interval: " + interval + "s" );

        getLogger().info( "Will schedule the first build in: " + delay + "s" );

        JobDataMap dataMap = new JobDataMap();

        dataMap.put( "continuum", getContinuum() );

        dataMap.put( "logger", getLogger() );

        JobDetail jobDetail = new JobDetail( "job", "group", ContinuumBuildJob.class );

        jobDetail.setJobDataMap( dataMap );

        //ContinuumBuildTrigger trigger = new ContinuumBuildTrigger();

        CronTrigger trigger = new CronTrigger();

        trigger.setName( "buildTrigger" );

        trigger.setGroup( "continuum" );

        System.out.println( "new Date() = " + new Date() );

        System.out.println( "System.currentTimeMillis() = " + System.currentTimeMillis() );

        System.out.println( "delay = " + delay * 1000 );

        Date startTime = new Date(System.currentTimeMillis() + ( delay * 1000 ) );

        System.out.println( "startTime = " + startTime );

        trigger.setStartTime( startTime );

        trigger.setNextFireTime( startTime );

        try
        {
            trigger.setCronExpression( "0 0 * * * ?" );
        }
        catch ( ParseException e )
        {
            throw new StartingException( "Error parsing cron expression.", e );
        }

        //trigger.setRepeatInterval( interval * 1000 );

        //trigger.setRepeatCount( ContinuumBuildTrigger.REPEAT_INDEFINITELY );

        try
        {
            scheduler.scheduleJob( jobDetail, trigger );

            getLogger().info( trigger.getNextFireTime() + "" );
        }
        catch ( Exception e )
        {
            throw new StartingException( "Cannot schedule build job.", e );
        }
    }

    public void stop()
    {
    }
}
