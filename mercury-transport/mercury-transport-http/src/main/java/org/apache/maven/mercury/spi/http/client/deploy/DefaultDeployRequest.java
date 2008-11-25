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
package org.apache.maven.mercury.spi.http.client.deploy;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DefaultDeployRequest
implements DeployRequest
{
    private Set<Binding> _bindings = new HashSet<Binding>();
    private boolean _failFast;
    
    public Set<Validator> getValidators()
    {
        return null;
    }
    public void setBindings(Set<Binding> bindings)
    {
        _bindings=bindings;
    }
    public Set<Binding> getBindings()
    {
        return _bindings;
    }

    public boolean isFailFast()
    {
        return _failFast;
    }
    
    public void setFailFast (boolean f)
    {
        _failFast=f;
    }
}
