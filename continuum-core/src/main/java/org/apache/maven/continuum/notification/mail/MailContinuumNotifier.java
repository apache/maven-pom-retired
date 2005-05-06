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

import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.mailsender.MailMessage;
import org.codehaus.plexus.mailsender.MailSender;
import org.codehaus.plexus.mailsender.MailSenderException;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.notifier.Notifier;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: MailContinuumNotifier.java,v 1.3 2005/04/02 21:40:04 trygvis Exp $
 */
public class MailContinuumNotifier
    extends AbstractLogEnabled
    implements Initializable, Notifier
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /** @requirement */
    private VelocityComponent velocity;

    /** @configuration */
    private ContinuumStore store;

    /** @configuration */
    private MailSender mailSender;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /** @configuration */
    private String fromMailbox;

    /** @configuration */
    private String fromName;

    /** @configuration */
    private String timestampFormat;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String localHostName;

    private FormatterTool formatterTool;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static final String FALLBACK_FROM_MAILBOX = "continuum@localhost";

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws Exception
    {
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
            try
            {
                InetAddress address = InetAddress.getLocalHost();

                localHostName = address.getCanonicalHostName();

                fromName = "Continuum@" + localHostName;
            }
            catch ( UnknownHostException ex )
            {
                fromName = "Continuum";
            }
        }

        getLogger().info( "From name: " + fromName );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        formatterTool = new FormatterTool( timestampFormat );
    }

    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source, Set recipients, Map context )
        throws NotificationException
    {
        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ContinuumBuild build = (ContinuumBuild) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        ContinuumBuildResult result = (ContinuumBuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD_RESULT );

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
                buildComplete( project, build, result, source, recipients );
            }
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Error while notifiying.", e );
        }
    }

    private void buildComplete( ContinuumProject project, ContinuumBuild build, ContinuumBuildResult buildResult,
                                String source, Set recipients )
        throws ContinuumException
    {
        // ----------------------------------------------------------------------
        // Check if the mail should be sent at all
        // ----------------------------------------------------------------------

        ContinuumBuild previousBuild = getPreviousBuild( project, build );

        if ( !shouldNotify( build, previousBuild ) )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Generate the mail contents
        // ----------------------------------------------------------------------

        String packageName = getClass().getPackage().getName().replace( '.', '/' );

        String templateName = "/" + packageName + "/templates/" + project.getBuilderId() + "/" + source + ".vm";

        StringWriter writer = new StringWriter();

        String content;

        try
        {
            VelocityContext context = new VelocityContext();

            // ----------------------------------------------------------------------
            // Data objects
            // ----------------------------------------------------------------------

            context.put( "project", project );

            context.put( "build", build );

            context.put( "build", build );

            context.put( "previousBuild", previousBuild );

            context.put( "buildResult", buildResult );

            // ----------------------------------------------------------------------
            // Tools
            // ----------------------------------------------------------------------

            context.put( "formatter", formatterTool );

            // TODO: Make the build host a part of the build

            context.put( "buildHost", localHostName );

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            velocity.getEngine().mergeTemplate( templateName, context, writer );

            content = writer.getBuffer().toString();
        }
        catch ( ResourceNotFoundException e )
        {
            getLogger().info( "No such template: '" + templateName + "'." );

            return;
        }
        catch ( Exception e )
        {
            throw new ContinuumException( "Error while generating mail contents." , e );
        }

        // ----------------------------------------------------------------------
        // Send the mail
        // ----------------------------------------------------------------------

        String subject = generateSubject( project );

        sendMessage( project, recipients, subject, content );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String generateSubject( ContinuumProject project )
    {
        int state = project.getState();

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
            getLogger().warn( "Unknown build state " + project.getState() );

            return "[continuum] ERROR: Unknown build state " + project.getState();
        }
    }

    private void sendMessage( ContinuumProject project, Set recipients, String subject, String content )
        throws ContinuumException
    {
        String fromMailbox = getFromMailbox( project );

        if ( fromMailbox == null )
        {
            getLogger().warn( project.getName() + ": Project is missing nag email and global from mailbox is missing, not sending mail." );

            return;
        }

        MailMessage message = new MailMessage();

        message.addHeader( "X-Continuum-Host", localHostName );

        message.addHeader( "X-Continuum-Project-Id", project.getId() );

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
                MailMessage.Address to = new MailMessage.Address( mailbox  );

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

    private String getFromMailbox( ContinuumProject project )
    {
        if ( fromMailbox != null )
        {
            return fromMailbox;
        }

        if ( StringUtils.isEmpty( project.getNagEmailAddress() ) )
        {
            return FALLBACK_FROM_MAILBOX;
        }

        return project.getNagEmailAddress();
    }

    private boolean shouldNotify( ContinuumBuild build, ContinuumBuild previousBuild )
    {
        if ( build == null )
        {
            return true;
        }

        // Always send if the project failed
        if ( build.getState() == ContinuumProjectState.FAILED ||
             build.getState() == ContinuumProjectState.ERROR)
        {
            return true;
        }

        // Send if this is the first build
        if ( previousBuild == null )
        {
            return true;
        }

        // Send if the state has changed
        getLogger().info( "Current build state: " + build.getState() + ", previous build state: " + previousBuild.getState() );

        if ( build.getState() != previousBuild.getState() )
        {
            return true;
        }

        getLogger().info( "Same state, not sending mail." );

        return false;
    }

    private ContinuumBuild getPreviousBuild( ContinuumProject project, ContinuumBuild currentBuild )
        throws ContinuumException
    {
        Iterator it;

        try
        {
            it = store.getBuildsForProject( project.getId(), 0, 0 );
        }
        catch ( ContinuumStoreException ex )
        {
            throw new ContinuumException( "Error while finding the last project build.", ex );
        }

        if ( !it.hasNext() )
        {
            return null;
        }

        ContinuumBuild build = (ContinuumBuild) it.next();

        if ( currentBuild != null && !build.getId().equals( currentBuild.getId() ) )
        {
            throw new ContinuumException( "INTERNAL ERROR: The current build wasn't the first in the build list. " +
                                          "Current build: '" + currentBuild.getId() + "', " +
                                          "first build: '" + build.getId() + "'." );
        }

        if ( !it.hasNext() )
        {
            return null;
        }

        return (ContinuumBuild) it.next();
    }
}
