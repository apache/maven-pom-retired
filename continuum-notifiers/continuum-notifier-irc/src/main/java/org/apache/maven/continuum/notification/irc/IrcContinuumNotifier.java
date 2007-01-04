package org.apache.maven.continuum.notification.irc;

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
import org.codehaus.plexus.ircbot.IrcBot;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class IrcContinuumNotifier
    extends AbstractContinuumNotifier
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.configuration
     */
    private IrcBot ircClient;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

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
        // Generate and send message
        // ----------------------------------------------------------------------

        try
        {
            if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
            {
                buildComplete( project, projectNotifier, build, configuration );
            }
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Error while notifiying.", e );
        }
    }

    private void buildComplete( Project project, ProjectNotifier projectNotifier, BuildResult build, Map configuration )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Check if the message should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, build );

        if ( !shouldNotify( build, previousBuild, projectNotifier ) )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Gather configuration values
        // ----------------------------------------------------------------------

        String host = (String) configuration.get( "host" );

        int port = Integer.parseInt( (String) configuration.get( "port" ) );

        String channel = (String) configuration.get( "channel" );

        String login = (String) configuration.get( "nick" );

        if ( !StringUtils.isEmpty( login ) )
        {
            ircClient.setLogin( login );
        }

        String fullName = (String) configuration.get( "fullName" );

        if ( !StringUtils.isEmpty( fullName ) )
        {
            ircClient.setFullName( fullName );
        }

        String password = (String) configuration.get( "password" );

        if ( !StringUtils.isEmpty( password ) )
        {
            ircClient.setPassword( password );
        }

        // ----------------------------------------------------------------------
        // Send message
        // ----------------------------------------------------------------------

        try
        {
            ircClient.connect( host, port, "continuum" );

            ircClient.logon();

            ircClient.sendMessageToChannel( channel, generateMessage( project, build ) );
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Exception while sending message.", e );
        }
        finally
        {
            try
            {
                ircClient.logoff();
            }
            catch ( Exception e )
            {
                throw new ContinuumException( "Exception while logoff.", e );
            }
            finally
            {
                try
                {
                    ircClient.disconnect();
                }
                catch ( Exception e )
                {
                    throw new ContinuumException( "Exception while disconnecting.", e );
                }
            }
        }
    }

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

    private BuildResult getPreviousBuild( Project project, BuildResult currentBuild )
        throws ContinuumException
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
            throw new ContinuumException( "Unable to obtain project builds", e );
        }
        List builds = project.getBuildResults();

        if ( builds.size() < 2 )
        {
            return null;
        }

        BuildResult build = (BuildResult) builds.get( builds.size() - 1 );

        if ( currentBuild != null && build.getId() != currentBuild.getId() )
        {
            throw new ContinuumException( "INTERNAL ERROR: The current build wasn't the first in the build list. " +
                "Current build: '" + currentBuild.getId() + "', " + "first build: '" + build.getId() + "'." );
        }

        return (BuildResult) builds.get( builds.size() - 2 );
    }

    /**
     * @see org.codehaus.plexus.notification.notifier.Notifier#sendNotification(java.lang.String, java.util.Set, java.util.Properties)
     */
    public void sendNotification( String arg0, Set arg1, Properties arg2 )
        throws NotificationException
    {
        throw new NotificationException( "Not implemented." );
    }
}
