package org.apache.maven.mercury.util.event;

import java.util.Map;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface MercuryEvent
{
  /**
   * aggregate tag of this event. Used to trace event propagation in the system 
   * 
   * @return
   */
  String getTag();
  
  /**
   * get the event start time as UTC timestapm
   * 
   * @return start time as UTC timestamp
   */
  long getStart();
  
  /**
   * start the event
   */
  void start();
  
  /**
   * stop the event and calculate the duration
   */
  void stop();
  
  /**
   * duration of this event in millis
   * 
   * @return duration of this event
   */
  long getDuration();
  
  /**
   * event's payload
   *  
   * @return results, associated with this event
   */
  Map<String,Object> getPayload();
  
  /**
   * get one of payload values
   *  
   * @param name element name  
   * @return results, associated with this event
   */
  Object getPayload( String name );
  
  /**
   * set the whole payload
   * @param payload
   */
  void setPayload( Map<String,Object> payload );
  
  /**
   * set the whole payload
   * @param name
   * @param value
   */
  void setPayload( String name, Object value );
  
}
