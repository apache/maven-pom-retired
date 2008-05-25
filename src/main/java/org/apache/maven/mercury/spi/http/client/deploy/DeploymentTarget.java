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

import org.apache.maven.mercury.spi.http.client.Binding;
import org.apache.maven.mercury.spi.http.client.MercuryException;
import org.apache.maven.mercury.spi.http.validate.Validator;
import org.mortbay.jetty.client.HttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

public abstract class DeploymentTarget
{
    public static final String __DIGEST_SUFFIX = ".sha1";


    private HttpClient _httpClient;
    private String _batchId;
    private Binding _binding;
    private Set<Validator> _validators;
    private File _localChecksumFile;
    private String _calculatedChecksum;
    private TargetState _targetState;
    private TargetState _checksumState;
    private MercuryException _exception;
    private String _remoteJettyUrl;


    public abstract void onComplete();

    public abstract void onError( MercuryException exception );


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

    public DeploymentTarget( HttpClient client, String batchId, Binding binding, Set<Validator> validators )
    {
        _httpClient = client;
        _batchId = batchId;
        _binding = binding;
        _validators = validators;
        if ( _binding == null || _binding.getLocalFile() == null || !_binding.getLocalFile().exists() )
        {
            throw new IllegalArgumentException( "No local file to deploy" );
        }
        _localChecksumFile = new File( _binding.getLocalFile().getParentFile(),
            _binding.getLocalFile().getName() + __DIGEST_SUFFIX );
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
            _exception = ( t instanceof MercuryException ? (MercuryException) t : new MercuryException( _binding, t ) );
        }

        //if the target file can be fetched then get it
        if ( _targetState.isStart() )
        {
            System.err.println( "Starting deployment of " + _binding.getLocalFile().getName() );
            deployLocalFile( _localChecksumFile.exists() );
        }

        //if there is a local checksum file, then get it
        if ( _checksumState.isStart() && _localChecksumFile.exists() )
        {
            System.err.println( "Starting deployment of checksum file for " + _binding.getLocalFile().getName() );
            deployChecksumFile();
        }

        //if the local checksum file doesn't exist then only upload it after the target file has been sent,
        //as we need to calculate the checksum as we send it
        if ( _targetState.isReady() && _checksumState.isStart() && !_localChecksumFile.exists() )
        {
            System.err.println( "Calculated checksum ready, starting deployment" );
            deployChecksumFile();
        }

        if ( _targetState.isReady() && _checksumState.isReady() )
        {
            if ( _exception == null )
            {
                onComplete();
            }
            else
            {
                onError( _exception );
            }
        }
    }

    private void deployLocalFile( boolean checksumExists )
    {
        FilePutExchange exchange = new FilePutExchange( _batchId, _binding, _binding.getLocalFile(), true, _httpClient )
        {
            public void onFileComplete( String url, File localFile,
                                        String digest )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                _calculatedChecksum = digest;
                _targetState.ready();
                System.err.println( "File complete " + url );
                updateState( null );
            }

            public void onFileError( String url, Exception e )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                _targetState.ready( e );
                System.err.println( "File complete " + url );
                updateState( e );
            }
        };
        _targetState.requested();
        exchange.send();
    }


    private void deployChecksumFile()
    {
        Binding binding = _binding;
        File file = _localChecksumFile;
        if ( !_localChecksumFile.exists() && _calculatedChecksum != null )
        {
            //No local checksum file, so make a temporary one using the checksum we 
            //calculated as we uploaded the file
            try
            {
                binding = new Binding();
                binding.setRemoteUrl( _binding.getRemoteUrl() + __DIGEST_SUFFIX );
                file = File.createTempFile( _binding.getLocalFile().getName() + __DIGEST_SUFFIX, ".tmp" );
                OutputStreamWriter fw = new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );
                fw.write( _calculatedChecksum );
                fw.close();
                binding.setLocalFile( file );
            }
            catch ( Exception e )
            {
                _checksumState.ready( e );
            }
        }
        else
        {
            binding = new Binding();
            binding.setRemoteUrl( _binding.getRemoteUrl() + __DIGEST_SUFFIX );
            binding.setLocalFile( _localChecksumFile );
        }

        //upload the checksum file
        FilePutExchange exchange = new FilePutExchange( _batchId, binding, file, false, _httpClient )
        {
            public void onFileComplete( String url, File localFile, String digest )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                _checksumState.ready();
                System.err.println( "Checksum file complete: " + url );
                updateState( null );
            }

            public void onFileError( String url, Exception e )
            {
                DeploymentTarget.this._remoteJettyUrl = getRemoteJettyUrl();
                _checksumState.ready( e );
                System.err.println( "Checksum file error: " + url );
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

    public String toString()
    {
        return "DeploymentTarget:" + _binding.getRemoteUrl() + ":" + _targetState + ":" + _checksumState + ":" + isComplete();
    }
}
