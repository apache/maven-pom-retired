package org.apache.maven.continuum.web.rememberme;

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
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.rememberme.AbstractRememberMeServices;
import org.codehaus.plexus.rememberme.UsernameNotFoundException;
import org.codehaus.plexus.security.DefaultUser;
import org.codehaus.plexus.security.User;

public class DefaultRememberMeServices
    extends AbstractRememberMeServices
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    protected User getUserByUserName( String username )
        throws UsernameNotFoundException
    {
        ContinuumUser user = null;

        try
        {
            user = store.getUserByUsername( username );
        }
        catch ( ContinuumStoreException e )
        {
            throw new UsernameNotFoundException( "Error while retreiving user.", e );
        }

        if ( user == null )
        {
            throw new UsernameNotFoundException( "User doesn't exist." );
        }

        DefaultUser u = new DefaultUser();

        u.setUsername( user.getUsername() );

        u.setPassword( user.getPassword() );

        u.setFullName( user.getFullName() );

        u.setEmail( user.getEmail() );

        u.setEnabled( true );

        u.setAccountNonExpired( true );

        u.setAccountNonLocked( true );

        u.setPasswordNonExpired( true );

        u.setDetails( user );

        return u;
    }
}
