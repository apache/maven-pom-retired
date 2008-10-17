package org.apache.maven.mercury.transport.http;

import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.transport.api.AbstractTransport;
import org.apache.maven.mercury.transport.api.InitializationException;
import org.apache.maven.mercury.transport.api.TransportException;
import org.apache.maven.mercury.transport.api.TransportTransaction;
import org.apache.maven.mercury.transport.api.WriterTransport;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class HttpWriterTransport
extends AbstractTransport
implements WriterTransport
{
  
  DefaultDeployer _deployer;

  public TransportTransaction write( TransportTransaction trx )
  throws TransportException
  {
    return null;
  }

  public void init()
  throws InitializationException
  {
  }

}
