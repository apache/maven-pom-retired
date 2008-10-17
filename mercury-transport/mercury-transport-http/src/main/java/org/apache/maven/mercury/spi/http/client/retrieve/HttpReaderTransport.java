package org.apache.maven.mercury.spi.http.client.retrieve;

import org.apache.maven.mercury.transport.api.AbstractTransport;
import org.apache.maven.mercury.transport.api.InitializationException;
import org.apache.maven.mercury.transport.api.ReaderTransport;
import org.apache.maven.mercury.transport.api.TransportException;
import org.apache.maven.mercury.transport.api.TransportTransaction;

/**
 * HTTP retriever adaptor: adopts DefaultRetriever to ReaderTransport API
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class HttpReaderTransport
extends AbstractTransport
implements ReaderTransport
{
  private DefaultRetriever _retriever;
  
  public TransportTransaction read( TransportTransaction trx )
  throws TransportException
  {
    return null;
  }

  public void init()
  throws InitializationException
  {
  }

}
