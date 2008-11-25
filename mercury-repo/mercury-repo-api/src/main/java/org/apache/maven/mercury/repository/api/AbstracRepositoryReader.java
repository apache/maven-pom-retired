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

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;


/**
 * This is to keep MetadataProcessor for all readers
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstracRepositoryReader
implements RepositoryReader, MetadataReader
{
  protected DependencyProcessor    _mdProcessor;
  
  protected MetadataReader         _mdReader;
  
  protected RepositoryMetadataCache _mdCache;
  
  public void setDependencyProcessor( DependencyProcessor mdProcessor )
  {
    _mdProcessor = mdProcessor;
  }
  
  public DependencyProcessor getDependencyProcessor()
  {
    return _mdProcessor;
  }
  
  public void setMetadataReader( MetadataReader mdReader )
  {
    _mdReader = mdReader;
  }
  
  public MetadataReader getMetadataReader()
  {
    return _mdReader;
  }
  
  public void setMetadataCache( RepositoryMetadataCache mdCache )
  {
    this._mdCache = mdCache;
  }
  
  public RepositoryMetadataCache getMetadataCache()
  {
    return _mdCache;
  }
  
  public boolean hasMetadataCache()
  {
    return _mdCache != null;
  }
  
  public byte[] readMetadata( ArtifactBasicMetadata bmd  )
  throws MetadataReaderException
  {
    return readRawData( bmd, "", "pom" );
  }
  
}
