package org.apache.maven.continuum.web.action.notifier;

/*
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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;

/**
 * Action that deletes a {@link ProjectNotifier} of type 'MSN' from the 
 * specified {@link Project}.
 * 
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: MsnNotifierEditAction.java 465060 2006-10-17 21:24:38Z jmcconnell $
 * @since 1.1
 * 
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="msnProjectNotifierEdit"
 */
public class MsnProjectNotifierEditAction
    extends AbstractProjectNotifierEditAction
{
    private String login;

    private String password;

    private String address;

    protected void initConfiguration( Map configuration )
    {
        login = (String) configuration.get( "login" );

        password = (String) configuration.get( "password" );

        address = (String) configuration.get( "address" );
    }

    protected void setNotifierConfiguration( ProjectNotifier notifier )
    {
        HashMap configuration = new HashMap();

        configuration.put( "login", login );

        configuration.put( "password", password );

        configuration.put( "address", address );

        notifier.setConfiguration( configuration );
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin( String login )
    {
        this.login = login;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress( String address )
    {
        this.address = address;
    }
}
