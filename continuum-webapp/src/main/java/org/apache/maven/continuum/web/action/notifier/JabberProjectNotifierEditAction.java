package org.apache.maven.continuum.web.action.notifier;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that deletes a {@link ProjectNotifier} of type 'Jabber' from the 
 * specified {@link Project}.
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: JabberNotifierEditAction.java 465060 2006-10-17 21:24:38Z jmcconnell $
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="jabberProjectNotifierEdit"
 */
public class JabberProjectNotifierEditAction
    extends AbstractProjectNotifierEditAction
{
    private String host;

    private int port = 5222;

    private String login;

    private String password;

    private String domainName;

    private String address;

    private boolean sslConnection;

    private boolean group;

    protected void initConfiguration( Map configuration )
    {
        host = (String) configuration.get( "host" );

        if ( configuration.get( "port" ) != null )
        {
            port = Integer.parseInt( (String) configuration.get( "port" ) );
        }

        login = (String) configuration.get( "login" );

        password = (String) configuration.get( "password" );

        domainName = (String) configuration.get( "domainName" );

        address = (String) configuration.get( "address" );

        sslConnection = Boolean.valueOf( (String) configuration.get( "sslConnection" ) ).booleanValue();

        group = Boolean.valueOf( (String) configuration.get( "isGroup" ) ).booleanValue();
    }

    protected void setNotifierConfiguration( ProjectNotifier notifier )
    {
        HashMap configuration = new HashMap();

        configuration.put( "host", host );

        configuration.put( "port", String.valueOf( port ) );

        configuration.put( "login", login );

        configuration.put( "password", password );

        configuration.put( "domainName", domainName );

        configuration.put( "address", address );

        configuration.put( "sslConnection", String.valueOf( sslConnection ) );

        configuration.put( "isGroup", String.valueOf( group ) );

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

    public String getLogin()
    {
        return login;
    }

    public void setLogin( String login )
    {
        this.login = login;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress( String address )
    {
        this.address = address;
    }

    public boolean isSslConnection()
    {
        return sslConnection;
    }

    public void setSslConnection( boolean sslConnection )
    {
        this.sslConnection = sslConnection;
    }

    public boolean isGroup()
    {
        return group;
    }

    public void setGroup( boolean group )
    {
        this.group = group;
    }
}
