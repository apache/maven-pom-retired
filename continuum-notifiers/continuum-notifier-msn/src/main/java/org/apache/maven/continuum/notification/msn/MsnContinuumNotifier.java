package org.apache.maven.continuum.notification.msn;

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

import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.codehaus.plexus.msn.MsnClient;
import org.codehaus.plexus.msn.MsnException;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.notifier.AbstractNotifier;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: $
 */
public class MsnContinuumNotifier
    extends AbstractNotifier
    implements Initializable
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /** @plexus.requirement */
    private MsnClient msnClient;

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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Map configuration;

    private Set recipients;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
    }

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source, Set recipients, Map configuration, Map context )
        throws NotificationException
    {
        this.configuration = configuration;

        this.recipients = recipients;

        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ContinuumBuild build = (ContinuumBuild) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_STARTED ) )
        {
            buildStarted( project );
        }
        else if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_CHECKOUT_STARTED ) )
        {
            checkoutStarted( project );
        }
        else if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_CHECKOUT_COMPLETE ) )
        {
            checkoutComplete( project );
        }
        else if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_RUNNING_GOALS ) )
        {
            runningGoals( project, build );
        }
        else if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_GOALS_COMPLETED ) )
        {
            goalsCompleted( project, build );
        }
        else if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            buildComplete( project, build );
        }
        else
        {
            getLogger().warn( "Unknown source: '" + source + "'." );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void buildStarted( ContinuumProject project )
    throws NotificationException
    {
        sendMessage( project, null, "Build started." );
    }

    private void checkoutStarted( ContinuumProject project )
    throws NotificationException
    {
        sendMessage( project, null, "Checkout started." );
    }

    private void checkoutComplete( ContinuumProject project )
    throws NotificationException
    {
        sendMessage( project, null, "Checkout complete." );
    }

    private void runningGoals( ContinuumProject project, ContinuumBuild build )
    throws NotificationException
    {
        sendMessage( project, build, "Running goals." );
    }

    private void goalsCompleted( ContinuumProject project, ContinuumBuild build )
    throws NotificationException
    {
        if ( build.getError() == null )
        {
            sendMessage( project, build, "Goals completed. state: " + build.getState() );
        }
        else
        {
            sendMessage( project, build, "Goals completed." );
        }
    }

    private void buildComplete( ContinuumProject project, ContinuumBuild build )
    throws NotificationException
    {
        if ( build.getError() == null )
        {
            sendMessage( project, build, "Build complete. state: " + build.getState() );
        }
        else
        {
            sendMessage( project, build, "Build complete." );
        }
    }

    private void sendMessage( ContinuumProject project, ContinuumBuild build, String msg )
        throws NotificationException
    {
        String message = "Build event for project '" + project.getName() + "':" + msg;

        msnClient.setLogin( getUsername() );

        msnClient.setPassword( getPassword() );

        try
        {
            msnClient.login();

            for ( Iterator i = recipients.iterator(); i.hasNext(); )
            {
                String recipient = (String) i.next();

                msnClient.sendMessage( recipient, message );
            }
        }
        catch ( MsnException e )
        {
            throw new NotificationException( "Exception while sending message.", e );
        }
        finally
        {
            try
            {
                msnClient.logout();
            }
            catch ( MsnException e )
            {

            }
        }

        if ( build != null && !StringUtils.isEmpty( build.getError() ) )
        {
            System.out.println( build.getError() );
        }
    }

    /**
     * @see org.codehaus.plexus.notification.notifier.Notifier#sendNotification(java.lang.String, java.util.Set, java.util.Properties)
     */
    public void sendNotification( String arg0, Set arg1, Properties arg2 )
        throws NotificationException
    {
        throw new NotificationException( "Not implemented." );
    }

    private String getUsername()
    {
        if ( configuration.containsKey( "address" ) )
        {
            String username = (String) configuration.get( "address" );

            if ( username.indexOf( "@" ) > 0 )
            {
                username = username.substring( 0, username.indexOf( "@" ) );
            }

            return username;
        }

        return fromAddress;
    }

    private String getPassword()
    {
        if ( configuration.containsKey( "password" ) )
        {
            String password = (String) configuration.get( "password" );

            return password;
        }

        return fromPassword;
    }
}
