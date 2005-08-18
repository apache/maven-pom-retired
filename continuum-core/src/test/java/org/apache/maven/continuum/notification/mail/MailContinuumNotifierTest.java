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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.mailsender.MailMessage;
import org.codehaus.plexus.mailsender.test.MockMailSender;
import org.codehaus.plexus.notification.notifier.Notifier;
import org.codehaus.plexus.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MailContinuumNotifierTest
    extends AbstractContinuumTest
{
    public void testSuccessfulBuild()
        throws Exception
    {
        Project project = makeStubProject( "Test Project" );

        BuildResult build = makeBuild( ContinuumProjectState.OK );

        MailMessage mailMessage = sendNotificationAndGetMessage( project, build, "lots out build output" );

        dumpContent( mailMessage );
    }

    public void testFailedBuild()
        throws Exception
    {
        Project project = makeStubProject( "Test Project" );

        BuildResult build = makeBuild( ContinuumProjectState.FAILED );

        MailMessage mailMessage = sendNotificationAndGetMessage( project, build, "output" );

        dumpContent( mailMessage );
    }

    public void testErrorenousBuild()
        throws Exception
    {
        Project project = makeStubProject( "Test Project" );

        BuildResult build = makeBuild( ContinuumProjectState.ERROR );

        build.setError( "Big long error message" );

        MailMessage mailMessage = sendNotificationAndGetMessage( project, build, "lots of stack traces" );

        dumpContent( mailMessage );
    }


    private void dumpContent( MailMessage mailMessage )
    {
        if ( false )
        {
            System.err.println( mailMessage.getContent() );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private MailMessage sendNotificationAndGetMessage( Project project, BuildResult build, String buildOutput )
        throws Exception
    {
        Set recipients = new HashSet();

        recipients.add( "foo@bar" );

        Map context = new HashMap();

        context.put( ContinuumNotificationDispatcher.CONTEXT_PROJECT, project );

        context.put( ContinuumNotificationDispatcher.CONTEXT_BUILD, build );

        context.put( ContinuumNotificationDispatcher.CONTEXT_BUILD_OUTPUT, buildOutput );

        context.put( "buildHost", "foo.bar.com" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Notifier notifier = (Notifier) lookup( Notifier.ROLE, "mail" );

        notifier.sendNotification( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE, recipients, context );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MockMailSender mailSender = (MockMailSender) lookup( MockMailSender.ROLE );

        assertEquals( 1, mailSender.getReceivedEmailSize() );

        List mails = CollectionUtils.iteratorToList( mailSender.getReceivedEmail() );

        MailMessage mailMessage = (MailMessage) mails.get( 0 );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertEquals( "continuum@localhost", mailMessage.getFrom().getMailbox() );

        assertEquals( "Continuum", mailMessage.getFrom().getName() );

        List to = mailMessage.getToAddresses();

        assertEquals( 1, to.size() );

        assertEquals( "foo@bar", ( (MailMessage.Address) to.get( 0 ) ).getMailbox() );

        assertNull( ( (MailMessage.Address) to.get( 0 ) ).getName() );

        return mailMessage;
    }

    private BuildResult makeBuild( int state )
    {
        BuildResult build = new BuildResult();

        build.setId( 17 );

        build.setStartTime( System.currentTimeMillis() );

        build.setEndTime( System.currentTimeMillis() + 1234567 );

        build.setState( state );

        build.setTrigger( ContinuumProjectState.TRIGGER_FORCED );

        build.setExitCode( 10 );

        return build;
    }
}
