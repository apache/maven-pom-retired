package org.apache.maven.mercury.util.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.maven.mercury.util.event.MercuryEvent.EventMask;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class EventFrameworkTest
extends TestCase
{
  static final int THREAD_COUNT = 5;
  static final int EVENT_COUNT  = 10;
  
  ExecutorService es;
  
  EventManager em;
  
  Listener listener;
  
  @Override
  protected void setUp()
  throws Exception
  {
    es = Executors.newFixedThreadPool( THREAD_COUNT );
  }
  public void testListenAllEvents()
  throws InterruptedException
  {
    em = new EventManager();
    
    // testing this one - receive all events
    listener = new Listener( null );
    
    em.register( listener );

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.localRepository, ""+i ) );
    }

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.remoteRepository, ""+i ) );
    }
    
    es.awaitTermination( 2, TimeUnit.SECONDS );
    
    assertEquals( THREAD_COUNT * EventFrameworkTest.EVENT_COUNT, listener.localRepoCount );
    assertEquals( THREAD_COUNT * EventFrameworkTest.EVENT_COUNT, listener.remoteRepoCount );
  }

  public void testListenMaskedListenerEvents()
  throws InterruptedException
  {
    em = new EventManager();
    
    // test this - receive only local repo events
    listener = new Listener( new MercuryEvent.EventMask(MercuryEvent.EventTypeEnum.localRepository) );
    
    em.register( listener );

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.localRepository, ""+i ) );
    }

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.remoteRepository, ""+i ) );
    }
    
    es.awaitTermination( 2, TimeUnit.SECONDS );
    
    assertEquals( THREAD_COUNT * EventFrameworkTest.EVENT_COUNT, listener.localRepoCount );
    assertEquals( 0, listener.remoteRepoCount );
  }

  public void testListenMaskedManagerEvents()
  throws InterruptedException
  {
    // test this - propagate only remote repo events
    em = new EventManager( new MercuryEvent.EventMask(MercuryEvent.EventTypeEnum.remoteRepository) );
    
    // listen to all
    listener = new Listener( null );
    
    em.register( listener );

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.localRepository, ""+i ) );
    }

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.remoteRepository, ""+i ) );
    }
    
    es.awaitTermination( 2, TimeUnit.SECONDS );
    
    assertEquals( 0, listener.localRepoCount );
    assertEquals( THREAD_COUNT * EventFrameworkTest.EVENT_COUNT, listener.remoteRepoCount );
  }

  public void testListenMismatchedMaskEvents()
  throws InterruptedException
  {
    // test this - propagate only remote repo events
    em = new EventManager( new MercuryEvent.EventMask(MercuryEvent.EventTypeEnum.remoteRepository) );
    
    // and this - listen only to local events
    listener = new Listener( new MercuryEvent.EventMask(MercuryEvent.EventTypeEnum.localRepository) );
    
    em.register( listener );

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.localRepository, ""+i ) );
    }

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, MercuryEvent.EventTypeEnum.remoteRepository, ""+i ) );
    }
    
    es.awaitTermination( 2, TimeUnit.SECONDS );
    
    assertEquals( 0, listener.localRepoCount );
    assertEquals( 0, listener.remoteRepoCount );
  }
}

//=====================  helper classes  =====================
class Listener
implements MercuryEventListener
{
  MercuryEvent.EventMask _mask;
  
  int localRepoCount = 0;
  
  int remoteRepoCount = 0;
  
  public Listener( MercuryEvent.EventMask mask )
  {
    _mask = mask;
  }

  public void fire( MercuryEvent event )
  {
    System.out.println( EventManager.toString( event ) );
    System.out.flush();
    
    if( event.getType().equals( MercuryEvent.EventTypeEnum.localRepository ) )
      ++localRepoCount;
    else
      if( event.getType().equals( MercuryEvent.EventTypeEnum.remoteRepository ) )
        ++remoteRepoCount;
  }

  public EventMask getMask()
  {
    return _mask;
  }
  
}

class Generator
implements Runnable, EventGenerator
{
  
  EventManager _eventManager;
  
  String _msg;
  
  MercuryEvent.EventTypeEnum _myType;
  
  public Generator( EventManager em, MercuryEvent.EventTypeEnum type, String msg  )
  {
    _eventManager = em;
    _msg = msg;
    _myType = type;
  }
  
  public void run()
  {
    for( int i=0; i< EventFrameworkTest.EVENT_COUNT; i++ )
      try
      {
        GenericEvent event = new GenericEvent( _myType, _msg );
        Thread.sleep( (int)(100.0*Math.random()) );
        event.stop();
        _eventManager.fireEvent( event );
      }
      catch( InterruptedException e )
      {
        return;
      }
  }

  public void register( MercuryEventListener listener )
  {
    if( _eventManager == null )
      _eventManager = new EventManager();
      
    _eventManager.register( listener );
  }

  public void unRegister( MercuryEventListener listener )
  {
    if( _eventManager != null )
      _eventManager.unRegister( listener );
  }
  
  public void setEventManager( EventManager eventManager )
  {
    if( _eventManager == null )
      _eventManager = eventManager;
    else
      _eventManager.getListeners().addAll( eventManager.getListeners() );
      
  }
}

