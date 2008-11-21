package org.apache.maven.mercury.util.event;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.maven.mercury.util.event.MercuryEvent.EventMask;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DumbListener
implements MercuryEventListener
{
  
  Writer wr;
  
  public DumbListener()
  {
    this( System.out );
  }
  
  public DumbListener( OutputStream os )
  {
    wr = new OutputStreamWriter( os );
  }

  public void fire( MercuryEvent event )
  {
    try
    {
      wr.write( "mercury event: "+EventManager.toString( event )+"\n" );
      wr.flush();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }
  }

  public EventMask getMask()
  {
    return null;
  }

}
