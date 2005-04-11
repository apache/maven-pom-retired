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
import java.util.Map;

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.notification.NotificationDispatcher;
import org.codehaus.plexus.notification.NotificationException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: DefaultContinuumNotificationDispatcher.java,v 1.2 2005/04/01 22:55:52 trygvis Exp $
 */
public class DefaultContinuumNotificationDispatcher
    extends AbstractLogEnabled
    implements ContinuumNotificationDispatcher
{
    /** @requirement */
    private NotificationDispatcher notificationDispatcher;

    /** @requirement */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // ContinuumNotificationDispatcher Implementation
    // ----------------------------------------------------------------------

    public void buildStarted( ContinuumBuild build )
    {
        sendNotifiaction( MESSAGE_ID_BUILD_STARTED, build );
    }

    public void checkoutStarted( ContinuumBuild build )
    {
        sendNotifiaction( MESSAGE_ID_CHECKOUT_STARTED, build );
    }

    public void checkoutComplete( ContinuumBuild build )
    {
        sendNotifiaction( MESSAGE_ID_CHECKOUT_COMPLETE, build );
    }

    public void runningGoals( ContinuumBuild build )
    {
        sendNotifiaction( MESSAGE_ID_RUNNING_GOALS, build );
    }

    public void goalsCompleted( ContinuumBuild build )
    {
        sendNotifiaction( MESSAGE_ID_GOALS_COMPLETED, build );
    }

    public void buildComplete( ContinuumBuild build )
    {
        sendNotifiaction( MESSAGE_ID_BUILD_COMPLETE, build );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void sendNotifiaction( String messageId, ContinuumBuild build )
    {
        Map context = new HashMap();

        try
        {
            context.put( CONTEXT_PROJECT, store.getProjectByBuild( build.getId() ) );

            context.put( CONTEXT_BUILD, build );

            context.put( CONTEXT_BUILD_RESULT, store.getBuildResultForBuild( build.getId() ) );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while population the notification context.", e );

            return;
        }

        try
        {
            notificationDispatcher.sendNotification( messageId, context );
        }
        catch ( NotificationException e )
        {
            getLogger().error( "Error while notifying.", e );
        }
    }
}
