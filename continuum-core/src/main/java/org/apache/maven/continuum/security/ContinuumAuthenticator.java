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

import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.model.system.ContinuumUser;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.opensymphony.user.authenticator.AbstractAuthenticator;
import com.opensymphony.user.authenticator.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ContinuumAuthenticator
    extends AbstractAuthenticator
{
    // ----------------------------------------------------------------------
    // Authenticator Implementation
    // ----------------------------------------------------------------------

    public boolean login(String username, String password, HttpServletRequest req)
        throws AuthenticationException
    {
        PlexusContainer container = (PlexusContainer) req.getAttribute( PlexusConstants.PLEXUS_KEY );

        if ( container == null )
        {
            throw new AuthenticationException( "Can't get plexus container in request." );
        }

        ContinuumUser user = getUser( container, username );

        System.err.println( "username: " + username );

        if ( user != null && user.equalsPassword( password ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------

    private ContinuumUser getUser( PlexusContainer container, String username )
        throws AuthenticationException
    {
        try
        {
            ContinuumStore store = (ContinuumStore) container.lookup( ContinuumStore.ROLE );

            return store.getUserByUsername( username );
        }
        catch ( ContinuumStoreException e )
        {
            throw new AuthenticationException( "Error while retreiving user." + e.getMessage() );
        }
        catch ( ComponentLookupException e )
        {
            throw new AuthenticationException( "Can't get store component." + e.getMessage() );
        }
    }
}
