package org.apache.maven.mercury.util.event;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractMercuryEvent
    implements MercuryEvent
{
  String tag;
  long start;
  long duration;
  Map<String, Object> payload;

  public long getDuration()
  {
    return duration;
  }

  public Map<String, Object> getPayload()
  {
    return null;
  }

  public long getStart()
  {
    return start;
  }

  public String getTag()
  {
    return tag;
  }

  public Object getPayload( String name )
  {
    return payload == null ? null : payload.get( name );
  }

  public void setPayload( Map<String, Object> payload )
  {
    this.payload = payload;
  }

  public void setPayload( String name, Object value )
  {
    if( payload == null )
      payload = new HashMap<String, Object>(4);
    
    payload.put( name, value );
  }

  public void start()
  {
    this.start = System.currentTimeMillis();
  }

  public void stop()
  {
    duration = System.currentTimeMillis() - start;
  }

}
