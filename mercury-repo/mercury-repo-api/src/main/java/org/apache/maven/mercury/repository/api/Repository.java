package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.builder.api.MetadataProcessor;


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 */
public interface Repository
{
  String getId();
  

  /**
   * repository type - m2, nexus, ivy, p2 - to name a few. It defines the RepositoryReader/Writer 
   * that will be searched for in the registry.
   */
  public String getType();

  /**
   * Indicates whether this is local Repository. This flag defines the necessity to download
   * the artifact, if it was cleared by the conflict resolver but not read from a localRepo.
   */
  public boolean isLocal();
  

  /**
   * Indicates whether it's even possible to write to this Repository. If there are multiple localRepo's and Artifact
   * needs to be downloaded - it will be "written" to all "local" repositories that are not "readOnly".
   */
  public boolean isReadOnly();
  
    /**
     * get default reader, if any
     * 
     * @return default reader or null, if none exists
     * @throws RepositoryException 
     */
    RepositoryReader getReader( MetadataProcessor processor )
    throws RepositoryException;
    
    /**
     * 
     * @param protocol
     * @return reader instance for the specified protocol
     * @throws NonExistentProtocolException if protocol not supported
     */
    RepositoryReader getReader( MetadataProcessor processor, String protocol )
    throws RepositoryException;

    
    /**
     * get default writer, if any
     * 
     * @return default writer or null, if none exists
     */
    RepositoryWriter getWriter();
    
    /**
     * 
     * @param protocol
     * @return writer instance for the specified protocol
     * @throws NonExistentProtocolException if protocol not supported
     */
    RepositoryWriter getWriter( String protocol )
    throws NonExistentProtocolException;
}
