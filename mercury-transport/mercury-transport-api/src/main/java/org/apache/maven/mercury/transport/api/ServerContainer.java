package org.apache.maven.mercury.transport.api;

/**
 * common interface for transports to obtain their servers from Collection's
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface ServerContainer
{
  public Server getServer();
}
