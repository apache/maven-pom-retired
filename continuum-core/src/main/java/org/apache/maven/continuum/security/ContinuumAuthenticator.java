package org.apache.maven.continuum.security;

/*
 * Copyright 2005 The Apache Software Foundation.
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
 *
 */

import org.codehaus.plexus.security.Authenticator;
import org.codehaus.plexus.security.exception.UnknownEntityException;
import org.codehaus.plexus.security.exception.AuthenticationException;
import org.codehaus.plexus.security.exception.UnauthorizedException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.model.system.ContinuumUser;

import java.util.Map;

/**
 * TODO: Move this to o.a.m.c.security once plexus-security doesn't depend on plexus-summit.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumAuthenticator
    implements Authenticator
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // Authenticator Implementation
    // ----------------------------------------------------------------------

    public Object authenticate( Map tokens )
        throws UnknownEntityException, AuthenticationException, UnauthorizedException
    {
        String username = (String) tokens.get( "username" );
        String password = (String) tokens.get( "password" );

        ContinuumUser user = getUser( username );

        if ( user == null )
        {
            throw new UnknownEntityException();
        }

        System.err.println( "username: " + username );
        System.err.println( "password: " + password );
        System.err.println( "user.password: " + user.getPassword() );

        if ( !user.getPassword().equals( password ) )
        {
            throw new AuthenticationException( "Invalid password." );
        }

        return null;
    }

    public Object getAnonymousEntity()
    {
        throw new RuntimeException( "Not implemented" );
    }

    // ----------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------

    private ContinuumUser getUser( String username )
        throws AuthenticationException
    {
        try
        {
            return store.getUserByUsername( username );
        }
        catch ( ContinuumStoreException e )
        {
            throw new AuthenticationException( "Error while retreiving user.", e );
        }
    }
}
