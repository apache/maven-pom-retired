/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.maven.mercury.spi.http.server;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;

public class HttpTestServer
    extends Server
{
    public HttpTestServer( File base, String remotePathFragment )
        throws Exception
    {
        super( 0 );

        System.out.println( base );
        
        if ( !base.exists() )
        {
            throw new IllegalArgumentException( "Specified base directory does not exist: " + base.getCanonicalPath() );
        }
        
        HandlerCollection handlers = new HandlerCollection();
        setHandler( handlers );

        Context context = new Context( handlers, remotePathFragment );
        handlers.addHandler( new DefaultHandler() );

        context.addServlet( DefaultServlet.class, "/" );               
        context.setResourceBase( base.getCanonicalPath() );
    }

    public int getPort()
    {
        return getConnectors()[0].getLocalPort();
    }
}
