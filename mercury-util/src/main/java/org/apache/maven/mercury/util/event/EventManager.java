package org.apache.maven.mercury.util.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class EventManager
{
  List<MercuryEventListener> listeners = new ArrayList<MercuryEventListener>(8);
  
  public void register( MercuryEventListener listener )
  {
    listeners.add( listener );
  }
  
  public void unRegister( MercuryEventListener listener )
  {
    listeners.remove( listener );
  }
  
  public void fireEvent( MercuryEvent event )
  {
    for( MercuryEventListener listener : listeners )
      listener.fire( event );
  }
  
  public static final String toString( MercuryEvent event )
  {
    return new Date( event.getStart() )+", dur: "+ event.getDuration()+" millis : ["+ event.getType()+"] "+event.getTag();
  }

}
