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
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;

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

    private boolean jobExists( String jobName, String jobGroup )
        throws ContinuumSchedulerException
    {
        String[] jobNames;

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

            if ( jobName.equals( name ) )
            {
                return true;
            }
        }

        return false;
    }

    public void scheduleJob( JobDetail jobDetail, Trigger trigger )
        throws ContinuumSchedulerException
    {
        if ( jobExists( jobDetail.getName(), jobDetail.getGroup() ) )
        {
            getLogger().warn( "Will not schedule this job as a job {" + jobDetail.getName() + ":" +
                jobDetail.getGroup() + "} already exists." );

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
