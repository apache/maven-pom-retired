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

package org.apache.maven.mercury.spi.http.client;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpDestination;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.client.security.ProxyAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SecureSender
 *
 * Initiates a HttpExchange with the remote server, taking into account any proxy
 * authentication required. Any non-proxy authentication required will be taken
 * care of by the DestinationRealmResolver that is set on the HttpClient used
 * by this class.
 * 
 * TODO think of a better name for this class.
 */
public class SecureSender
{
  private static final Logger _log = LoggerFactory.getLogger( SecureSender.class );

  public static void send (Server server, HttpClient httpClient, HttpExchange exchange)
    throws Exception
    {
        if (server != null && server.hasProxy() && (server.getProxy() != null))
        {
            String s = exchange.getURI();
            URI uri = new URI(s);
            boolean ssl = "https".equalsIgnoreCase(uri.getScheme());
            URL proxy = server.getProxy();
            
            String host = proxy.getHost();
            int port = proxy.getPort();
            boolean proxySsl = "https".equalsIgnoreCase(proxy.getProtocol());
            if (port < 0)
            {
                port = proxySsl?443:80;
            }
            InetSocketAddress proxyAddress = new InetSocketAddress(host,port);
            HttpDestination destination = httpClient.getDestination(exchange.getAddress(), ssl);  
            if( _log.isDebugEnabled() )
              _log.debug("Matched destination "+destination);

            destination.setProxy(proxyAddress);
            if( _log.isDebugEnabled() )
              _log.debug("Set proxy "+host+":"+port+" on destination");
_log.info("Set proxy "+host+":"+port+" on destination");
System.out.println("Set proxy "+host+":"+port+" on destination");
            
            //set up authentication for the proxy
            Credentials proxyCredentials = server.getProxyCredentials();

            if (proxyCredentials != null)
            {
                if (proxyCredentials.isCertificate())
                    throw new UnsupportedOperationException ("Proxy credential not supported");
                else
                {
                    destination.setProxyAuthentication(new ProxyAuthorization (proxyCredentials.getUser(), proxyCredentials.getPass()));
                    if( _log.isDebugEnabled() )
                      _log.debug( "Set proxy authentication: "+proxyCredentials.getUser()+":"+proxyCredentials.getPass());
                }
            }
            destination.send(exchange); 
        }
        else
        { 
            httpClient.send(exchange);
        } 
    }

}
