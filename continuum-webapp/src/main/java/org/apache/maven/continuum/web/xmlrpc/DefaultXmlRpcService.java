package org.apache.maven.continuum.web.xmlrpc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.xmlrpc.ContinuumXmlRpc;
import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.xmlrpc.XmlRpcServer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultXmlRpcService
    extends AbstractLogEnabled
    implements XmlRpcService, Startable
{
    /**
     * @plexus.requirement
     */
    private XmlRpcServer xmlRpcServer;

    /**
     * @plexus.requirement
     */
    private ContinuumXmlRpc handler;

    /**
     * port activity flag
     *
     * @plexus.configuration default-value="8000"
     */
    int port;

    /**
     * @plexus.configuration default-value="continuum"
     */
    private String handlerName;

    public void start()
        throws StartingException
    {
        getLogger().info( "Starting XML-RPC service." );

        try
        {
            xmlRpcServer.addListener( null, port, false );

            xmlRpcServer.startListener( null, port );

            getLogger().info( "Adding XML-RPC handler for role '" + handler.ROLE + " to name '" + handlerName + "'." );

            xmlRpcServer.addHandler( null, handlerName, port, handler );
        }
        catch ( XmlRpcException e )
        {
            throw new StartingException( "Error while starting XML-RPC server on port " + port + ".", e );
        }
    }

    public void stop()
        throws StoppingException
    {
        getLogger().info( "Stopping XML-RPC service." );

        if ( port == -1 )
        {
            return;
        }

        try
        {
            xmlRpcServer.removeListener( null, port );
        }
        catch ( XmlRpcException e )
        {
            getLogger().error( "Error while stopping the XML-RPC server.", e );
        }
    }
}
