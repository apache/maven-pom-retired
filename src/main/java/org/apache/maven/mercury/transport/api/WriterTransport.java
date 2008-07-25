package org.apache.maven.mercury.transport.api;

/**
 * generic Transport interface - allows client to write data to a remote repository
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface WriterTransport
{
  public TransportTransaction write( TransportTransaction trx )
  throws TransportException;
}
