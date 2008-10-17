package org.apache.maven.mercury.transport.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Helper parent of transport implementations. Common for read and write transports
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractTransport
{
  private Collection<Server> _servers = Collections.synchronizedSet( new HashSet< Server >() );
  
  public void setServers( Collection<Server> servers )
  {
    _servers = servers;
  }
  
  public Collection<Server> getServers()
  {
    return _servers;
  }
  

}
