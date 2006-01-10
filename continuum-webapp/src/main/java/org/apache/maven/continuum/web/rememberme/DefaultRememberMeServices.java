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

import org.codehaus.plexus.rememberme.AbstractRememberMeServices;
import org.codehaus.plexus.rememberme.UsernameNotFoundException;
import org.codehaus.plexus.security.DefaultUser;
import org.codehaus.plexus.security.User;

import com.opensymphony.user.UserManager;
import com.opensymphony.user.EntityNotFoundException;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultRememberMeServices
    extends AbstractRememberMeServices
{
    protected User getUserByUserName( String username )
        throws UsernameNotFoundException
    {
        com.opensymphony.user.User osuser;

        try
        {
            osuser = UserManager.getInstance().getUser( username );
        }
        catch ( EntityNotFoundException e )
        {
            throw new UsernameNotFoundException( "User " + username + " doesn't exist." );
        }

        DefaultUser user = new DefaultUser();

        user.setUsername( osuser.getName() );

        user.setFullName( osuser.getFullName() );

        user.setEmail( osuser.getEmail() );

        user.setDetails( osuser );

        user.setEnabled( true );

        user.setAccountNonExpired( true );

        user.setAccountNonLocked( true );

        user.setPasswordNonExpired( true );

        return user;
    }
}
