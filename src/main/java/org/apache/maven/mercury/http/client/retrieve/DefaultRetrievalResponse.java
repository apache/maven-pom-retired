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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.http.client.MercuryException;

public class DefaultRetrievalResponse implements RetrievalResponse
{
    private Set<MercuryException> _exceptions = Collections.synchronizedSet( new HashSet<MercuryException>() );

    public DefaultRetrievalResponse()
    {
    }

    protected void add( MercuryException exception )
    {
        _exceptions.add( exception );
    }

    public Set<MercuryException> getExceptions()
    {
        return _exceptions;
    }

    public String toString()
    {
        return _exceptions.toString();
    }
    
    public boolean hasExceptions()
    {
        return _exceptions != null;
    }

}
