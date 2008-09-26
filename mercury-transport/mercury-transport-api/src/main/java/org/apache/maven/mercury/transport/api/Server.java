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
package org.apache.maven.mercury.transport.api;

import java.net.URL;
import java.util.Set;

import org.apache.maven.mercury.crypto.api.StreamObserverFactory;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;

public class Server
{
  private String                      id;
  
  private URL                         url;
  private Credentials                 serverCredentials;
  
  private URL                         proxy;
  private Credentials                 proxyCredentials;
  
  private boolean                     requireEncryption = false;
  private boolean                     requireTrustedServer = false;

  private Set<StreamObserverFactory>  writerStreamObserverFactories;
  private Set<StreamObserverFactory>  readerStreamObserverFactories;

  private Set<StreamVerifierFactory>  writerStreamVerifierFactories;
  private Set<StreamVerifierFactory>  readerStreamVerifierFactories;
  
  private String                      userAgent;

  public Server( String id, URL url )
  {
    this.url = url;
    this.id = id;
  }

  public Server( String id, URL url, boolean requireEncryption, boolean requireTrustedServer )
  {
    this( id, url );
    
    this.requireEncryption = requireEncryption;
    this.requireTrustedServer = requireTrustedServer;
  }

  public Server( String id, URL url, boolean requireEncryption, boolean requireTrustedServer, Credentials serverCredentials )
  {
    this( id, url, requireEncryption, requireTrustedServer );
    this.serverCredentials = serverCredentials;
  }

  public Server( String id, URL url, boolean requireEncryption, boolean requireTrustedServer, Credentials serverCredentials, URL proxy )
  {
    this( id, url, requireEncryption, requireTrustedServer, serverCredentials );
    this.proxy = proxy;
  }

  public Server( String id, URL url, boolean requireEncryption, boolean requireTrustedServer, Credentials serverCredentials, URL proxy, Credentials proxyCredentials )
  {
    this( id, url, requireEncryption, requireTrustedServer, serverCredentials, proxy );
    this.proxyCredentials = proxyCredentials;
  }

  public String getId()
  {
    return id;
  }

  public boolean hasUserAgent()
  {
    return userAgent != null;
  }

  public String getUserAgent()
  {
    return userAgent;
  }

  public void setUserAgent( String userAgent )
  {
    this.userAgent = userAgent;
  }

  public void setURL( URL url )
  {
    this.url = url;
  }

  public URL getURL()
  {
    return this.url;
  }

  public Credentials getServerCredentials()
  {
    return this.serverCredentials;
  }

  public void setServerCredentials( Credentials cred )
  {
    this.serverCredentials = cred;
  }

  public URL getProxy()
  {
    return this.proxy;
  }

  public boolean hasProxy()
  {
    return this.proxy != null;
  }
  
  public void setProxy( URL proxy )
  {
    this.proxy = proxy;
  }

  public Credentials getProxyCredentials()
  {
    return this.proxyCredentials;
  }

  public void setProxyCredentials( Credentials user )
  {
    this.proxyCredentials = user;
  }

  public boolean hasWriterStreamVerifierFactories()
  {
    return writerStreamVerifierFactories != null && writerStreamVerifierFactories.size() > 0;
  }

  public Set<StreamVerifierFactory> getWriterStreamVerifierFactories()
  {
    return writerStreamVerifierFactories;
  }

  public void setWriterStreamVerifierFactories( Set<StreamVerifierFactory> factories )
  {
    writerStreamVerifierFactories = factories;
  }

  public boolean hasReaderStreamVerifierFactories()
  {
    return readerStreamVerifierFactories != null && readerStreamVerifierFactories.size() > 0;
  }

  public Set<StreamVerifierFactory> getReaderStreamVerifierFactories()
  {
    return readerStreamVerifierFactories;
  }

  public void setReaderStreamVerifierFactories( Set<StreamVerifierFactory> factories )
  {
    readerStreamVerifierFactories = factories;
  }

  public boolean hasWriterStreamObserverFactories()
  {
    return writerStreamObserverFactories != null && writerStreamObserverFactories.size() > 0;
  }

  public Set<StreamObserverFactory> getWriterStreamObserverFactories()
  {
    return writerStreamObserverFactories;
  }

  public void setWriterStreamObserverFactories( Set<StreamObserverFactory> factories )
  {
    writerStreamObserverFactories = factories;
  }

  public boolean hasReaderStreamObserverFactories()
  {
    return readerStreamObserverFactories != null && readerStreamObserverFactories.size() > 0;
  }

  public Set<StreamObserverFactory> getReaderStreamObserverFactories()
  {
    return readerStreamObserverFactories;
  }

  public void setReaderStreamObserverFactories( Set<StreamObserverFactory> factories )
  {
    readerStreamObserverFactories = factories;
  }

  public boolean isRequireEncryption()
  {
    return requireEncryption;
  }
  
  public boolean isRequireTrustedServer()
  {
    return requireTrustedServer;
  }

}
