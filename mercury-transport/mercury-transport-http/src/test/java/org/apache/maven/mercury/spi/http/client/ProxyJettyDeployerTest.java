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

import java.net.URL;
import java.util.HashSet;

import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.spi.http.server.AuthenticatingProxyServer;
import org.apache.maven.mercury.spi.http.server.AuthenticatingPutServer;
import org.apache.maven.mercury.transport.api.Credentials;

public class ProxyJettyDeployerTest extends JettyDeployerTest
{

    
    AuthenticatingProxyServer _proxyServer;
    String _proxyPort;

    
    public ProxyJettyDeployerTest() throws Exception
    {
        super();
    }
    protected void setUp() throws Exception
    {        
        setUpFiles();
        //Set up a proxy server (which requires authentication)
        _proxyServer = new AuthenticatingProxyServer();
        _proxyServer.start();
        _proxyPort = String.valueOf(_proxyServer.getPort());
        
        _deployer = new DefaultDeployer();
        
        //set up a target server (which requires authentication)
        _putServer = new AuthenticatingPutServer();
        _putServer.start();
        _port = String.valueOf(_putServer.getPort());
        setUpServerType();
    }

  
    
    protected void setUpServerType () throws Exception
    {
        HashSet<org.apache.maven.mercury.transport.api.Server> remoteServerTypes = new HashSet<org.apache.maven.mercury.transport.api.Server>();
        remoteServerType = new org.apache.maven.mercury.transport.api.Server( "test", 
                new URL(_HOST_FRAGMENT+_port), 
                false, 
                false, 
                new Credentials(((AuthenticatingPutServer)_putServer).getUsername(), ((AuthenticatingPutServer)_putServer).getPassword()),
                new URL(_HOST_FRAGMENT+_proxyPort),
                new Credentials(_proxyServer.getUsername(), _proxyServer.getPassword()));
        factories = new HashSet<StreamVerifierFactory>();       
        remoteServerTypes.add(remoteServerType);
        _deployer.setServers(remoteServerTypes);
    }
    
   

    protected void tearDown() throws Exception
    {
        _proxyServer.stop();
        super.tearDown();
    }
}
