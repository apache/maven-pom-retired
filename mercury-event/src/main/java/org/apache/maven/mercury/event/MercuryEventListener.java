package org.apache.maven.mercury.event;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface MercuryEventListener
{
  /**
   * identifies what events this listrener is interested in. 
   * 
   * @return the mask - BitSet of event type bits, or null, if this listener wants to be notified of all events 
   */
  MercuryEvent.EventMask getMask();
  
  /**
   * this is called when an event matching the listener mask is generated
   * 
   * @param event
   */
  void fire( MercuryEvent event );
}
