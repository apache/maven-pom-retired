package org.apache.maven.continuum.notification;

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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.RecipientSource;
import org.codehaus.plexus.notification.notifier.Notifier;
import org.codehaus.plexus.notification.notifier.manager.NotifierManager;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumNotificationDispatcher
    extends AbstractLogEnabled
    implements ContinuumNotificationDispatcher
{
    /** @requirement */
    private NotifierManager notifierManager;

    /** @requirement */
    private ContinuumStore store;

    /** @requirement */
    private RecipientSource recipientSource;

    // ----------------------------------------------------------------------
    // ContinuumNotificationDispatcher Implementation
    // ----------------------------------------------------------------------

    public void buildStarted( ContinuumProject project )
    {
        sendNotification( MESSAGE_ID_BUILD_STARTED, project, null );
    }

    public void checkoutStarted( ContinuumProject project )
    {
        sendNotification( MESSAGE_ID_CHECKOUT_STARTED, project, null );
    }

    public void checkoutComplete( ContinuumProject project, UpdateScmResult scmResult )
    {
        sendNotification( MESSAGE_ID_CHECKOUT_COMPLETE, project, null );
    }

    public void runningGoals( ContinuumProject project, ContinuumBuild build )
    {
        sendNotification( MESSAGE_ID_RUNNING_GOALS, project, build );
    }

    public void goalsCompleted( ContinuumProject project, ContinuumBuild build )
    {
        sendNotification( MESSAGE_ID_GOALS_COMPLETED, project, build );
    }

    public void buildComplete( ContinuumProject project, ContinuumBuild build )
    {
        sendNotification( MESSAGE_ID_BUILD_COMPLETE, project, build );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------


    private void sendNotification( String messageId,
                                   ContinuumProject project,
                                   ContinuumBuild build )
    {
        sendNotification( messageId, project, build, null );
    }

    private void sendNotification( String messageId,
                                   ContinuumProject project,
                                   ContinuumBuild build,
                                   UpdateScmResult scmResult )
    {
        Map context = new HashMap();

        // The objects are reread from the store to make sure they're getting the "final"
        // state of the objects. Ideally this should be done on a pr notifier basis or the
        // objects should be made read only.

        try
        {
            context.put( CONTEXT_PROJECT, store.getProject( project.getId() ) );

            if ( build != null )
            {
                context.put( CONTEXT_BUILD, store.getBuild( build.getId() ) );

                context.put( CONTEXT_BUILD_RESULT, store.getBuildResultForBuild( build.getId() ) );
            }

            if ( scmResult != null )
            {
                context.put( CONTEXT_UPDATE_SCM_RESULT, scmResult );
            }
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while population the notification context.", e );

            return;
        }

        try
        {
            for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
            {
                ContinuumNotifier continuumNotifier = (ContinuumNotifier) i.next();

                String notifierType = continuumNotifier.getType();

                Properties configuration = continuumNotifier.getConfiguration();

                Notifier notifier = notifierManager.getNotifier( notifierType );

                Set recipients = recipientSource.getRecipients( notifierType, messageId, configuration, context );

                notifier.sendNotification( messageId, recipients, continuumNotifier.getConfiguration(), context );
            }
        }
        catch ( NotificationException e )
        {
            getLogger().error( "Error while notifying.", e );
        }
    }
}
