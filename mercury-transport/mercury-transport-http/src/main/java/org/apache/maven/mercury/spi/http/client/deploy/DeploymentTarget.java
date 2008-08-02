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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.StreamObserver;
import org.apache.maven.mercury.transport.api.Verifier;
import org.mortbay.jetty.client.HttpClient;

public abstract class DeploymentTarget
{
    public static final String __DIGEST_SUFFIX = ".sha1";


    protected HttpClient _httpClient;
    protected String _batchId;
    protected Binding _binding;
    protected Set<Validator> _validators;
    protected TargetState _targetState;
    protected TargetState _checksumState;
    protected HttpClientException _exception;
    protected String _remoteJettyUrl;
    protected Set<StreamObserver> _observers = new HashSet<StreamObserver>();
    protected List<Verifier> _verifiers = new ArrayList<Verifier>();
    protected int _index = -1; 

    
    public abstract void onComplete();

    public abstract void onError( HttpClientException exception );


    public class TargetState
    {
        public static final int __START_STATE = 1;
        public static final int __REQUESTED_STATE = 2;
        public static final int __READY_STATE = 3;

        private int _state;
        private Exception _exception;

        public TargetState()
        {
            _state = __START_STATE;
        }

        public synchronized void ready()
        {
            setState( __READY_STATE );
        }

        public synchronized void ready( Exception e )
        {
            setState( __READY_STATE );
            _exception = e;
        }

        public synchronized void requested()
        {
            setState( __REQUESTED_STATE );
        }

        public synchronized boolean isStart()
        {
            return _state == __START_STATE;
        }

        public synchronized boolean isRequested()
        {
            return _state == __REQUESTED_STATE;
        }

        public synchronized boolean isError()
        {
            return _exception != null;
        }

        public boolean isReady()
        {
            return _state == __READY_STATE;
        }

        public synchronized void setState( int status )
        {
            _state = status;
        }

        public synchronized int getState()
        {
            return _state;
        }

        public synchronized Exception getException()
        {
            return _exception;
        }
    }

    public DeploymentTarget( HttpClient client, String batchId, Binding binding, Set<Validator> validators, Set<StreamObserver> observers )
    {
        _httpClient = client;
        _batchId = batchId;
        _binding = binding;
        _validators = validators;
        
        for (StreamObserver o:observers)
        {
            if (Verifier.class.isAssignableFrom(o.getClass()))
                _verifiers.add((Verifier)o);
            _observers.add(o);
        }
      
        if ( _binding == null )
        {
            throw new IllegalArgumentException( "Nothing to deploy - null binding" );
        }
        
        if ( _binding.isFile() && (_binding.getLocalFile() == null || !_binding.getLocalFile().exists()) )
        {
            throw new IllegalArgumentException( "Nothing to deploy - local file not found: " + _binding.getLocalFile() );
        }
        if( _binding.isInMemory() && _binding.getLocalInputStream() == null )
        {
            throw new IllegalArgumentException( "Nothing to deploy - inMemory binding with null stream" );
        }
        _targetState = new TargetState();
        _checksumState = new TargetState();
    }


    public void deploy()
    {
        updateState( null );
    }

    private synchronized void updateState( Throwable t )
    {
        if ( t != null && _exception == null )
        {
            _exception = ( t instanceof HttpClientException ? (HttpClientException) t : new HttpClientException( _binding, t ) );
        }

        if (_exception != null)
        {
            onError(_exception);
        }
        else
        {
            //if the target file can be uploaded, then upload it, calculating checksums on the fly as necessary
            if ( _targetState.isStart() )
            {
                deployLocalFile();
            }

            //Upload the checksums
            if ( _targetState.isReady() && (++_index) < _verifiers.size() )
            {
                deployChecksumFile();
            }

            if ( _targetState.isReady() && _checksumState.isReady() )
            {
                onComplete();
            }
        }
    }

    private void deployLocalFile()
    {
        FilePutExchange exchange = new FilePutExchange( _batchId, _binding, _binding.getLocalFile(), _observers, _httpClient )
        {
            public void onFileComplete( String url, File localFile )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                _targetState.ready();
                updateState( null );
            }

            public void onFileError( String url, Exception e )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                _targetState.ready( e );
                updateState( e );
            }
        };
        _targetState.requested();
        exchange.send();
    }


    private void deployChecksumFile()
    {
        Binding binding = _binding;
        File file = null;
        Verifier v =  _verifiers.get(_index);

        //No local checksum file, so make a temporary one using the checksum we 
        //calculated as we uploaded the file
        try
        {
            URL url = _binding.getRemoteResource();
            if (url != null)
            {
                url = new URL( url.toString() + v.getExtension());
            }
      
            String localFileName = getFileName(url);
            
            file = File.createTempFile( localFileName, ".tmp" );
            file.deleteOnExit();
            OutputStreamWriter fw = new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );
            fw.write( v.getSignature() );
            fw.close();
            binding = new Binding(url, file);
        }
        catch ( Exception e )
        {
            _checksumState.ready( e );
        }


        //upload the checksum file
        Set<StreamObserver> emptySet = Collections.emptySet();
        FilePutExchange exchange = new FilePutExchange( _batchId, binding, file, emptySet, _httpClient )
        {
            public void onFileComplete( String url, File localFile )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                if (_index == (_verifiers.size() - 1))
                    _checksumState.ready();
                updateState( null );
            }

            public void onFileError( String url, Exception e )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                if (_index == (_verifiers.size() - 1))
                    _checksumState.ready( e );
                updateState( e );
            }
        };
        _checksumState.requested();
        exchange.send();
    }

    public boolean isRemoteJetty()
    {
        return _remoteJettyUrl != null;
    }

    public String getRemoteJettyUrl()
    {
        return _remoteJettyUrl;
    }

    public synchronized boolean isComplete()
    {
        return ( _checksumState.isReady() && _targetState.isReady() );
    }
    
    public String getFileName (URL url)
    {
        if (url==null)
            return "";
        String localFileName = url.getFile();
        int i = localFileName.indexOf('?');
        if (i > 0)
            localFileName = localFileName.substring(0, i);
        if (localFileName.endsWith("/"))
            localFileName = localFileName.substring(0, localFileName.length()-1);
        i = localFileName.lastIndexOf('/');
        if (i >= 0)
            localFileName = localFileName.substring(i+1);
        return localFileName;
    }

    public String toString()
    {
        return "DeploymentTarget:" + _binding.getRemoteResource() + ":" + _targetState + ":" + _checksumState + ":" + isComplete();
    }
}
