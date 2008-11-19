package org.apache.maven.mercury.util.event;

/**
 * 
 * this component generates events 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface EventGenerator
{
  /**
   * register event listener
   * 
   * @param listener
   */
  void register( MercuryEventListener listener );
  
  /**
   * remove particular event listener
   * 
   * @param listener
   */
  void unRegister( MercuryEventListener listener );
  
  /**
   * set entire event manager
   * 
   * @param eventManager
   */
  void setEventManager( EventManager eventManager );
  
  /**
   * send this event to all listeners
   * 
   * @param event
   */
  void fireEvent( MercuryEvent event );
}
