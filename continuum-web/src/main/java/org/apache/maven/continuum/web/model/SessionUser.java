package org.apache.maven.continuum.web.model;

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

import org.codehaus.plexus.security.summit.User;
import org.codehaus.plexus.security.summit.session.SessionBindingEvent;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SessionUser
    implements User
{
    private int userId;

    private String username;

    private String fullName;

    private boolean loggedIn;

    public SessionUser( int id, String username )
    {
        this.userId = id;

        this.username = username;
    }

    public int getUserId()
    {
        return userId;
    }

    public String getUserName()
    {
        return username;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName)
    {
        this.fullName =  fullName;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public void setLoggedIn( boolean loggedIn )
    {
        this.loggedIn = loggedIn;
    }

    public void updateLastAccessDate()
    {
    }

    public void incrementAccessCounter()
    {
    }

    public void incrementAccessCounterForSession()
    {
    }

    public void setTemp( String key, Object value )
    {
    }

    public Object getTemp( String key )
    {
        return null;
    }

    public void setPerm( String key, Object value )
    {
    }

    public Object getPerm( String key )
    {
        return null;
    }

    public void valueBound(SessionBindingEvent event)
    {
    }

    public void valueUnbound(SessionBindingEvent event)
    {
    }
}
