package org.apache.maven.mercury.client;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpExchange;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * HandshakeExchange
 * <p/>
 * Asychronous http message sending. Used to generate a commit or discard
 * message to the remote server after the mercury upload is finished.
 */
public abstract class HandshakeExchange extends HttpExchange
{
    private HttpClient _httpClient;
    private String _method;
    private String _url;
    private Map<String, String> _headers;
    private int _status;

    public abstract void onHandshakeComplete( String url );

    public abstract void onHandshakeError( String url, Exception e );

    /**
     * @param httpClient
     * @param method
     * @param url
     * @param headers
     */
    public HandshakeExchange( HttpClient httpClient, String method, String url, Map<String, String> headers )
    {
        _httpClient = httpClient;
        _method = method;
        _url = url;
        _headers = headers;
    }

    public void send()
    {
        try
        {
            setMethod( _method );
            setURL( _url );
            if ( _headers != null )
            {
                for ( Map.Entry<String, String> e : _headers.entrySet() )
                {
                    setRequestHeader( e.getKey(), e.getValue() );
                }
            }
            _httpClient.send( this );
        }
        catch ( Exception e )
        {
            onHandshakeError( _url, new Exception( null, e ) );
        }
    }


    protected void onResponseStatus( Buffer version, int status, Buffer reason )
    {
        _status = status;
    }

    protected void onException( Throwable ex )
    {
        onHandshakeError( _url, new Exception( ex ) );
    }

    protected void onExpire()
    {
        onHandshakeError( _url, new Exception( "Timeout occurred" ) );
    }

    protected void onResponseComplete()
    {
        if ( _status != HttpServletResponse.SC_OK )
        {
            onHandshakeError( _url, new Exception( "Http Error Code:" + _status ) );
        }
        else
        {
            onHandshakeComplete( _url );
        }
    }

}
