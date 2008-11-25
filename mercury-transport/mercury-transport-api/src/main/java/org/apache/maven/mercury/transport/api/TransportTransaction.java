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
package org.apache.maven.mercury.transport.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
  
  protected Collection<Binding> _bindings;
  
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
  public Collection<Binding> getBindings()
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
