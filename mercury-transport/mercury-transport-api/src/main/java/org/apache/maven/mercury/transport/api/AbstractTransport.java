package org.apache.maven.mercury.transport.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper parent of transport implementations. Common for read and write transports
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractTransport
{
  private Map< String, Server > _servers = Collections.synchronizedMap( new HashMap< String, Server >(8) );
  
  public void addServer( Server server )
  {
    if( server == null )
      return;
    
    _servers.put( server.getId(), server );
  }
  
  public void addServers( Collection<ServerContainer> containers )
  {
    for( ServerContainer c : containers )
      addServer( c.getServer() );
  }

  public Server findServer( String id )
  {
    return _servers.get( id );
  }

  public void dropServer( String id )
  {
    _servers.remove( id );
  }

}
