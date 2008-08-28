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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.security.B64Code;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.servlet.ProxyServlet;
import org.mortbay.util.StringUtil;

public class AuthenticatingProxyServer extends Server
{
    public static final String __username = "foo";
    public static final String __password = "banana";
    public static final String __role = "fooproxy";
    protected Context  _context;
    
    
    
    
    public static class AuthProxyServlet extends ProxyServlet 
    {
        public void service(ServletRequest request, ServletResponse response)
                throws ServletException, IOException
        {
            String proxyAuth = ((HttpServletRequest)request).getHeader("Proxy-Authorization");
            if (proxyAuth!=null)
            {

                if (proxyAuth.startsWith("basic "))
                    proxyAuth = proxyAuth.substring(6);
             
                proxyAuth = B64Code.decode(proxyAuth,StringUtil.__ISO_8859_1);
                int sep = proxyAuth.indexOf(":");
                String username = proxyAuth.substring(0,sep);
                String password = proxyAuth.substring(sep+1);
                
                if (__username.equalsIgnoreCase(username) && __password.equalsIgnoreCase(password))
                    super.service(request, response);
                else
                    ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            else
            {
                ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }    
    }
    
    public AuthenticatingProxyServer()
    throws Exception
    {
        super( 0 );

        HandlerCollection handlers = new HandlerCollection();
        setHandler( handlers );

        _context = new Context( handlers, "/" );
        handlers.addHandler( new DefaultHandler() );

        _context.addServlet( AuthProxyServlet.class, "/" );
        
        HashUserRealm realm = new HashUserRealm();
        realm.put (__username, __password);
        realm.addUserToRole(__username, __role);
        realm.setName("proxyrealm");
    }
    
    public int getPort()
    {
        return getConnectors()[0].getLocalPort();
    }
    
    public String getUsername()
    {
        return __username;
    }
    
    public String getPassword()
    {
        return __password;
    }
}
