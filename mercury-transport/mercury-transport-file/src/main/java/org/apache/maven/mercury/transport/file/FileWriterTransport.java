package org.apache.maven.mercury.transport.file;

import java.util.List;

import org.apache.maven.mercury.crypto.api.StreamObserver;
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
