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
  EventTypeEnum type;
  
  String name;
  
  String tag;
  
  String result;
  
  long start;
  
  long duration;
  
  Map<String, Object> payload;
  
  public AbstractMercuryEvent()
  {
    start();
  }
  
  public AbstractMercuryEvent( EventTypeEnum type, String name )
  {
    this();
    this.type = type;
    this.name = name;
  }
  
  public AbstractMercuryEvent( EventTypeEnum type, String name, String tag )
  {
    this( type, name );
    this.tag = tag;
  }

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

  public EventTypeEnum getType()
  {
    return type;
  }

  public String getName()
  {
    return name;
  }

  public String getTag()
  {
    return tag;
  }

  public String getResult()
  {
    return result;
  }

  public void setResult( String result )
  {
    this.result = result;
  }

  public boolean hasResult()
  {
    return result != null;
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
