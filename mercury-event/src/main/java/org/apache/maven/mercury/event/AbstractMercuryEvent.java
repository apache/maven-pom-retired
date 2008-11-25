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
  EventTypeEnum _type;
  
  String _name;
  
  String _tag;
  
  String _info;
  
  String _result;
  
  long _start;
  
  long _duration;
  
  Map<String, Object> _payload;
  
  public AbstractMercuryEvent()
  {
    start();
  }
  
  public AbstractMercuryEvent( EventTypeEnum type, String name )
  {
    this();
    this._type = type;
    this._name = name;
  }
  
  public AbstractMercuryEvent( EventTypeEnum type, String name, String info )
  {
    this( type, name );
    this._info = info;
  }

  public long getDuration()
  {
    return _duration;
  }

  public Map<String, Object> getPayload()
  {
    return null;
  }

  public long getStart()
  {
    return _start;
  }

  public EventTypeEnum getType()
  {
    return _type;
  }

  public String getName()
  {
    return _name;
  }

  public String getInfo()
  {
    return _info;
  }

  public String getTag()
  {
    return _tag;
  }

  public String getResult()
  {
    return _result;
  }

  public void setResult( String result )
  {
    this._result = result;
  }

  public void setTag( String tag )
  {
    this._tag = tag;
  }

  public void setInfo( String info )
  {
    this._info = info;
  }

  public boolean hasResult()
  {
    return _result != null;
  }

  public Object getPayload( String name )
  {
    return _payload == null ? null : _payload.get( name );
  }

  public void setPayload( Map<String, Object> payload )
  {
    this._payload = payload;
  }

  public void setPayload( String name, Object value )
  {
    if( _payload == null )
      _payload = new HashMap<String, Object>(4);
    
    _payload.put( name, value );
  }

  public void start()
  {
    this._start = System.currentTimeMillis();
  }

  public void stop()
  {
    _duration = System.currentTimeMillis() - _start;
  }

}
