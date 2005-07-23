package org.apache.maven.continuum.trigger.alarmclock;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.trigger.AbstractContinuumTrigger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AlarmClockTrigger
    extends AbstractContinuumTrigger
    implements Initializable, Startable
{
    /** @plexus.configuration */
    private int interval;

    /** @plexus.configuration */
    private int delay;

    private Timer timer;

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

        timer = new Timer();
    }

    public void start()
    {
        getLogger().info( "Build interval: " + interval + "s" );

        getLogger().info( "Will schedule the first build in: " + delay + "s" );

        timer.schedule( new BuildTask(), delay * 1000, interval * 1000 );
    }

    public void stop()
    {
        timer.cancel();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void onTimer()
    {
        Iterator it;

        getLogger().info( "Scheduling projects." );

        try
        {
            it = getContinuum().getAllProjects( 0, 0 ).iterator();
        }
        catch ( Exception e )
        {
            getLogger().error( "Error while getting the project list.", e );

            return;
        }

        while ( it.hasNext() )
        {
            ContinuumProject project = (ContinuumProject) it.next();

            try
            {
                getContinuum().buildProject( project.getId(), false );
            }
            catch ( ContinuumException ex )
            {
                getLogger().error( "Could not enqueue project: " + project.getId() +
                                   " ('" + project.getName() + "').", ex );

                continue;
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private class BuildTask
        extends TimerTask
    {
        public void run()
        {
            onTimer();
        }
    }
}
