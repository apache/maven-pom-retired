package org.apache.maven.continuum.scheduler;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.codehaus.plexus.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.Iterator;
import java.util.Set;

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

        logger.info( ">>>>>>>>>>>>>>>>>>>>> Executing build job ..." );

        return;

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