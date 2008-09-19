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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.spi.http.client.FileExchange;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.ObservableOutputStream;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.mortbay.io.Buffer;
import org.mortbay.io.BufferUtil;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FileGetExchange
 * <p/>
 * Make an asynchronous request to download a file and stream its bytes to a file.
 * When all bytes have been received onFileComplete will be called.
 * <p/>
 * As an optimization, the file that is being downloaded can have it's
 * SHA-1 digest calculated as it is being streamed down.
 */
public abstract class FileGetExchange extends FileExchange
{
    private static final Logger log = LoggerFactory.getLogger(FileGetExchange.class);
    private OutputStream _outputStream;
    private Set<StreamObserver> _observers = new HashSet<StreamObserver>();
    int _contentLength = -1;
    
    /**
     * Constructor.
     *
     * @param binding        the remote file to fetch
     * @param localFile      the local file location to store the remote file
     * @param observers      observers of the io stream
     * @param client         async http client
     */
    public FileGetExchange( Server server, Binding binding, File localFile, Set<StreamObserver> observers, HttpClient client )
    {
        super( server, binding, localFile, client );
        if (observers != null)
            _observers.addAll(observers);
    }


    /** Start the retrieval. */
    public void send()
    {
        setMethod( HttpMethods.GET );
        super.send();
    }

    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        int header = HttpHeaders.CACHE.getOrdinal(value);
        switch (header)
        {
            case HttpHeaders.CONTENT_LENGTH_ORDINAL:
                _contentLength = BufferUtil.toInt(value);
                for (StreamObserver o:_observers)
                {
                    o.setLength(_contentLength);
                }
                if (log.isDebugEnabled())
                    log.debug("GET of "+_contentLength +" bytes");
                break;
            case HttpHeaders.LAST_MODIFIED_ORDINAL:
                for (StreamObserver o:_observers)
                {
                    o.setLastModified(BufferUtil.to8859_1_String(value));
                }
                break;
        }
    }
    

    protected void onResponseComplete()
    {
        //All bytes of file have been received
        try
        {
            if (_outputStream != null)
                _outputStream.close();
            
            if ( _status == HttpServletResponse.SC_NOT_FOUND )
            {
                onFileError( _url, new FileNotFoundException( "File not found on remote server" ) );
                return;
            }
            else if ( _status != HttpServletResponse.SC_OK )
            {
                onFileError( _url, new Exception( "Http status code=" + _status ) );
                return;
            }

            onFileComplete( _url, _localFile );
        }
        catch ( Exception e )
        {
            onFileError( _url, new HttpClientException( _binding, e.getLocalizedMessage() ) );
        }
    }


    /**
     * Stream the downloaded bytes to a file
     *
     * @see org.mortbay.jetty.client.HttpExchange$ContentExchange#onResponseContent(org.sonatype.io.Buffer)
     */
    protected void onResponseContent( Buffer content )
        throws IOException
    {
        try
        {
            OutputStream os = getOutputStream();
            content.writeTo( os );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new IOException( e.getLocalizedMessage() );
        }
    }


    /**
     * Get an output stream for the file contents. A digest can be optionally calculated
     * for the file contents as they are being streamed.
     *
     * @return OutputStream for file contents
     * @throws IOException              if io error occurs
     * @throws NoSuchAlgorithmException if the SHA-1 algorithm is not supported
     */
    protected OutputStream getOutputStream()
        throws IOException, NoSuchAlgorithmException
    {
        if ( _outputStream == null )
        {
            OutputStream os = null;
            if (_binding.isFile())
                os = new FileOutputStream( _localFile );
            else if (_binding.isInMemory())
                os = _binding.getLocalOutputStream();
            
            ObservableOutputStream oos = new ObservableOutputStream( os );
            oos.addObservers(_observers);
            _outputStream = oos;
        }
        return _outputStream;
    }
}
