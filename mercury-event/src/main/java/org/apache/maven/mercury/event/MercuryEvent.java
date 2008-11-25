/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.maven.mercury.event;

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
   * information for this event. Used to trace event propagation in the system 
   * 
   * @return
   */
  String getInfo();
  void setInfo( String info );
  
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
