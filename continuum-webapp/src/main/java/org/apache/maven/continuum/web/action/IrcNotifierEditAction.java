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

import org.apache.maven.continuum.model.project.ProjectNotifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="ircNotifierEdit"
 */
public class IrcNotifierEditAction
    extends AbstractNotifierEditAction
{
    private String host;

    private int port = 6667;

    private String channel;

    private String nick;

    private String fullName;

    private String password;

    protected void initConfiguration( Map configuration )
    {
        host = (String) configuration.get( "host" );

        if ( configuration.get( "port" ) != null )
        {
            port = Integer.parseInt( (String) configuration.get( "port" ) );
        }

        channel = (String) configuration.get( "channel" );

        nick = (String) configuration.get( "nick" );

        fullName = (String) configuration.get( "fullName" );

        password = (String) configuration.get( "password" );
    }

    protected void setNotifierConfiguration( ProjectNotifier notifier )
    {
        HashMap configuration = new HashMap();

        configuration.put( "host", host );

        configuration.put( "port", String.valueOf( port ) );

        configuration.put( "channel", channel );

        configuration.put( "nick", nick );

        configuration.put( "fullName", fullName );

        configuration.put( "password", password );

        notifier.setConfiguration( configuration );
    }

    public String getHost()
    {
        return host;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel( String channel )
    {
        this.channel = channel;
    }

    public String getNick()
    {
        return nick;
    }

    public void setNick( String nick )
    {
        this.nick = nick;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }
}
