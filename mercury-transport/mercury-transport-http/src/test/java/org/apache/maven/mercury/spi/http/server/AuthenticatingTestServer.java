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
package org.apache.maven.mercury.spi.http.server;

import org.mortbay.jetty.security.BasicAuthenticator;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;

public class AuthenticatingTestServer extends SimpleTestServer
{
    private static final String __username = "foo";
    private static final String __password = "bar";
    private static final String __role = "foomeister";
    
    public AuthenticatingTestServer()
    throws Exception
    {
        super();
        
        HashUserRealm realm = new HashUserRealm();
        realm.put (__username, __password);
        realm.addUserToRole(__username, __role);
        realm.setName("foorealm");
        
        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setUserRealm(realm);
        Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{__role});
        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");
        securityHandler.setConstraintMappings(new ConstraintMapping[]{cm});
        context.addHandler(securityHandler);
    }

    public String getUsername()
    {
        return __username;
    }
    
    public String getPassword()
    {
        return __password;
    }
}
