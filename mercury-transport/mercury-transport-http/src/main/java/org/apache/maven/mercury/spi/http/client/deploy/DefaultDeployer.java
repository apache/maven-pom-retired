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

package org.apache.maven.mercury.spi.http.client.deploy;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.spi.http.client.FileExchange;
import org.apache.maven.mercury.spi.http.client.HandshakeExchange;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.client.HttpClient;

/**
 * JettyDeployer
 * <p/>
 * Implementation of Deployer using Jetty async HttpClient.
 */
public class DefaultDeployer implements Deployer
{
    private HttpClient _httpClient;
    private BatchIdGenerator _idGenerator;
    private Set<Server> _servers = new HashSet<Server>();

    public DefaultDeployer()
        throws HttpClientException
    {
        _idGenerator = new RandomBatchIdGenerator();
        _httpClient = new HttpClient();
        _httpClient.setConnectorType( HttpClient.CONNECTOR_SELECT_CHANNEL );
        _httpClient.registerListener( "org.mortbay.jetty.client.webdav.WebdavListener");
        try
        {
            _httpClient.start();
        }
        catch ( Exception e )
        {
            throw new HttpClientException( null, "unable to start http client", e );
        }
    }

    public DefaultDeployer( HttpClient client, BatchIdGenerator idGenerator )
        throws HttpClientException
    {
        _idGenerator = idGenerator;
        if ( _idGenerator == null )
        {
            throw new HttpClientException( null, "no id generator supplied" );
        }

        _httpClient = client;
        try
        {
            if ( _httpClient.isStarted() )
            {
                _httpClient.start();
            }
        }
        catch ( Exception e )
        {
            throw new HttpClientException( null, "unable to start http client", e );
        }
    }

    public BatchIdGenerator getBatchIdGenerator()
    {
        return _idGenerator;
    }

    public HttpClient getHttpClient()
    {
        return _httpClient;
    }
    
    public void setServers (Set<Server>servers)
    {
        _servers.clear();
        _servers.addAll(servers);
    }
    
    public Set<Server> getServers()
    {
        return _servers;
    }

    /**
     * Deploy a set files synchronously. This call will return when either all
     * files have been successfully deployed, or one or more failures have
     * occurred, depending on the failFast setting of the DeployRequest.
     *
     * @see org.apache.maven.mercury.spi.http.client.deploy.Deployer#deploy(org.apache.maven.mercury.spi.http.client.deploy.DeployRequest)
     */
    public DeployResponse deploy( DeployRequest request )
    {
        final DeployResponse[] response = new DeployResponse[]{null};

        deploy( request, new DeployCallback()
        {
            public void onComplete( DeployResponse r )
            {
                synchronized ( response )
                {
                    response[0] = r;
                    response.notify();
                }
            }
        } );

        synchronized ( response )
        {
            try
            {
                while ( response[0] == null )
                {
                    response.wait();
                }
            }
            catch ( InterruptedException e )
            {
                return null;
            }
            return response[0];
        }
    }

    /**
     * Deploy a set of files, returning immediately. The callback will be called when
     * all the files have been deployed or one or more errors occur (depends on the FailFast
     * setting of the DeployRequest).
     *
     * @see org.apache.maven.mercury.spi.http.client.deploy.Deployer#deploy(org.apache.maven.mercury.spi.http.client.deploy.DeployRequest, org.apache.maven.mercury.spi.http.client.deploy.DeployCallback)
     */
    public void deploy( final DeployRequest request, final DeployCallback callback )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "No request" );
        }

        if ( callback == null )
        {
            throw new IllegalArgumentException( "No callback" );
        }

        final String batchId = _idGenerator.getId();
        final AtomicInteger count = new AtomicInteger( request.getBindings().size() );
        final List<DeploymentTarget> targets = new ArrayList<DeploymentTarget>( request.getBindings().size() );
        final DefaultDeployResponse response = new DefaultDeployResponse();
        final Set<String> remoteHandshakeUrls = new HashSet<String>();

        Binding[] bindings = new Binding[request.getBindings().size()];
        request.getBindings().toArray( bindings );
        for ( int i = 0; i < bindings.length && count.get() > 0; i++ )
        {
            final Binding binding = bindings[i];
            DeploymentTarget target = null;
            try
            {
                Server server = resolveServer(binding);
                Set<StreamObserver> observers = createStreamObservers(server);
                target = new DeploymentTarget( _httpClient, batchId, binding, request.getValidators(), observers )
                {
                    public void onComplete()
                    {
                        if ( getRemoteJettyUrl() != null )
                        {
                            remoteHandshakeUrls.add( getRemoteJettyUrl() );
                        }
                        //uploaded the file - have we uploaded all of them?
                        checkComplete( callback, batchId, count, request, response, remoteHandshakeUrls );
                    }

                    public void onError( HttpClientException exception )
                    {
                        if ( getRemoteJettyUrl() != null )
                        {
                            remoteHandshakeUrls.add( getRemoteJettyUrl() );
                        }
                        response.add( exception );
                        checkComplete( callback, batchId, count, request, response, remoteHandshakeUrls );
                    }
                };
                targets.add( target );
            }
            catch ( Exception e )
            {
                response.add( new HttpClientException( binding, e ) );
                checkComplete( callback, batchId, count, request, response, remoteHandshakeUrls );
            }
        }

        for ( final DeploymentTarget target : targets )
        {
            target.deploy(); //upload file
        }
    }

    private synchronized void checkComplete( final DeployCallback callback,
                                             String batchId,
                                             AtomicInteger count,
                                             DeployRequest request,
                                             DeployResponse response,
                                             Set<String> remoteHandshakeUrls )
    {
        int x = count.decrementAndGet();
        boolean completor = x == 0;
        if ( !completor && request.isFailFast() && response.getExceptions().size() > 0 )
        {
            completor = count.getAndSet( 0 ) > 0;
        }

        if ( completor )
        {
            commit( callback, response, batchId, remoteHandshakeUrls );
        }
    }

    /**
     * Send message to remote server (if Jetty) to indicate all
     * files uploaded should now be commited or discarded if there were exceptions.
     *
     * @param batchId
     */
    private void commit( final DeployCallback callback,
                         final DeployResponse response,
                         final String batchId,
                         final Set<String> remoteHandshakeUrls )
    {
        if ( remoteHandshakeUrls.isEmpty() )
        {
            callback.onComplete( response );
        }
        else
        {
            final AtomicInteger count = new AtomicInteger( remoteHandshakeUrls.size() );
            Map<String, String> headers = new HashMap<String, String>();
            //if no errors, then commit, otherwise send a discard message
            if ( response.getExceptions().isEmpty() )
            {
                headers.put( FileExchange.__BATCH_COMMIT_HEADER, batchId );
            }
            else
            {
                headers.put( FileExchange.__BATCH_DISCARD_HEADER, batchId );
            }
            for ( final String remoteUrl : remoteHandshakeUrls )
            {
                HandshakeExchange exchange = new HandshakeExchange( _httpClient, HttpMethods.POST, remoteUrl, headers )
                {
                    public void onHandshakeComplete( String url )
                    {
                        checkHandshakeComplete( callback, response, count );

                    }

                    public void onHandshakeError( String url, Exception e )
                    {
                        response.getExceptions().add( new HttpClientException( null, e ) );
                        checkHandshakeComplete( callback, response, count );
                    }
                };
            }
        }
    }


    private void checkHandshakeComplete( final DeployCallback callback,
                                         final DeployResponse response,
                                         AtomicInteger count )
    {
        boolean completor = count.decrementAndGet() == 0;
        if ( completor )
        {
            callback.onComplete( response );
        }
    }
 
    private Server resolveServer (Binding binding)
    throws MalformedURLException
    {
        if (binding.getRemoteResource() == null)
        return null;
        
        URL bindingURL = binding.getRemoteResource();
        Iterator<Server> itor = _servers.iterator();
        Server server = null;
        while(itor.hasNext() && server==null)
        {
            Server s = itor.next();
            if (bindingURL.getProtocol().equalsIgnoreCase(s.getURL().getProtocol()) 
                    && bindingURL.getHost().equalsIgnoreCase(s.getURL().getHost())
                    && bindingURL.getPort() == s.getURL().getPort())
                server = s;
        }
        return server;
    }
    
    private Set<StreamObserver> createStreamObservers (Server server)
    throws StreamVerifierException
    {
        HashSet<StreamObserver> observers = new HashSet<StreamObserver>();
        Set<StreamVerifierFactory> factories = server.getStreamObserverFactories();
        for (StreamVerifierFactory f:factories)
        {
            observers.add( f.newInstance() );
        }
        return observers;
    }

}
