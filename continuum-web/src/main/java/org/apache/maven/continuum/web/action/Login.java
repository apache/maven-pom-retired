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

import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.web.model.SessionUser;
import org.codehaus.plexus.security.summit.SecureRunData;
import org.codehaus.plexus.security.Authenticator;
import org.codehaus.plexus.security.exception.UnknownEntityException;
import org.codehaus.plexus.security.exception.UnauthorizedException;
import org.codehaus.plexus.security.exception.AuthenticationException;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class Login
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private Authenticator authenticator;

    public void execute( Map map )
        throws Exception
    {
        SecureRunData data = (SecureRunData) map.get( "data" );

        String username = (String) map.get( "login.username" );

        getLogger().info( "Trying to log in '" + username + "'." );

        String password = (String) map.get( "login.password" );

        if ( StringUtils.isEmpty( username ) || StringUtils.isEmpty( password ) )
        {
            data.getViewContext().put( "loginMessage", "Both username and password has to be supplied.");
            data.setTarget( "Login.vm" );
            return;
        }

        // ----------------------------------------------------------------------
        // Authenticate the user
        // ----------------------------------------------------------------------

        Map tokens = new HashMap();

        tokens.put( "username", username );
        tokens.put( "password", password );

        try
        {
            authenticator.authenticate( tokens );
        }
        catch ( UnknownEntityException e )
        {
            // TODO: Internationalize
            data.getViewContext().put( "loginMessage", "Unknown user '" + username + "'.");
            data.setTarget( "Login.vm" );

            return;
        }
        catch ( AuthenticationException e )
        {
            // TODO: Internationalize
            data.getViewContext().put( "loginMessage", "Could not authenticate: " + e.getMessage() );
            data.setTarget( "Login.vm" );

            return;
        }
        catch ( UnauthorizedException e )
        {
            data.getViewContext().put( "loginMessage", "User '" + username + "' is not authorized .");
            data.setTarget( "Login.vm" );

            return;
        }

        ContinuumUser user = store.getUserByUsername( username );

        SessionUser usr = new SessionUser( user.getAccountId(), user.getUsername() );

        usr.setFullName( user.getFullName() );

        usr.setLoggedIn( true );

        data.setUser( usr );

        data.setTarget( "Summary.vm" );
    }
}
