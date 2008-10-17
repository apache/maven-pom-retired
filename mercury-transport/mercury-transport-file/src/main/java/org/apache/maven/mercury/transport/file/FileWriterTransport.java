package org.apache.maven.mercury.transport.file;

import org.apache.maven.mercury.transport.api.AbstractTransport;
import org.apache.maven.mercury.transport.api.InitializationException;
import org.apache.maven.mercury.transport.api.Server;
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
public class FileWriterTransport
extends AbstractTransport
implements WriterTransport
{
  protected Server server;
  

  public FileWriterTransport( Server server )
  {
    this.server = server;
  }

  public TransportTransaction write( TransportTransaction trx )
  throws TransportException
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.maven.mercury.transport.api.Initializable#init()
   */
  public void init()
      throws InitializationException
  {
    // TODO Auto-generated method stub
    
  }

}
//==============================================================
class FileWriter
implements Runnable
{

  public void run()
  {
  }
  
}
//==============================================================
