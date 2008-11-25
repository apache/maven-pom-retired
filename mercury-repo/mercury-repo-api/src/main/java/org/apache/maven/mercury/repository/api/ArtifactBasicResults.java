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
package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

/**
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactBasicResults
extends AbstractRepOpResult
{
  Map< ArtifactBasicMetadata, List<ArtifactBasicMetadata>> _result = new HashMap<ArtifactBasicMetadata, List<ArtifactBasicMetadata>>(8);

  /**
   * first result is ready 
   */
  public ArtifactBasicResults( ArtifactBasicMetadata query, List<ArtifactBasicMetadata> result )
  {
    this._result.put( query, result );
  }
  
  /**
   * optimization opportunity
   * 
   * @param size
   */
  public ArtifactBasicResults( int size )
  {
  }

  private ArtifactBasicResults()
  {
  }

  public static ArtifactBasicResults add( final ArtifactBasicResults res, final ArtifactBasicMetadata key, final Exception err )
  {
    ArtifactBasicResults ret = res;
    if( res == null )
      ret = new ArtifactBasicResults();
    
    ret.addError( key, err );
    
    return ret;
  }

  public static ArtifactBasicResults add( final ArtifactBasicResults res, final ArtifactBasicMetadata key, final List<ArtifactBasicMetadata> result )
  {
    ArtifactBasicResults ret = res;
    if( res == null )
      ret = new ArtifactBasicResults();
    
    ret.add( key, result );
    
    return ret;
  }

  public static ArtifactBasicResults add( final ArtifactBasicResults res, final ArtifactBasicMetadata key, final ArtifactBasicMetadata result )
  {
    ArtifactBasicResults ret = res;
    if( res == null )
      ret = new ArtifactBasicResults();
    
    ret.add( key, result );
    
    return ret;
  }
  
  private List<ArtifactBasicMetadata> getOrCreate( ArtifactBasicMetadata query )
  {
    List<ArtifactBasicMetadata> res = _result.get( query );
    if( res == null )
    {
      res = new ArrayList<ArtifactBasicMetadata>(8);
      _result.put( query, res );
    }
    return res;
  }
  
  /**
   * add results if they are not there yet
   * 
   * @param query
   * @param result
   */
  public void add( ArtifactBasicMetadata query, List<ArtifactBasicMetadata> result )
  {
    List<ArtifactBasicMetadata> res = getOrCreate( query );
    for( ArtifactBasicMetadata r : result )
    {
      if( res.contains( r ) )
        continue;
      res.add( r );
    }
  }
  
  public void add( ArtifactBasicMetadata query, ArtifactBasicMetadata result )
  {
    List<ArtifactBasicMetadata> res = getOrCreate( query );
    res.add( result );
  }

  public Map< ArtifactBasicMetadata, List<ArtifactBasicMetadata>> getResults()
  {
    return _result;
  }

  public List<ArtifactBasicMetadata> getResult( ArtifactBasicMetadata query )
  {
    return _result.get( query );
  }

  @Override
  public boolean hasResults()
  {
    return ! _result.isEmpty();
  }

  @Override
  public boolean hasResults( ArtifactBasicMetadata key )
  {
    return ! _result.isEmpty() && _result.containsKey( key ) && ! _result.get( key ).isEmpty();
  }

}
