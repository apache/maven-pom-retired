package org.apache.maven.mercury.util.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.util.Util;
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
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( EventManager.class );
  private static final Language _lang = new DefaultLanguage( EventManager.class );
  
  public static final int THREAD_COUNT = 4;
  
  List<MercuryEventListener> listeners = new ArrayList<MercuryEventListener>(8);
  
  final LinkedBlockingQueue<UnitOfWork> queue = new LinkedBlockingQueue<UnitOfWork>( 512 );
  
  ExecutorService execService;
  
  public EventManager()
  {
    execService = Executors.newFixedThreadPool( THREAD_COUNT );
    for( int i = 0; i < THREAD_COUNT; i++ )
      execService.execute( new Runner( queue ) );
  }
  
  public void register( MercuryEventListener listener )
  {
    listeners.add( listener );
  }
  
  public void unRegister( MercuryEventListener listener )
  {
    listeners.remove( listener );
  }
  
  public void fireEvent( MercuryEvent event )
  {
    for( MercuryEventListener listener : listeners )
      queue.add( new UnitOfWork( listener, event ) );
  }

  public static final String toString( MercuryEvent event )
  {
    return new Date( event.getStart() )+", dur: "+ event.getDuration()+" millis :"
    		   + " ["+ event.getType()+"] "
    		   + ( Util.isEmpty( event.getTag() ) ? "" : ", tag: "+event.getTag() )
           + ( Util.isEmpty( event.getError() ) ? "" : ", error: "+event.getError() )
    ;
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

