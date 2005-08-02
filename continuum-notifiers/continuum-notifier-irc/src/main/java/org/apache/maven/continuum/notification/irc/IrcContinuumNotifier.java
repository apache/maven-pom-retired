package org.apache.maven.continuum.notification.irc;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.ircbot.IrcBot;

import java.util.Collection;
import java.util.Iterator;
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

    public void sendNotification( String source,
                                  Set recipients,
                                  Map configuration,
                                  Map context )
        throws NotificationException
    {
        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ContinuumBuild build = (ContinuumBuild) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

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
                buildComplete( project, build, configuration );
            }
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Error while notifiying.", e );
        }
    }

    private void buildComplete( ContinuumProject project,
                                ContinuumBuild build,
                                Map configuration )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Check if the message should be sent at all
        // ----------------------------------------------------------------------

        ContinuumBuild previousBuild = getPreviousBuild( project, build );

        if ( !shouldNotify( build, previousBuild ) )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Gather configuration values
        // ----------------------------------------------------------------------

        String host = (String) configuration.get( "host" );

        int port = Integer.parseInt( (String) configuration.get( "port" ) );

        String channel = (String) configuration.get( "channel" );

        // ----------------------------------------------------------------------
        // Send message
        // ----------------------------------------------------------------------

        try
        {
            ircClient.connect( host, port, "continuum" );

            ircClient.logon();

            ircClient.sendMessageToChannel( channel, generateMessage( project, build ) );
        }
        catch( Exception e )
        {
            throw new ContinuumException( "Exception while sending message.", e );
        }
        finally
        {
            try
            {
                ircClient.logoff();
            }
            catch( Exception e )
            {
                throw new ContinuumException( "Exception while logoff.", e );
            }
            finally
            {
                try
                {
                    ircClient.disconnect();
                }
                catch( Exception e )
                {
                    throw new ContinuumException( "Exception while disconnecting.", e );
                }
            }
        }
    }

    private String generateMessage( ContinuumProject project, ContinuumBuild build )
        throws ContinuumException
    {
        int state = build.getState();

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
            getLogger().warn( "Unknown build state " + build.getState() + " for project " + project.getId() );

            message = "ERROR: Unknown build state " + build.getState();
        }

        return message + " " + getReportUrl( project, build, configurationService );
    }

    private boolean shouldNotify( ContinuumBuild build, ContinuumBuild previousBuild )
    {
        if ( build == null )
        {
            return true;
        }

        // Always send if the project failed
        if ( build.getState() == ContinuumProjectState.FAILED || build.getState() == ContinuumProjectState.ERROR )
        {
            return true;
        }

        // Send if this is the first build
        if ( previousBuild == null )
        {
            return true;
        }

        // Send if the state has changed
        getLogger().info(
                          "Current build state: " + build.getState() + ", previous build state: "
                              + previousBuild.getState() );

        if ( build.getState() != previousBuild.getState() )
        {
            return true;
        }

        getLogger().info( "Same state, not sending message." );

        return false;
    }

    private ContinuumBuild getPreviousBuild( ContinuumProject project, ContinuumBuild currentBuild )
        throws ContinuumException
    {
        Collection builds;

        try
        {
            builds = store.getBuildsForProject( project.getId(), 0, 0 );
        }
        catch ( ContinuumStoreException ex )
        {
            throw new ContinuumException( "Error while finding the last project build.", ex );
        }

        if ( builds.size() == 0 )
        {
            return null;
        }

        Iterator itr = builds.iterator();
        ContinuumBuild build = (ContinuumBuild) itr.next();

        if ( currentBuild != null && !build.getId().equals( currentBuild.getId() ) )
        {
            throw new ContinuumException( "INTERNAL ERROR: The current build wasn't the first in the build list. "
                                          + "Current build: '" + currentBuild.getId() + "', " + "first build: '"
                                          + build.getId() + "'." );
        }

        if ( !itr.hasNext() )
        {
            return null;
        }

        return (ContinuumBuild) itr.next();
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
