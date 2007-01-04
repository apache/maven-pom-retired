package org.apache.maven.continuum.notification.jabber;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.jabber.JabberClient;
import org.codehaus.plexus.jabber.JabberClientException;
import org.codehaus.plexus.notification.NotificationException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JabberContinuumNotifier
    extends AbstractContinuumNotifier
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private JabberClient jabberClient;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @plexus.configuration
     */
    private String fromAddress;

    /**
     * @plexus.configuration
     */
    private String fromPassword;

    /**
     * @plexus.configuration
     */
    private String host;

    /**
     * @plexus.configuration
     */
    private int port;

    /**
     * @plexus.configuration
     */
    private String imDomainName;

    /**
     * @plexus.configuration
     */
    private boolean sslConnection;

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source, Set recipients, Map configuration, Map context )
        throws NotificationException
    {
        Project project = (Project) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ProjectNotifier projectNotifier =
            (ProjectNotifier) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT_NOTIFIER );

        BuildResult build = (BuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null )
        {
            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        if ( recipients.size() == 0 )
        {
            getLogger().info( "No Jabber recipients for '" + project.getName() + "'." );

            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            sendMessage( project, projectNotifier, build, recipients, configuration );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String generateMessage( Project project, BuildResult build )
        throws ContinuumException
    {
        int state = project.getState();

        if ( build != null )
        {
            state = build.getState();
        }

        String message;

        if ( state == ContinuumProjectState.OK )
        {
            message = "BUILD SUCCESSFUL: " + project.getName();
        }
        else if ( state == ContinuumProjectState.FAILED )
        {
            message = "BUILD FAILURE: " + project.getName();
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            message = "BUILD ERROR: " + project.getName();
        }
        else
        {
            getLogger().warn( "Unknown build state " + state + " for project " + project.getId() );

            message = "ERROR: Unknown build state " + state + " for " + project.getName() + " project";
        }

        return message + " " + getReportUrl( project, build, configurationService );
    }

    private void sendMessage( Project project, ProjectNotifier projectNotifier, BuildResult build, Set recipients,
                              Map configuration )
        throws NotificationException
    {
        String message;

        // ----------------------------------------------------------------------
        // Check if the mail should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, build );

        if ( !shouldNotify( build, previousBuild, projectNotifier ) )
        {
            return;
        }

        try
        {
            message = generateMessage( project, build );
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Can't generate the message.", e );
        }

        jabberClient.setHost( getHost( configuration ) );

        jabberClient.setPort( getPort( configuration ) );

        jabberClient.setUser( getUsername( configuration ) );

        jabberClient.setPassword( getPassword( configuration ) );

        jabberClient.setImDomainName( getImDomainName( configuration ) );

        jabberClient.setSslConnection( isSslConnection( configuration ) );

        try
        {
            jabberClient.connect();

            jabberClient.logon();

            for ( Iterator i = recipients.iterator(); i.hasNext(); )
            {
                String recipient = (String) i.next();

                if ( isGroup( configuration ) )
                {
                    jabberClient.sendMessageToGroup( recipient, message );
                }
                else
                {
                    jabberClient.sendMessageToUser( recipient, message );
                }
            }
        }
        catch ( JabberClientException e )
        {
            throw new NotificationException( "Exception while sending message.", e );
        }
        finally
        {
            try
            {
                jabberClient.logoff();
            }
            catch ( JabberClientException e )
            {

            }
        }
    }

    private BuildResult getPreviousBuild( Project project, BuildResult currentBuild )
        throws NotificationException
    {
        try
        {
            // TODO: prefer to remove this and get them up front
            if ( project.getId() > 0 )
            {
                project = store.getProjectWithBuilds( project.getId() );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new NotificationException( "Unable to obtain project builds", e );
        }
        List builds = project.getBuildResults();

        if ( builds.size() < 2 )
        {
            return null;
        }

        BuildResult build = (BuildResult) builds.get( builds.size() - 1 );

        if ( currentBuild != null && build.getId() != currentBuild.getId() )
        {
            throw new NotificationException( "INTERNAL ERROR: The current build wasn't the first in the build list. " +
                "Current build: '" + currentBuild.getId() + "', " + "first build: '" + build.getId() + "'." );
        }

        return (BuildResult) builds.get( builds.size() - 2 );
    }

    public void sendNotification( String arg0, Set arg1, Properties arg2 )
        throws NotificationException
    {
        throw new NotificationException( "Not implemented." );
    }

    private String getHost( Map configuration )
    {
        if ( configuration.containsKey( "host" ) )
        {
            return (String) configuration.get( "host" );
        }
        else
        {
            if ( configuration.containsKey( "address" ) )
            {
                String username = (String) configuration.get( "address" );

                if ( username.indexOf( "@" ) > 0 )
                {
                    return username.substring( username.indexOf( "@" ) + 1 );
                }
            }
        }

        return host;
    }

    private int getPort( Map configuration )
    {
        if ( configuration.containsKey( "port" ) )
        {
            try
            {
                return Integer.parseInt( (String) configuration.get( "port" ) );
            }
            catch ( NumberFormatException e )
            {
                getLogger().error( "jabber port isn't a number.", e );
            }
        }

        if ( port > 0 )
        {
            return port;
        }
        else if ( isSslConnection( configuration ) )
        {
            return 5223;
        }
        else
        {
            return 5222;
        }
    }

    private String getUsername( Map configuration )
    {
        if ( configuration.containsKey( "login" ) )
        {
            String username = (String) configuration.get( "login" );

            if ( username.indexOf( "@" ) > 0 )
            {
                username = username.substring( 0, username.indexOf( "@" ) );
            }

            return username;
        }

        return fromAddress;
    }

    private String getPassword( Map configuration )
    {
        if ( configuration.containsKey( "password" ) )
        {
            return (String) configuration.get( "password" );
        }

        return fromPassword;
    }

    private boolean isSslConnection( Map configuration )
    {
        if ( configuration.containsKey( "sslConnection" ) )
        {
            return convertBoolean( (String) configuration.get( "sslConnection" ) );
        }

        return sslConnection;
    }

    private String getImDomainName( Map configuration )
    {
        if ( configuration.containsKey( "domainName" ) )
        {
            return (String) configuration.get( "domainName" );
        }

        return imDomainName;
    }

    private boolean isGroup( Map configuration )
    {
        if ( configuration.containsKey( "isGroup" ) )
        {
            return convertBoolean( (String) configuration.get( "isGroup" ) );
        }
        else
        {
            return false;
        }
    }

    private boolean convertBoolean( String value )
    {
        if ( "true".equalsIgnoreCase( value ) || "on".equalsIgnoreCase( value ) || "yes".equalsIgnoreCase( value ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
