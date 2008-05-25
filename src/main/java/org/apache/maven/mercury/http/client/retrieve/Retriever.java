/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file                                                                                            
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.maven.mercury.http.client.retrieve;

/**
 * Retriever
 * <p/>
 * Component to retrieve a set of remote files as an atomic operation.
 */
public interface Retriever
{
    /**
     * Retrieve a set of artifacts and wait until all retrieved successfully
     * or an error occurs.
     * <p/>
     * Note: whilst this method is synchronous for the caller, the implementation
     * will be asynchronous so many artifacts are fetched in parallel.
     *
     * @param request
     * @return
     */
    RetrievalResponse retrieve( RetrievalRequest request );


    /**
     * Retrieve a set of artifacts without waiting for the results.
     * When all results have been obtained (or an error occurs) the
     * RetrievalResponse will be called.
     *
     * @param request
     * @param callback
     */
    void retrieve( RetrievalRequest request, RetrievalCallback callback );
}
