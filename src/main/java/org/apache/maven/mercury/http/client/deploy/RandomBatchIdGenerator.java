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

package org.apache.maven.mercury.http.client.deploy;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


public class RandomBatchIdGenerator implements BatchIdGenerator
{
    protected final static String SESSION_ID_RANDOM_ALGORITHM = "SHA1PRNG";
    private Random _random;
    private boolean _initialized;


    public RandomBatchIdGenerator()
    {
    }

    public String getId()
    {
        init();
        String id = "";
        if ( !( _random instanceof SecureRandom ) )
        {
            id = String.valueOf( hashCode() ^ Runtime.getRuntime().freeMemory() ^ _random.nextInt() );
        }
        else
        {
            id = String.valueOf( _random.nextLong() );
        }
        return id;
    }

    public void setRandom( Random random )
    {
        _random = random;
    }

    public Random getRandom()
    {
        return _random;
    }

    private void init()
    {
        synchronized ( this )
        {
            if ( !_initialized )
            {
                try
                {
                    _random = SecureRandom.getInstance( SESSION_ID_RANDOM_ALGORITHM );
                }
                catch ( NoSuchAlgorithmException e )
                {
                    _random = new Random();
                }
                _random.setSeed(
                    _random.nextLong() ^ System.currentTimeMillis() ^ hashCode() ^ Runtime.getRuntime().freeMemory() );
                _initialized = true;
            }
        }
    }
}
