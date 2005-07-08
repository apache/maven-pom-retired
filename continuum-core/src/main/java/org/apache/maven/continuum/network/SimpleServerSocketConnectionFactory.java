package org.apache.maven.continuum.network;

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

import java.io.IOException;
import java.net.ServerSocket;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SimpleServerSocketConnectionFactory
    extends AbstractLogEnabled
    implements ConnectionFactory, Initializable, Startable
{
    /**
     * @default 0
     */
    private int port;

    /**
     * @default 50
     */
    private int backlog;

    private ConnectionConsumer consumer;

    private WorkerThread thread;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        if ( port <= 0 )
            throw new InitializationException( "The port must be bigger than 0." );

        if ( port >= 65536 )
            throw new InitializationException( "The port must be lesser than 65536." );

        if ( backlog < 0 )
            throw new InitializationException( "The valud of the backlog element must be bigger than 0." );

        if ( consumer == null )
            throw new InitializationException( "There is no connection consumer configured." );
    }

    public void start()
        throws StartingException
    {
        getLogger().info( "Starting socket listener on port " + port );

        ServerSocket serverSocket;

        try
        {
            serverSocket = new ServerSocket( port, backlog );
        }
        catch ( IOException ex )
        {
            throw new StartingException( "Could not create a server socket.", ex );
        }

        thread = new WorkerThread( serverSocket, consumer, getLogger() );

        thread.start();
    }

    public void stop()
    {
        getLogger().info( "Stopping socket listener on port " + port );

        thread.shutdown();

        if ( thread.getServerSocket() != null )
        {
            try
            {
                thread.getServerSocket().close();
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }
    }
}
