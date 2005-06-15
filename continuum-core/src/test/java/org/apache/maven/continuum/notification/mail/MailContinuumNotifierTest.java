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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.MavenTwoProject;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.mailsender.MailMessage;
import org.codehaus.plexus.mailsender.test.MockMailSender;
import org.codehaus.plexus.notification.notifier.Notifier;
import org.codehaus.plexus.util.CollectionUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MailContinuumNotifierTest
    extends PlexusTestCase
{
    public void testMailNotification()
        throws Exception
    {
        Notifier notifier = (Notifier) lookup( Notifier.ROLE, "mail" );

        MockMailSender mailSender = (MockMailSender) lookup( MockMailSender.ROLE );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String source = ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE;

        Set recipients = new HashSet();

        recipients.add( "foo@bar" );

        Map context =  new HashMap();

        // ----------------------------------------------------------------------
        // ContinuumProject
        // ----------------------------------------------------------------------

        ContinuumProject project = new MavenTwoProject();

        project.setName( "Test Project" );

        project.setExecutorId( MavenTwoBuildExecutor.ID );

        context.put( ContinuumNotificationDispatcher.CONTEXT_PROJECT, project );

        // ----------------------------------------------------------------------
        // ContinuumBuild
        // ----------------------------------------------------------------------

        ContinuumBuild build = new ContinuumBuild();

        build.setId( "17" );

        build.setStartTime( System.currentTimeMillis() );

        build.setEndTime( System.currentTimeMillis() + 1234567 );

        build.setError( null );

        build.setState( ContinuumProjectState.OK );

        context.put( ContinuumNotificationDispatcher.CONTEXT_BUILD, build );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        notifier.sendNotification( source, recipients, context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( 1, mailSender.getReceivedEmailSize() );

        List mails = CollectionUtils.iteratorToList( mailSender.getReceivedEmail() );

        MailMessage mailMessage = (MailMessage) mails.get( 0 );

        assertEquals( "continuum@localhost", mailMessage.getFrom().getMailbox() );

        assertEquals( "Continuum", mailMessage.getFrom().getName() );

        List to = mailMessage.getToAddresses();

        assertEquals( 1, to.size() );

        assertEquals( "foo@bar", ( (MailMessage.Address) to.get( 0 ) ).getMailbox() );

        assertNull( ( (MailMessage.Address) to.get( 0 ) ).getName() );
    }
}
