package org.apache.maven.continuum.web.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.web.model.SessionUser;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Map;
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
 */

/**
 * UserInterceptor:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id:$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.interceptor.Interceptor"
 *   role-hint="userInterceptor"
 */
public class UserInterceptor
    extends AbstractLogEnabled
    implements Interceptor
{
    /**
     * @plexus.requirement
     */
    private Continuum continuum;


    /**
     *
     * @param actionInvocation
     * @return
     * @throws Exception
     */

    public String intercept( ActionInvocation actionInvocation )
        throws Exception
    {
        Map session  = actionInvocation.getInvocationContext().getSession();

        Integer userId = (Integer)session.get( "userId" );

        ContinuumUser continuumUser;

        if ( userId != null )
        {
            continuumUser = continuum.getUser( userId.intValue() );
        }
        else
        {
            continuumUser = continuum.getSecurity().getGuestUser();
        }

        if ( continuumUser != null)
        {

            SessionUser sessionUser = new SessionUser();

            sessionUser.setId( continuumUser.getAccountId() );
            sessionUser.setUsername( continuumUser.getUsername() );
            sessionUser.setFullName( continuumUser.getFullName() );

            Boolean loggedIn = (Boolean)session.get( "loggedIn" );

            if ( loggedIn != null )
            {
                sessionUser.setLoggedIn( loggedIn.booleanValue() );
            }
            else
            {
                sessionUser.setLoggedIn( false );
            }

            session.put( "user", sessionUser );
            session.put( "userId", new Integer( sessionUser.getId() ));

            actionInvocation.getInvocationContext().setSession( session );

            return actionInvocation.invoke();
        }
        else
        {
            getLogger().info( "error in UserInterceptor" );
            return ActionSupport.ERROR;
        }
    }

    public void destroy()
    {
        // This space left intentionally blank
    }

    public void init()
    {
        // This space left intentionally blank
    }
}
