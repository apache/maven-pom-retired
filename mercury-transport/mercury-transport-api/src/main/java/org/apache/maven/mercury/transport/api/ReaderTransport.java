package org.apache.maven.mercury.transport.api;

import java.util.Collection;

/**
 * generic Transport interface - allows client to read data from a remote repository
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface ReaderTransport
extends Initializable
{
  public TransportTransaction read( TransportTransaction trx )
  throws TransportException;
  
  public void setServers( Collection<Server> servers );
  
  public Collection<Server> getServers();
}
