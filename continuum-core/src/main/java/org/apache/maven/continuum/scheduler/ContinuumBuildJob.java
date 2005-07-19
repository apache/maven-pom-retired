package org.apache.maven.continuum.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobDetail;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.logging.Logger;

import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
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

        Continuum continuum = (Continuum) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.CONTINUUM );

        ContinuumSchedule schedule = (ContinuumSchedule) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.SCHEDULE );

        // ----------------------------------------------------------------------
        // Lookup all projects that belong to this schedule
        // ----------------------------------------------------------------------

        Set projects = schedule.getProjects();

        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            try
            {
                continuum.buildProject( project.getId(), false );
            }
            catch ( ContinuumException ex )
            {
                logger.error( "Could not enqueue project: " + project.getId() + " ('" + project.getName() + "').", ex );

                continue;
            }
        }
    }
}