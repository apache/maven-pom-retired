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

package org.apache.maven.mercury.transport.http;

import org.apache.maven.mercury.crypto.basic.SHA1Verifier;
import org.apache.maven.mercury.crypto.api.StreamObserver;
import org.apache.maven.mercury.crypto.api.StreamObserverFactory;

public class SHA1VerifierFactory implements StreamObserverFactory
{
    private boolean isLenient;
    private boolean isSufficient;

    public SHA1VerifierFactory (boolean isLenient, boolean isSufficient)
    {
        this.isLenient = isLenient;
        this.isSufficient = isSufficient;
    }
    public StreamObserver newInstance()
    {
       return new SHA1Verifier(this.isLenient, this.isSufficient);
    }

}
