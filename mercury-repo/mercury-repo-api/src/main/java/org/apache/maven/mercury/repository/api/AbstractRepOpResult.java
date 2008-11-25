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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
/**
 * generic repository operation result. Represents a Map of query object to AbstractRepositoryOperationResult
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractRepOpResult
{
  private Map<ArtifactBasicMetadata,Exception> _exceptions;
  
  public AbstractRepOpResult()
  {
  }
  
  public Map<ArtifactBasicMetadata,Exception> getExceptions()
  {
      return _exceptions;
  }
  
  public abstract boolean hasResults();
  
  public abstract boolean hasResults( ArtifactBasicMetadata key );
  
  public boolean hasExceptions()
  {
    return _exceptions != null && ! _exceptions.isEmpty();
  }
  
  public void addError( ArtifactBasicMetadata key, Exception error )
  {
    if( _exceptions == null )
      _exceptions = new HashMap<ArtifactBasicMetadata, Exception>(8);

    _exceptions.put( key, error );
  }
  
  public Exception getError( ArtifactBasicMetadata key )
  {
    if( _exceptions == null )
      return null;

    return _exceptions.get( key );
  }

  public String toString()
  {
      return _exceptions.toString();
  }
}
