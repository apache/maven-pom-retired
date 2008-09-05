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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.spi.http.client.FileExchange;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.ObservableInputStream;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.io.Buffer;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FilePutExchange
 * <p/>
 * Asynchronously PUT a file to a remote server. The file that is being uploaded can also
 * have it's SHA-1 digest calculated as it is being streamed up.
 */
public abstract class FilePutExchange extends FileExchange
{
    private static final int __readLimit = 1024;
    private static final Logger log = LoggerFactory.getLogger(FilePutExchange.class);
    private String _batchId;
    private InputStream _inputStream;
    private String _remoteRepoUrl;
    private String _remoteBatchId;
    private Set<StreamObserver> _observers = new HashSet<StreamObserver>();
    
    public abstract void onFileComplete( String url, File localFile );

    public abstract void onFileError( String url, Exception e );


    public FilePutExchange( Server server, String batchId, Binding binding, File localFile, Set<StreamObserver> observers, HttpClient client )
    {
        super( server, binding, localFile, client );
        _observers.addAll(observers);
        _batchId = batchId;
    }


    /**
     * Start the upload. Ensure that the id of the mercury is set as a request header
     * so all files part of the same mercury can be identified as an atomic unit.
     */
    public void send()
    {
        try
        {
            setMethod( HttpMethods.PUT );
            setRequestHeader( "Content-Type", "application/octet-stream" );
            if (_binding.isFile())
            {
                setRequestHeader( "Content-Length", String.valueOf( _localFile.length() ) );
                if (log.isDebugEnabled())
                    log.debug("PUT of "+_localFile.length()+" bytes");
                
                for (StreamObserver o: _observers)
                    o.setLength(_localFile.length());
            }
            setRequestContentSource( getInputStream() );
            setRequestHeader( __BATCH_HEADER, _batchId );            
            super.send();
        }
        catch ( Exception e )
        {
            onFileError( _url, e );
        }
    }

    public boolean isRemoteJetty()
    {
        return _remoteRepoUrl != null;
    }

    public String getRemoteJettyUrl()
    {
        return _remoteRepoUrl;
    }


    protected void onResponseHeader( Buffer name, Buffer value )
    {
        if ( name.toString().equalsIgnoreCase( __BATCH_SUPPORTED_HEADER ) )
        {
            _remoteRepoUrl = value.toString();
        }
        else if ( name.toString().equalsIgnoreCase( __BATCH_HEADER ) )
        {
            _remoteBatchId = value.toString();
        }
    }

    protected void onResponseComplete()
    {
        try
        {
            if (_inputStream != null)
                _inputStream.close();
            
            if ( _status != HttpServletResponse.SC_OK && _status != HttpServletResponse.SC_CREATED && _status != HttpServletResponse.SC_NO_CONTENT )
            {
                onFileError( _url, new HttpClientException( _binding, "Http status code=" + _status ) );
                return;
            }

            if ( _remoteBatchId != null && !_batchId.equals( _remoteBatchId ) )
            {
                onFileError( _url, new HttpClientException( _binding,
                    "Non matching mercury ids. Sent=" + _batchId + " received=" + _remoteBatchId ) );
                return;
            }

            //we've uploaded the file
            onFileComplete( _url, _localFile );
        }
        catch ( Exception e )
        {
            onFileError( _url, new HttpClientException( _binding, e.getLocalizedMessage() ) );
        }
    }


    private InputStream getInputStream()
        throws IOException
    {
        if ( _inputStream == null )
        {
            InputStream is = null;
            if (_binding.isFile())
                is = new FileInputStream( _localFile );
            else if (_binding.isInMemory())
            {
                is = _binding.getLocalInputStream();
                if (!getRetryStatus())
                {                 
                    if (is.markSupported())
                        is.mark(__readLimit);
                }
                else
                {
                    if (is.markSupported())
                        is.reset();
                }
            }

            //if this request is being retried, then don't set up the observers a second
            //time?
            if (!getRetryStatus())
            {
                ObservableInputStream ois = new ObservableInputStream( is );
                _inputStream = ois;
                ois.addObservers(_observers);
            }
            else
                _inputStream = is;
        }    
        return _inputStream;
    }

   
    protected void onRetry() throws IOException
    {
        super.onRetry();
        if (_inputStream != null)
            _inputStream.close();
        
        _inputStream = null;
        setRequestContent(null);
        setRequestContentSource(getInputStream());
    }
}
