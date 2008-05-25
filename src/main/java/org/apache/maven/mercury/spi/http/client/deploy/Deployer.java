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

package org.apache.maven.mercury.spi.http.client.deploy;

/**
 * Deployer
 * <p/>
 * Deploy a set of files to remote locations as an atomic operation.
 */
public interface Deployer
{
    /** Deploy a set of files and return when all done. */
    DeployResponse deploy( DeployRequest request );


    /**
     * Deploy a set of files and return immediately without waiting.
     * The callback will be called when files are ready or an error
     * has occurred.
     */
    void deploy( DeployRequest request, DeployCallback callback );
}
