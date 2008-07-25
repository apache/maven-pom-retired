package org.apache.maven.mercury.transport.api;

/**
 * generic Transport interface - allows client to read data from a remote repository
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface ReaderTransport
{
  public TransportTransaction read( TransportTransaction trx )
  throws TransportException;
}
