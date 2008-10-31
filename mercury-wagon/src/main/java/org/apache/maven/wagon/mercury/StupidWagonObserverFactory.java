package org.apache.maven.wagon.mercury;

import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamObserverFactory;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class StupidWagonObserverFactory
implements StreamObserverFactory
{
  private MercuryWagon wagon;
  
  public StupidWagonObserverFactory( MercuryWagon wagon )
  {
    this.wagon = wagon;
  }

  public StreamObserver newInstance()
  throws StreamObserverException
  {
    return new StupidWagonObserverAdapter( wagon, wagon.popEvent() );
  }

}
