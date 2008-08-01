package org.apache.maven.mercury.transport.http;

import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.transport.api.AbstractTransport;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.ReaderTransport;
import org.apache.maven.mercury.transport.api.TransportException;
import org.apache.maven.mercury.transport.api.TransportTransaction;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * Jetty client adaptor to ReaderTransport APIs
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class HttpReaderTransport
extends AbstractTransport
implements ReaderTransport
{
  private static final Language _lang = new DefaultLanguage( HttpReaderTransport.class );
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( HttpReaderTransport.class ); 

  DefaultRetriever _httpReader;
  
  /**
   * 
   */
  public HttpReaderTransport()
  throws TransportException
  {
    try
    {
      _httpReader = new DefaultRetriever();
    }
    catch( HttpClientException e )
    {
      throw new TransportException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.apache.maven.mercury.transport.api.ReaderTransport#read(org.apache.maven.mercury.transport.api.TransportTransaction)
   */
  public TransportTransaction read( TransportTransaction trx )
  throws TransportException
  {
    if( trx == null )
    {
      _log.error( _lang.getMessage( "empty.transaction", trx == null ? "null" : trx.toString() ) );
       return trx;
    }

    DefaultRetrievalRequest request = new DefaultRetrievalRequest();
    
    for( Binding b : trx.getBindings() )
      request.addBinding( b );
    
    RetrievalResponse response = _httpReader.retrieve( request );

    return trx;
  }

}
