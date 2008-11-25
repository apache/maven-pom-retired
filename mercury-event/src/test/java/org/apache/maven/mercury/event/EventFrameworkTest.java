package org.apache.maven.mercury.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.maven.mercury.event.EventGenerator;
import org.apache.maven.mercury.event.EventManager;
import org.apache.maven.mercury.event.EventTypeEnum;
import org.apache.maven.mercury.event.GenericEvent;
import org.apache.maven.mercury.event.MercuryEvent;
import org.apache.maven.mercury.event.MercuryEventListener;
import org.apache.maven.mercury.event.MercuryEvent.EventMask;

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
  throws Exception
  {
    runTest( null, null, THREAD_COUNT * EventFrameworkTest.EVENT_COUNT,  THREAD_COUNT * EventFrameworkTest.EVENT_COUNT );
  }

  public void testListenMaskedListenerEvents()
  throws Exception
  {
    runTest(  null
            , new MercuryEvent.EventMask(EventTypeEnum.localRepository)
            , THREAD_COUNT * EventFrameworkTest.EVENT_COUNT
            , 0
           );
  }

  public void testListenMaskedManagerEvents()
  throws Exception
  {
    runTest( new MercuryEvent.EventMask(EventTypeEnum.remoteRepository)
            , null
            , 0
            , THREAD_COUNT * EventFrameworkTest.EVENT_COUNT
       );
  }

  public void testListenMismatchedMaskEvents()
  throws Exception
  {
    runTest( new MercuryEvent.EventMask(EventTypeEnum.remoteRepository)
            , new MercuryEvent.EventMask(EventTypeEnum.localRepository)
            , 0
            , 0
          );
  }
  //-------------------------------------------------------------------------------------------------------------------------------
  private void runTest( MercuryEvent.EventMask emMask, MercuryEvent.EventMask listenerMask, int expectedLocal, int expectedRemote )
  throws Exception
  {
    em = new EventManager( emMask );
    
    listener = new Listener( listenerMask  );
    
    em.register( listener );

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, EventTypeEnum.localRepository, ""+i ) );
    }

    for( int i=0; i<THREAD_COUNT; i++ )
    {
      es.execute( new Generator( em, EventTypeEnum.remoteRepository, ""+i ) );
    }
    
    es.awaitTermination( 2, TimeUnit.SECONDS );
    
    assertEquals( expectedLocal, listener.localRepoCount );
    assertEquals( expectedRemote, listener.remoteRepoCount );
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
//    System.out.println( EventManager.toString( event ) );
//    System.out.flush();
    
    if( event.getType().equals( EventTypeEnum.localRepository ) )
      ++localRepoCount;
    else
      if( event.getType().equals( EventTypeEnum.remoteRepository ) )
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
  
  EventTypeEnum _myType;
  
  public Generator( EventManager em, EventTypeEnum type, String msg  )
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

