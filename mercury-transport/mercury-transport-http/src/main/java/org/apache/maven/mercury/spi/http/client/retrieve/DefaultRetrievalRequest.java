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

package org.apache.maven.mercury.spi.http.client.retrieve;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;

public class DefaultRetrievalRequest implements RetrievalRequest
{
    private boolean _isFailFast;
    private Set<Binding> _bindings;
    private Set<Validator> _validators;
    
    public void setFailFast(boolean failFast)
    {
        _isFailFast = failFast;
    }

    public boolean isFailFast()
    {
        return _isFailFast;
    }
    
    public RetrievalRequest addBinding( Binding binding )
    {
        if ( _bindings == null )
        {
            _bindings = new HashSet<Binding>();
        }
        
        _bindings.add( binding );
        
        return this;
    }
    
    public void setBindings(Set<Binding> bindings)
    {
        _bindings = new HashSet<Binding>(bindings);
    }
    
    public Set<Binding> getBindings()
    {
        return _bindings;
    }

    public void setValidators(Set<Validator> validators)
    {
        _validators = new HashSet<Validator>(validators);
    }
    
    public Set<Validator> getValidators()
    {
        return _validators;
    }
    
    public String toString()
    {
        return _bindings.toString()+"|"+_isFailFast;
    }

}
