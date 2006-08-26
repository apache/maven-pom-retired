package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.ContinuumUser;

import java.util.Iterator;
import java.util.List;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="session"
 */
public class SessionAction
    extends ContinuumActionSupport
{
    private String username;

    private String password;

    private boolean login;

    private boolean rememberMe = false;

    /**
     * Execute the login action
     */
    public String login()
    {
        try
        {
            if ( !login )
            {
                return INPUT;
            }

            List userList = getContinuum().getUsers();

            for ( Iterator i = userList.iterator(); i.hasNext(); )
            {
                ContinuumUser user = (ContinuumUser)i.next();

                if ( username != null && username.equals( user.getUsername() ) )
                {
                    if ( user.equalsPassword( password ) )
                    {
                        session.put( "userId", new Integer( user.getAccountId() ) );
                        session.put( "loggedIn", new Boolean( true ) );
                        return SUCCESS;
                    }
                    else
                    {
                        getLogger().info("invalid password");
                        addActionError( "invalid password" );
                        return ERROR;
                    }
                }
            }
            getLogger().info("no user by name: " + username );
            return ERROR;
        }
        catch ( ContinuumException  e )
        {
            getLogger().info(e.getMessage() );
            addActionError( e.getMessage() );
            return ERROR;
        }

    }

    public String logout()
    {
        session.clear();

        return SUCCESS;
    }

    public boolean isLogin()
    {
        return login;
    }

    public void setLogin( boolean login )
    {
        this.login = login;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public boolean isRememberMe()
    {
        return rememberMe;
    }

    public void setRememberMe( boolean rememberMe )
    {
        this.rememberMe = rememberMe;
    }
}
