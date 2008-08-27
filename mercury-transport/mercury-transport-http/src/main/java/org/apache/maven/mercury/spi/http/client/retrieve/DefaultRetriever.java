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

package org.apache.maven.mercury.spi.http.client.retrieve;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.spi.http.client.DestinationRealmResolver;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.jetty.client.HttpClient;

public class DefaultRetriever implements Retriever
{
    private HttpClient _httpClient;
    private Set<Server> _servers = new HashSet<Server>();
    

    public DefaultRetriever()
        throws HttpClientException
    {
        // TODO take the default settings for now
        _httpClient = new HttpClient();
        _httpClient.setConnectorType( HttpClient.CONNECTOR_SELECT_CHANNEL );
        try
        {
            //TODO: What are all the reasons that the httpclient couldn't start up correctly?
            
            _httpClient.start();
        }
        catch ( Exception e )
        {
            throw new HttpClientException( null, "Unable to start http client.", e );
        }
    }

    public DefaultRetriever( HttpClient client )
        throws HttpClientException
    {
        // TODO take the default settings for now
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
                           
    public void setServers (Set<Server>servers)
    {
        _servers.clear();
        _servers.addAll(servers);
        _httpClient.setRealmResolver(new DestinationRealmResolver(_servers));
    }
    
    public Set<Server> getServers()
    {
        return _servers;
    }

    /**
     * Retrieve a set of artifacts and wait until all retrieved successfully
     * or an error occurs.
     * <p/>
     * Note: whilst this method is synchronous for the caller, the implementation
     * will be asynchronous so many artifacts are fetched in parallel.
     *
     * @param request
     * @return the list of errors, if any
     */
    public RetrievalResponse retrieve( RetrievalRequest request )
    {
        final RetrievalResponse[] response = new RetrievalResponse[]{null};

        retrieve( request, new RetrievalCallback()
        {
            public void onComplete( RetrievalResponse r )
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
     * Retrieve a set of artifacts without waiting for the results.
     * When all results have been obtained (or an error occurs) the
     * RetrievalResponse will be called.
     *
     * @param request
     * @param callback
     */
    public void retrieve( final RetrievalRequest request, final RetrievalCallback callback )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "No request" );
        }

        if ( callback == null )
        {
            throw new IllegalArgumentException( "No callback" );
        }

        final AtomicInteger count = new AtomicInteger( request.getBindings().size() );

        final List<RetrievalTarget> targets = new ArrayList<RetrievalTarget>( request.getBindings().size() );
        final DefaultRetrievalResponse response = new DefaultRetrievalResponse();

        Binding[] bindings = new Binding[request.getBindings().size()];
        request.getBindings().toArray( bindings );

        for ( int i = 0; i < bindings.length && count.get() > 0; i++ )
        {
            final Binding binding = bindings[i];
            
            RetrievalTarget target = null;
            try
            {
                Server server = resolveServer(binding);
                Set<StreamObserver> observers = createStreamObservers(server);
                
                target = new RetrievalTarget( server, DefaultRetriever.this, binding, request.getValidators(), observers )
                {
                    public void onComplete()
                    {
                        //got the file, check the checksum
                        boolean checksumOK = false;
                        try
                        {
                          checksumOK = verifyChecksum();
                          if ( !checksumOK )
                          {
                              response.add( new HttpClientException( binding, "Checksum failed") );
                          }
                        }
                        catch( StreamVerifierException e )
                        {
                          response.add( new HttpClientException( binding, e.getMessage()) );
                        }

                        //if the file checksum is ok, then apply the validators
                        if ( checksumOK )
                        {
                            List<String> validateErrors = new ArrayList<String>();
                            if ( !validate( validateErrors ) )
                            {
                                for ( String s : validateErrors )
                                {
                                    response.add( new HttpClientException( binding, s ) );
                                }
                            }
                        }

                        if ( DefaultRetriever.this.isComplete( count, request, response, targets ) )
                        {
                            callback.onComplete( response );
                        }
                    }

                    public void onError( HttpClientException exception )
                    {
                        response.add( exception );
                        if ( DefaultRetriever.this.isComplete( count, request, response, targets ) )
                        {
                            callback.onComplete( response );
                        }
                    }
                };

                targets.add( target );
            }
            catch ( Exception e )
            {
                response.add( new HttpClientException( binding, e ) );
                if ( isComplete( count, request, response, targets ) )
                {
                    callback.onComplete( response );
                }
            }
        }

        for ( final RetrievalTarget target : targets )
        {
            target.retrieve(); //go get the remote file
        }
    }

    private boolean isComplete( AtomicInteger count,
                                RetrievalRequest request,
                                RetrievalResponse response,
                                List<RetrievalTarget> targets )
    {
        boolean completor = count.decrementAndGet() == 0;
        
        if ( !completor && request.isFailFast() && response.getExceptions().size() > 0 )
        {
            completor = count.getAndSet( 0 ) > 0;
        }

        if ( completor )
        {
            if ( response.getExceptions().size() == 0 )
            {
                for ( RetrievalTarget t : targets )
                {
                    if ( t != null )
                    {
                        t.move();
                    }
                }
            }

            for ( RetrievalTarget t : targets )
            {
                t.cleanup();
            }
            return true;
        }

        return false;
    }


    /**
     * Get the jetty async client
     *
     * @return
     */
    public HttpClient getHttpClient()
    {
        return _httpClient;
    }
    
    private Server resolveServer (Binding binding)
    throws MalformedURLException
    {
        if (binding.getRemoteResource() == null)
        return null;
        
        URL bindingURL = binding.getRemoteResource();
        if (_servers == null)
            return null;
        
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
        if (server != null && server.hasReaderStreamVerifierFactories() )
        {
            Set<StreamVerifierFactory> factories = server.getReaderStreamVerifierFactories();
            for( StreamVerifierFactory f:factories )
            {
                observers.add( f.newInstance() );
            }
        }
        return observers;
    }

}
