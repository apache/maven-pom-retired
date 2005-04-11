package org.apache.maven.continuum;

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

import org.apache.maven.continuum.buildcontroller.BuildController;
import org.apache.maven.continuum.buildqueue.BuildQueue;
import org.apache.maven.continuum.buildqueue.BuildQueueException;

import org.codehaus.plexus.logging.Logger;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id: BuilderThread.java,v 1.1.1.1 2005/03/29 20:41:58 trygvis Exp $
 */
public class BuilderThread
    implements Runnable
{
    /** */
    private BuildController buildController;

    /** */
    private BuildQueue buildQueue;

    /** */
    private Logger logger;

    /** */
    private boolean shutdown;

    /** */
    private boolean done;

    public BuilderThread( BuildController buildController, BuildQueue buildQueue, Logger logger )
    {
        this.buildController = buildController;

        this.buildQueue = buildQueue;

        this.logger = logger;
    }

    public void run()
    {
        while ( !shutdown )
        {
            String buildId = null;

            buildId = dequeue();

            if ( buildId == null )
            {
                sleep( 100 );

                continue;
            }

            buildController.build( buildId );
        }

        getLogger().info( "Builder thread exited." );

        done = true;

        synchronized ( this )
        {
            notifyAll();
        }
    }

    public void shutdown()
    {
        getLogger().info( "Builder thread got shutdown signal." );

        shutdown = true;
    }

    public boolean isDone()
    {
        return done;
    }

    private Logger getLogger()
    {
        return logger;
    }

    private String dequeue()
    {
        try
        {
            return buildQueue.dequeue();
        }
        catch ( BuildQueueException e )
        {
            getLogger().warn( "Error while getting build from the queue.", e );

            // TODO: Sleep for 10 seconds to give the system some time to breath.
            // If interruped it will return properly.
            sleep( 10 * 1000 );

            return null;
        }
    }

    private void sleep( int interval )
    {
        try
        {
            Thread.sleep( interval );
        }
        catch ( InterruptedException ex )
        {
            // ignore
        }
    }
}
