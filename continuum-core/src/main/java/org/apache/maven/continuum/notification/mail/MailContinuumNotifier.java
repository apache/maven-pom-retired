package org.apache.maven.continuum.notification.mail;

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
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.notification.ContinuumRecipientSource;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.mailsender.MailMessage;
import org.codehaus.plexus.mailsender.MailSender;
import org.codehaus.plexus.mailsender.MailSenderException;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.velocity.VelocityComponent;

import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class MailContinuumNotifier
    extends AbstractContinuumNotifier
    implements Initializable
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private VelocityComponent velocity;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.configuration
     */
    private ContinuumStore store;

    /**
     * @plexus.configuration
     */
    private MailSender mailSender;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @plexus.configuration
     */
    private String fromMailbox;

    /**
     * @plexus.configuration
     */
    private String fromName;

    /**
     * @plexus.configuration
     */
    private String timestampFormat;

    /**
     * @plexus.configuration
     */
    private boolean includeBuildResult = true;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String buildHost;

    private FormatterTool formatterTool;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static final String FALLBACK_FROM_MAILBOX = "continuum@localhost";

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        try
        {
            InetAddress address = InetAddress.getLocalHost();

            buildHost = StringUtils.clean( address.getHostName() );

            if ( buildHost == null )
            {
                buildHost = "localhost";
            }
        }
        catch ( UnknownHostException ex )
        {
            fromName = "Continuum";
        }

        // ----------------------------------------------------------------------
        // From mailbox
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( fromMailbox ) )
        {
            getLogger().info( "The from mailbox is not configured, will use the nag email address from the project." );

            fromMailbox = null;
        }
        else
        {
            getLogger().info( "Using '" + fromMailbox + "' as the from mailbox for all emails." );
        }

        if ( StringUtils.isEmpty( fromName ) )
        {
            fromName = "Continuum@" + buildHost;
        }

        getLogger().info( "From name: " + fromName );

        getLogger().info( "Build host name: " + buildHost );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        formatterTool = new FormatterTool( timestampFormat );
    }

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source, Set recipients, Map configuration, Map context )
        throws NotificationException
    {
        Project project = (Project) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        BuildResult build = (BuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        String buildOutput = (String) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD_OUTPUT );

        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------

        if ( build == null )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Generate and send email
        // ----------------------------------------------------------------------

        try
        {
            if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
            {
                buildComplete( project, build, buildOutput, source, recipients, configuration );
            }
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Error while notifiying.", e );
        }
    }

    private void buildComplete( Project project, BuildResult build, String buildOutput, String source, Set recipients,
                                Map configuration )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Check if the mail should be sent at all
        // ----------------------------------------------------------------------

        BuildResult previousBuild = getPreviousBuild( project, build );

        if ( !shouldNotify( build, previousBuild ) )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Generate the mail contents
        // ----------------------------------------------------------------------

        String packageName = getClass().getPackage().getName().replace( '.', '/' );

        String templateName = "/" + packageName + "/templates/" + project.getExecutorId() + "/" + source + ".vm";

        StringWriter writer = new StringWriter();

        String content;

        try
        {
            if ( includeBuildResult )
            {
                VelocityContext context = new VelocityContext();

                // ----------------------------------------------------------------------
                // Data objects
                // ----------------------------------------------------------------------

                context.put( "project", project );

                context.put( "build", build );

                context.put( "buildOutput", buildOutput );

                context.put( "previousBuild", previousBuild );

                // ----------------------------------------------------------------------
                // Tools
                // ----------------------------------------------------------------------

                context.put( "formatter", formatterTool );

                // TODO: Make the build host a part of the build

                context.put( "buildHost", buildHost );

                String osName = System.getProperty( "os.name" );

                String osPatchLevel = System.getProperty( "sun.os.patch.level" );

                if ( osPatchLevel != null )
                {
                    osName = osName + "(" + osPatchLevel + ")";
                }

                context.put( "osName", osName );

                context.put( "javaVersion", System.getProperty( "java.version" ) + "("
                    + System.getProperty( "java.vendor" ) + ")" );

                // ----------------------------------------------------------------------
                // Generate
                // ----------------------------------------------------------------------

                velocity.getEngine().mergeTemplate( templateName, context, writer );

                content = writer.getBuffer().toString();
            }
            else
            {
                content = getReportUrl( project, build, configurationService );
            }
        }
        catch ( ResourceNotFoundException e )
        {
            getLogger().info( "No such template: '" + templateName + "'." );

            return;
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while generating mail contents.", e );
        }

        // ----------------------------------------------------------------------
        // Send the mail
        // ----------------------------------------------------------------------

        String subject = generateSubject( project, build );

        sendMessage( project, recipients, subject, content, configuration );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String generateSubject( Project project, BuildResult build )
    {
        int state = build.getState();

        if ( state == ContinuumProjectState.OK )
        {
            return "[continuum] BUILD SUCCESSFUL: " + project.getName();
        }
        else if ( state == ContinuumProjectState.FAILED )
        {
            return "[continuum] BUILD FAILURE: " + project.getName();
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            return "[continuum] BUILD ERROR: " + project.getName();
        }
        else
        {
            getLogger().warn( "Unknown build state " + build.getState() + " for project " + project.getId() );

            return "[continuum] ERROR: Unknown build state " + build.getState() + " for " + project.getName() + " project";
        }
    }

    private void sendMessage( Project project, Set recipients, String subject, String content, Map configuration )
        throws ContinuumException
    {
        if ( recipients.size() == 0 )
        {
            // This is a useful message for the users when debugging why they don't
            // receive any mails

            getLogger().info( "No mail recipients for '" + project.getName() + "'." );

            return;
        }

        String fromMailbox = getFromMailbox( configuration );

        if ( fromMailbox == null )
        {
            getLogger().warn( project.getName() +
                ": Project is missing nag email and global from mailbox is missing, not sending mail." );

            return;
        }

        MailMessage message = new MailMessage();

        message.addHeader( "X-Continuum-Build-Host", buildHost );

        message.addHeader( "X-Continuum-Project-Id", Integer.toString( project.getId() ) );

        message.addHeader( "X-Continuum-Project-Name", project.getName() );

        try
        {
            message.setSubject( subject );

            message.setContent( content );

            MailMessage.Address from = new MailMessage.Address( fromMailbox, fromName );

            message.setFrom( from );

            getLogger().info( "Sending message: From '" + from + "'." );

            for ( Iterator it = recipients.iterator(); it.hasNext(); )
            {
                String mailbox = (String) it.next();

                // TODO: set a proper name
                MailMessage.Address to = new MailMessage.Address( mailbox );

                getLogger().info( "Recipient: To '" + to + "'." );

                message.addTo( to );
            }

            mailSender.send( message );
        }
        catch ( MailSenderException ex )
        {
            throw new ContinuumException( "Exception while sending message.", ex );
        }
    }

    private String getFromMailbox( Map configuration )
    {
        if ( fromMailbox != null )
        {
            return fromMailbox;
        }

        String address = null;

        if ( configuration != null )
        {
            address = (String) configuration.get( ContinuumRecipientSource.ADDRESS_FIELD );
        }

        if ( StringUtils.isEmpty( address ) )
        {
            return FALLBACK_FROM_MAILBOX;
        }

        return address;
    }

    private boolean shouldNotify( BuildResult build, BuildResult previousBuild )
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
            "Current build state: " + build.getState() + ", previous build state: " + previousBuild.getState() );

        if ( build.getState() != previousBuild.getState() )
        {
            return true;
        }

        getLogger().info( "Same state, not sending mail." );

        return false;
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
