package org.apache.maven.continuum.scheduler;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.SchedulerException;
import org.quartz.JobListener;
import org.quartz.TriggerListener;

public interface ContinuumScheduler
{
    public static String ROLE = ContinuumScheduler.class.getName();

    void scheduleJob( JobDetail jobDetail, Trigger trigger )
        throws ContinuumSchedulerException;

    void addGlobalJobListener( JobListener listener );

    void addGlobalTriggerListener( TriggerListener listener );  
}
