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
import org.apache.maven.mercury.spi.http.client.FileExchange;
import org.apache.maven.mercury.spi.http.client.MercuryException;
import org.apache.maven.mercury.transport.ChecksumCalculator;
import org.mortbay.io.Buffer;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.client.HttpClient;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * FilePutExchange
 * <p/>
 * Asynchronously PUT a file to a remote server. The file that is being uploaded can also
 * have it's SHA-1 digest calculated as it is being streamed up.
 */
public abstract class FilePutExchange extends FileExchange
{
    private boolean _digestRequired;
    private String _batchId;
    private InputStream _inputStream;
    private String _remoteRepoUrl;
    private String _remoteBatchId;

    public abstract void onFileComplete( String url, File localFile, String digest );

    public abstract void onFileError( String url, Exception e );


    public FilePutExchange( String batchId, Binding binding, File localFile, boolean isDigestRequired, HttpClient client )
    {
        super( binding, localFile, client );
        _digestRequired = isDigestRequired;
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
            setRequestContentSource( getInputStream() );
            setRequestHeader( "Content-Type", "application/octet-stream" );
            setRequestHeader( "Content-Length", String.valueOf( _localFile.length() ) );
            setRequestHeader( __BATCH_HEADER, _batchId );
            System.err.println( "Sending PUT for " + getURI() );
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
        System.err.println( "On ResponseComplete for put for " + _url );
        String digest = null;
        try
        {
            if ( _status != HttpServletResponse.SC_OK && _status != HttpServletResponse.SC_CREATED && _status != HttpServletResponse.SC_NO_CONTENT )
            {
                onFileError( _url, new MercuryException( _binding, "Http status code=" + _status ) );
                return;
            }

            if ( _remoteBatchId != null && !_batchId.equals( _remoteBatchId ) )
            {
                onFileError( _url, new MercuryException( _binding,
                    "Non matching mercury ids. Sent=" + _batchId + " received=" + _remoteBatchId ) );
                return;
            }

            if ( _digestRequired && _inputStream != null )
            {
                byte[] bytes = ( (DigestInputStream) _inputStream ).getMessageDigest().digest();
                digest = ChecksumCalculator.encodeToAsciiHex( bytes );
            }
            onFileComplete( _url, _localFile, digest );
        }
        catch ( Exception e )
        {
            onFileError( _url, new MercuryException( _binding, e.getLocalizedMessage() ) );
        }
    }


    private InputStream getInputStream()
        throws IOException, NoSuchAlgorithmException
    {
        if ( _inputStream == null )
        {
            if ( !_digestRequired )
            {
                _inputStream = new FileInputStream( _localFile );
            }
            else
            {
                MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
                _inputStream = new DigestInputStream( new FileInputStream( _localFile ), digest );
            }
        }
        System.err.println( "Returning input stream for " + _localFile.getName() );
        return _inputStream;
    }
}
