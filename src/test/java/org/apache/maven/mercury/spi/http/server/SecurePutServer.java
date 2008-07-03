/**
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

package org.apache.maven.mercury.spi.http.server;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.servlet.PutFilter;

public class SecurePutServer extends SimplePutServer
{
    private File _base;
    
    public SecurePutServer() throws Exception
    {       
        SslSocketConnector connector = new SslSocketConnector();
        String keystore = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
        + "secureServer" + File.separator + "keystore";

        connector.setPort(0);
        connector.setKeystore(keystore);
        connector.setPassword("storepwd");
        connector.setKeyPassword("keypwd");

        setConnectors(new Connector[] { connector });        
        
        HandlerCollection handlers = new HandlerCollection();
        setHandler(handlers);
        
        Context context = new Context(handlers,"/maven2/repo");
        handlers.addHandler(new DefaultHandler());

        _base = File.createTempFile("securePutServer",null);
        _base.delete();
        _base.mkdir();
        _base.deleteOnExit();
        FilterHolder holder = context.addFilter(PutFilter.class, "/*", 0);
        holder.setInitParameter("delAllowed","true");
        context.addServlet(DefaultServlet.class,"/");
        context.setResourceBase(_base.getCanonicalPath());
    }
    
    /* (non-Javadoc)
     * @see org.apache.maven.mercury.spi.http.server.PutServer#getPutDir()
     */
    public File getPutDir ()
    {
        return _base;
    }
    
    public void destroy ()
    {
        super.destroy();
        File[] files = _base.listFiles();
        for (int i=0;files!=null && i>0; i++)
        {
            files[i].delete();
        }
        _base.delete();
    }
    
    public static void main(String[] args)
    throws Exception
    {
        SecurePutServer server = new SecurePutServer();
        server.start();
        server.join();
    }

}
