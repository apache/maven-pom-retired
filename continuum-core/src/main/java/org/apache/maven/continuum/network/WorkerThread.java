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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id: WorkerThread.java,v 1.1.1.1 2005/03/29 20:42:01 trygvis Exp $
 */
public class WorkerThread
    extends Thread
{
    private boolean running;

    private ServerSocket serverSocket;

    private ConnectionConsumer connectionConsumer;

    private Logger logger;

    public WorkerThread( ServerSocket serverSocket, ConnectionConsumer connectionConsumer, Logger logger )
    {
        this.serverSocket = serverSocket;

        this.connectionConsumer = connectionConsumer;

        this.logger = logger;
    }

    public void run()
    {
        running = true;

        while ( running )
        {
            Socket socket;

            try
            {
                socket = serverSocket.accept();
            }
            catch ( IOException ex )
            {
                if ( running )
                    getLogger().warn( "Exception while accepting socket.", ex );

                return;
            }

            //                getLogger().info( "Got connection from: " +
            // socket.getInetAddress() );

            InputStream input;

            OutputStream output;

            try
            {
                input = socket.getInputStream();

                output = socket.getOutputStream();
            }
            catch ( IOException ex )
            {
                getLogger().fatalError( "Exception while getting the input and output streams from the socket.", ex );
                continue;
            }

            try
            {
                connectionConsumer.consumeConnection( input, output );
            }
            catch ( IOException ex )
            {
                getLogger().fatalError( "Exception while consuming connection.", ex );
            }

            IOUtil.close( input );

            IOUtil.close( output );

            NetworkUtils.closeSocket( socket );
        }

//        getLogger().info( "Worker thread for port " + port + " exiting." );
    }

    public void shutdown()
    {
        running = false;
    }

    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    private Logger getLogger()
    {
        return logger;
    }
}
