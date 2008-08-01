package org.apache.maven.mercury.transport.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository access transaction. Consists of a collection of bindings
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class TransportTransaction
{
  public static final int DEFAULT_SIZE = 32;
  
  protected List<Binding> _bindings;
  
  //------------------------------------------------------------------------------------------------
  private void init()
  {
    init( DEFAULT_SIZE );
  }
  //------------------------------------------------------------------------------------------------
  private void init( int n )
  {
    if( _bindings == null )
      _bindings = new ArrayList<Binding>( n );
  }
  //------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public TransportTransaction()
  {
    init();
  }
  //------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public TransportTransaction add( Binding binding )
  {
    init();
    
    _bindings.add( binding );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  public TransportTransaction add( URL remoteResource, File localResource )
  {
    init();
    
    _bindings.add( new Binding( remoteResource, localResource ) );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  public TransportTransaction add( URL remoteResource )
  {
    init();
    
    _bindings.add( new Binding( remoteResource ) );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  public TransportTransaction add( URL remoteResource, InputStream is )
  {
    init();
    
    _bindings.add( new Binding( remoteResource, is ) );
    
    return this;
  }

  //------------------------------------------------------------------------------------------------
  public TransportTransaction add( URL remoteResource, byte [] localResource )
  {
    init();
    
    _bindings.add( new Binding( remoteResource, new ByteArrayInputStream(localResource)) );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public List<Binding> getBindings()
  {
    return _bindings == null ? (List<Binding>)Collections.EMPTY_LIST : _bindings;
  }

  public void setBindings( List<Binding> bindings )
  {
    this._bindings = bindings;
  }
  //------------------------------------------------------------------------------------------------
  public boolean isEmpty()
  {
    if( _bindings == null || _bindings.size() < 1 )
      return true;
    
    return false;
  }
  //------------------------------------------------------------------------------------------------
  public boolean hasErrors()
  {
    if( _bindings == null )
      return false;
    
    for( Binding b : _bindings )
      if( b.getError() != null )
        return true;
    
    return false;
  }
  //------------------------------------------------------------------------------------------------
  public void clearErrors()
  {
    if( _bindings == null )
      return;
    
    for( Binding b : _bindings )
      b.setError( null );
  }
  //------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------
}
