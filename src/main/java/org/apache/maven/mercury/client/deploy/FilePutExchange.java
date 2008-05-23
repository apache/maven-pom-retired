package org.apache.maven.mercury.client.deploy;

import org.apache.maven.mercury.client.BatchException;
import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.client.ChecksumCalculator;
import org.apache.maven.mercury.client.FileExchange;
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

    private String _batchId;
    private InputStream _inputStream;
    private String _remoteRepoUrl;
    private String _remoteBatchId;

    public abstract void onFileComplete( String url, File localFile, String digest );

    public abstract void onFileError( String url, Exception e );


    public FilePutExchange( String batchId, Binding binding, File localFile, boolean digestRequired, HttpClient client )
    {
        super( binding, localFile, digestRequired, client );
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
                onFileError( _url, new BatchException( _binding, "Http status code=" + _status ) );
                return;
            }

            if ( _remoteBatchId != null && !_batchId.equals( _remoteBatchId ) )
            {
                onFileError( _url, new BatchException( _binding,
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
            onFileError( _url, new BatchException( _binding, e.getLocalizedMessage() ) );
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
                MessageDigest digest = MessageDigest.getInstance( _digestAlgorithm );
                _inputStream = new DigestInputStream( new FileInputStream( _localFile ), digest );
            }
        }
        System.err.println( "Returning input stream for " + _localFile.getName() );
        return _inputStream;
    }
}
