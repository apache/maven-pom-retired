package org.apache.maven.continuum.web.action;

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

import org.codehaus.plexus.rememberme.RememberMeServices;
import org.codehaus.plexus.security.Authentication;
import org.codehaus.plexus.security.Authenticator;
import org.codehaus.plexus.security.DefaultAuthentication;
import org.codehaus.plexus.security.User;

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.webwork.ServletActionContext;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class LoginAction
    extends ActionSupport
{
    private Authenticator authenticator;

    private RememberMeServices rememberMeServices;

    private String username = "";

    private String password = "";

    private boolean rememberMe = false;

    /**
     * Execute the login action
     */
    public String execute()
        throws Exception
    {
        try
        {
            Map params = new HashMap();

            params.put( "username", username );

            params.put( "password", password );

            User user = authenticator.authenticate( params );

            if ( rememberMe )
            {
                Authentication auth = new DefaultAuthentication();

                auth.setUser( user );

                auth.setAuthenticated( true );

                rememberMeServices.loginSuccess( ServletActionContext.getRequest(),
                    ServletActionContext.getResponse(), auth );
            }

            HttpSession session = ServletActionContext.getRequest().getSession( true );

            session.setAttribute( "authentication", user );

            return SUCCESS;
        }
        catch ( Exception e )
        {
            addActionError( "Login failed. " + e.getMessage() );

            if ( rememberMe )
            {
                rememberMeServices.loginFail( ServletActionContext.getRequest(),
                    ServletActionContext.getResponse() );
            }

            return INPUT;
        }
    }

    /**
     * Redirect to login view
     */
    public String doDefault()
    {
        return INPUT;
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
