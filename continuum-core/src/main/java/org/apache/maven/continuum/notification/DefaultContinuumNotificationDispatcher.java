package org.apache.maven.continuum.notification;

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

import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.RecipientSource;
import org.codehaus.plexus.notification.notifier.Notifier;
import org.codehaus.plexus.notification.notifier.manager.NotifierManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.apache.maven.continuum.notification.ContinuumNotificationDispatcher"
 *   role-hint="default"
 */
public class DefaultContinuumNotificationDispatcher
    extends AbstractLogEnabled
    implements ContinuumNotificationDispatcher
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    /**
     * @plexus.requirement
     */
    private NotifierManager notifierManager;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private RecipientSource recipientSource;

    // ----------------------------------------------------------------------
    // ContinuumNotificationDispatcher Implementation
    // ----------------------------------------------------------------------

    public void buildStarted( Project project )
    {
        sendNotification( MESSAGE_ID_BUILD_STARTED, project, null );
    }

    public void checkoutStarted( Project project )
    {
        sendNotification( MESSAGE_ID_CHECKOUT_STARTED, project, null );
    }

    public void checkoutComplete( Project project )
    {
        sendNotification( MESSAGE_ID_CHECKOUT_COMPLETE, project, null );
    }

    public void runningGoals( Project project, BuildResult build )
    {
        sendNotification( MESSAGE_ID_RUNNING_GOALS, project, build );
    }

    public void goalsCompleted( Project project, BuildResult build )
    {
        sendNotification( MESSAGE_ID_GOALS_COMPLETED, project, build );
    }

    public void buildComplete( Project project, BuildResult build )
    {
        sendNotification( MESSAGE_ID_BUILD_COMPLETE, project, build );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void sendNotification( String messageId, Project project, BuildResult build )
    {
        Map context = new HashMap();

        // ----------------------------------------------------------------------
        // The objects are reread from the store to make sure they're getting the "final"
        // state of the objects. Ideally this should be done on a per notifier basis or the
        // objects should be made read only.
        // ----------------------------------------------------------------------

        try
        {
            // TODO: remove re-reading?
            // Here we need to get all the project details
            //  - builds are used to detect if the state has changed (TODO: maybe previousState field is better)
            //  - notifiers are used to send the notification
            project = store.getProjectWithAllDetails( project.getId() );

            context.put( CONTEXT_PROJECT, project );

            if ( build != null )
            {
                context.put( CONTEXT_BUILD, build );

                if ( build.getEndTime() != 0 )
                {
                    context.put( CONTEXT_BUILD_OUTPUT,
                                 configurationService.getBuildOutput( build.getId(), project.getId() ) );
                }

                context.put( CONTEXT_UPDATE_SCM_RESULT, build.getScmResult() );
            }


            ProjectGroup projectGroup = store.getProjectGroupWithBuildDetails( project.getProjectGroup().getId() );

            // perform the project lvl notifications
            for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
            {
                sendNotification( messageId, (ProjectNotifier) i.next(), context );
            }

            // perform the project group lvl notifications
            if ( projectGroup.getNotifiers() != null )
            {
                for ( Iterator i = projectGroup.getNotifiers().iterator(); i.hasNext(); )
                {
                    sendNotification( messageId, (ProjectNotifier) i.next(), context );
                }
            }            
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().error( "Error while population the notification context.", e );

            return;
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "Error while population the notification context.", e );

            return;
        }
    }

    private void sendNotification( String messageId, ProjectNotifier projectNotifier, Map context )
    {
        String notifierType = projectNotifier.getType();

        if ( !projectNotifier.isEnabled() )
        {
            getLogger().info( notifierType + " notifier (id=" + projectNotifier.getId() + ") is disabled." );

            return;
        }

        Map configuration = projectNotifier.getConfiguration();

        try
        {
            context.put( CONTEXT_PROJECT_NOTIFIER, projectNotifier );

            Notifier notifier = notifierManager.getNotifier( notifierType );

            Set recipients = recipientSource.getRecipients( String.valueOf( projectNotifier.getId() ), messageId,
                                                            configuration, context );

            notifier.sendNotification( messageId, recipients, configuration, context );
        }
        catch ( NotificationException e )
        {
            getLogger().error( "Error while trying to use the " + notifierType + "notifier.", e );
        }
    }
}
