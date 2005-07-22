package org.apache.maven.continuum.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobDetail;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumSchedule;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.logging.Logger;

import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

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

        Continuum continuum = (Continuum) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.CONTINUUM );

        ContinuumBuildSettings buildSettings = (ContinuumBuildSettings) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.BUILD_SETTINGS );

        // ----------------------------------------------------------------------
        // Lookup all the project groups that belong to these build settings
        // ----------------------------------------------------------------------

        Set projectGroups = buildSettings.getProjectGroups();

        for ( Iterator iterator = projectGroups.iterator(); iterator.hasNext(); )
        {
            ContinuumProjectGroup projectGroup = (ContinuumProjectGroup) iterator.next();

            Set projects = projectGroup.getProjects();

            for ( Iterator j = projects.iterator(); j.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) j.next();

                try
                {
                    continuum.buildProject( project.getId(), false );
                }
                catch ( ContinuumException ex )
                {
                    logger.error( "Could not enqueue project: " + project.getId() + " ('" + project.getName() + "').", ex );
                }
            }
        }
    }
}