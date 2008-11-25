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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * event queue dispatcher. It registers/unregisters listeners, dispatches events
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class EventManager
{
  public static final int THREAD_COUNT = 4;

  /**
   * this property may contain comma separated list of bit numbers defined in MercuryEvent.EventTypeEnum. It supersedes 
   * any bits set by the appropriate EventManager constructor by OR operation with those
   */
  public static final String SYSTEM_PROPERTY_EVENT_MASK = "maven.mercury.events";
  public static final String systemPropertyEventMask = System.getProperty( SYSTEM_PROPERTY_EVENT_MASK, null );

  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( EventManager.class );
  private static final Language _lang = new DefaultLanguage( EventManager.class );
  
  final List<MercuryEventListener> _listeners = new ArrayList<MercuryEventListener>(8);
  
  final LinkedBlockingQueue<UnitOfWork> _queue = new LinkedBlockingQueue<UnitOfWork>( 512 );
  
  private ExecutorService _execService;
  
  private MercuryEvent.EventMask _eventMask;
  
  /**
   * default initialization - create thread pool
   */
  public EventManager()
  {
    _execService = Executors.newFixedThreadPool( THREAD_COUNT );

    for( int i = 0; i < THREAD_COUNT; i++ )
      _execService.execute( new Runner( _queue ) );
    
    processSystemOptions();
  }

  /**
   * default initialization - create thread pool
   */
  public EventManager( MercuryEvent.EventMask eventMask )
  {
    this();
    
    this._eventMask = eventMask;
    
    processSystemOptions();
  }
  
  private final void processSystemOptions()
  {
    if( systemPropertyEventMask == null )
      return;
    
    if( _eventMask == null )
      _eventMask = new MercuryEvent.EventMask( systemPropertyEventMask );
    else
      _eventMask.setBits( systemPropertyEventMask );
  }
  
  /**
   * add listener only if it meets the criteria
   * 
   * @param listener
   */
  public void register( MercuryEventListener listener )
  {
    MercuryEvent.EventMask lMask = listener.getMask();
    
    if( lMask == null || _eventMask == null || _eventMask.intersects( lMask ) )
      _listeners.add( listener );
  }
  
  public void unRegister( MercuryEventListener listener )
  {
    _listeners.remove( listener );
  }
  
  public List<MercuryEventListener> getListeners()
  {
    return _listeners;
  }
  
  public void fireEvent( MercuryEvent event )
  {
    for( MercuryEventListener listener : _listeners )
      _queue.add( new UnitOfWork( listener, event ) );
  }

  public static final String toString( MercuryEvent event )
  {
    return new Date( event.getStart() )+", dur: "+ event.getDuration()+" millis :"
    		   + " ["+ event.getType()+":"+event.getName()+"] "
           + ( isEmpty( event.getTag() ) ? "" : ", tag: "+event.getTag() )
           + ( isEmpty( event.getInfo() ) ? "" : ", info: "+event.getInfo() )
           + ( isEmpty( event.getResult() ) ? "" : ", result: "+event.getResult() )
    ;
  }
  
  public static final boolean isEmpty( String o )
  {
    return o == null || o.length() < 1;
  }


  class UnitOfWork
  {
    MercuryEventListener listener;
    MercuryEvent event;
    
    public UnitOfWork( MercuryEventListener listener, MercuryEvent event )
    {
      this.listener = listener;
      this.event = event;
    }
    
    void execute()
    {
      try
      {
        MercuryEvent.EventMask lMask = listener.getMask();
        
        if( _eventMask != null )
        {
          if( lMask == null )
            lMask = _eventMask;
          else 
            lMask.and( _eventMask );
        }
        
        
        if( lMask == null || lMask.get( event.getType().bitNo ) )
          listener.fire( event );
      }
      catch( Throwable th )
      {
        _log.error( _lang.getMessage( "listener.error", th.getMessage() ) );
      }
    }
  }
  
  class Runner
  implements Runnable
  {
    final LinkedBlockingQueue<UnitOfWork> queue;

    public Runner( LinkedBlockingQueue<UnitOfWork> queue )
    {
      this.queue = queue;
    }

    public void run()
    {
      UnitOfWork uow;

      for(;;)
        try
        {
          uow = queue.take();
          uow.execute();
        }
        catch( InterruptedException e )
        {
          return;
        }
    }
    
  }

}

