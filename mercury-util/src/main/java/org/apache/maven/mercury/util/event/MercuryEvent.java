package org.apache.maven.mercury.util.event;

import java.util.BitSet;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface MercuryEvent
{
  enum EventTypeEnum
  {
      dependencyBuilder(0)
    , satSolver(1)
    
    , virtualRepositoryReader(2)
    
    , localRepository(3)
    , localRepositoryReader(4)
    , localRepositoryWriter(5)
    
    , remoteRepository(6)
    , remoteRepositoryReader(7)
    , remoteRepositoryWriter(8)
    
    , cache(9)
    , fsCache(10)
    ;
    
    int bitNo;
    
    EventTypeEnum( int bitNo )
    {
      this.bitNo = bitNo;
    }
  }

  @SuppressWarnings("serial")
  class EventMask
  extends BitSet
  {
    public EventMask( EventTypeEnum... bits )
    {
      super();
      
      for( EventTypeEnum bit : bits )
        set( bit.bitNo );
    }

    public EventMask( String bits )
    {
      super();
      
      setBits( bits );
    }
    
    public final void setBits( String bits )
    {
      if( bits == null )
        return;
      
      StringTokenizer st = new StringTokenizer( bits, ",");
      
      while( st.hasMoreTokens() )
      {
        String bit = st.nextToken();
        
        int bitNo = Integer.valueOf( bit );
        
        set( bitNo, true );
      }
    }
  }

  /**
   * event type 
   * 
   * @return 
   */
  EventTypeEnum getType();

  /**
   * event name inside type 
   * 
   * @return
   */
  String getName();
  
  /**
   * aggregation tag of this event. Used to trace event propagation in the system 
   * 
   * @return
   */
  String getTag();
  void setTag( String tag );
  
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
   * result field
   * 
   * @return
   */
  public String getResult();

  public void setResult( String result );

  public boolean hasResult();
  
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
