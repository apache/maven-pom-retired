// ========================================================================
// Copyright 2008 Sonatype Inc.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.apache.maven.mercury.client.retrieve;

import org.apache.maven.mercury.client.BatchException;
import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.client.ChecksumCalculator;
import org.apache.maven.mercury.client.FileExchange;
import org.mortbay.io.Buffer;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.client.HttpClient;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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
    private OutputStream _outputStream;

    /**
     * Constructor.
     *
     * @param binding        the remote file to fetch
     * @param localFile      the local file location to store the remote file
     * @param digestRequired if true, the file stream will be passed thru the digest calculator
     * @param client         async http client
     */
    public FileGetExchange( Binding binding, File localFile, boolean digestRequired, HttpClient client )
    {
        super( binding, localFile, digestRequired, client );
    }


    /** Start the retrieval. */
    public void send()
    {
        setMethod( HttpMethods.GET );
        super.send();
    }


    protected void onResponseComplete()
    {
        //All bytes of file have been received
        String digest = null;

        try
        {
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

            if ( _digestRequired && _outputStream != null )
            {
                byte[] bytes = ( (DigestOutputStream) _outputStream ).getMessageDigest().digest();
                digest = ChecksumCalculator.encodeToAsciiHex( bytes );
            }
            onFileComplete( _url, _localFile, digest );
        }
        catch ( Exception e )
        {
            onFileError( _url, new BatchException( _binding, e.getLocalizedMessage() ) );
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
            if ( !_digestRequired )
            {
                _outputStream = new FileOutputStream( _localFile );
            }
            else
            {
                MessageDigest digest = MessageDigest.getInstance( _digestAlgorithm );
                _outputStream = new DigestOutputStream( new FileOutputStream( _localFile ), digest );
            }
        }
        return _outputStream;
    }
}
