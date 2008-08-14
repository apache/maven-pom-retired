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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.jetty.client.HttpDestination;
import org.mortbay.jetty.client.security.Realm;
import org.mortbay.jetty.client.security.RealmResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DestinationRealmResolver
 *
 * Resolve a security realm based on the Server instance
 * which matches an async Destination.
 */
public class DestinationRealmResolver implements RealmResolver
{
    private static final Logger log = LoggerFactory.getLogger(DestinationRealmResolver.class);
    
    protected Set<Server> _servers = new HashSet<Server>();
    
    
    
    public DestinationRealmResolver (Set<Server> servers)
    {
        _servers.addAll(servers);
    }

    public Realm getRealm(String name, HttpDestination dest, String pathSpec)
            throws IOException
    {
       InetSocketAddress address = dest.getAddress();
       boolean secure = dest.isSecure();
       
       //get a username and password appropriate for the destination. Usernames and passwords are
       //associated with Server Credential objects.
       Server server = null;
       Iterator<Server> itor = _servers.iterator();
       while (server==null && itor.hasNext())
       {
           Server s = itor.next();
           if (s.getURL() != null)
           {
               String protocol = s.getURL().getProtocol();
               String host = s.getURL().getHost();
               int port = s.getURL().getPort();
               
               //destination.host == server.host &&
               //destination.port == server.port &&
               //destination.isSecure && server.protocol == https || !destination.isSecure && server.protocol == http
               if (((dest.isSecure() && "https".equalsIgnoreCase(protocol)) || (!dest.isSecure() && "http".equalsIgnoreCase(protocol)))
                   &&
                   (address.getPort() == port)
                   &&
                   (address.getHostName().equalsIgnoreCase(host)))
               {
                   server = s;
                   if (log.isDebugEnabled())
                       log.debug("Matched server "+address.getHostName()+":"+address.getPort());
               }
           }
       }
       
       if (server == null || server.getServerCredentials() == null)
       {
           if (log.isDebugEnabled())
               log.debug("No server matching "+address.getHostName()+":"+address.getPort()+" or no credentials");
           return null;
       }
       
       if (server.getServerCredentials().isCertificate())
           throw new UnsupportedOperationException("Certificate not supported");
       
       final Server realmServer = server;
       return new Realm ()
       {
           public String getCredentials()
           {
               return realmServer.getServerCredentials().getPass();
           }

           public String getId()
           {
               return realmServer.getId();
           }

           public String getPrincipal()
           {
               return realmServer.getServerCredentials().getUser();
           }

       };
    }

}
