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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.codehaus.plexus.notification.AbstractRecipientSource;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumRecipientSource
    extends AbstractRecipientSource
    implements Initializable
{
    public static String ADDRESS_FIELD = "address";

    /**
     * @plexus.configuration
     */
    private String toOverride;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        // ----------------------------------------------------------------------
        // To address
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( toOverride ) )
        {
            getLogger().info(
                "To override address is not configured, will use the nag email address from the project." );
        }
        else
        {
            getLogger().warn( "Using '" + toOverride + "' as the to address for all emails." );
        }
    }

    // ----------------------------------------------------------------------
    // RecipientSource Implementation
    // ----------------------------------------------------------------------

    public Set getRecipients( String notifierId, String messageId, Map configuration, Map context )
        throws NotificationException
    {
        Project project = (Project) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ProjectNotifier projectNotifier =
            (ProjectNotifier) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT_NOTIFIER );

        if ( project == null )
        {
            throw new NotificationException( "Missing project from the notification context." );
        }

        Set recipients = new HashSet();

        if ( !StringUtils.isEmpty( toOverride ) )
        {
            recipients.add( toOverride );
        }
        else if ( projectNotifier != null )
        {
            addNotifierAdresses( projectNotifier, recipients );
        }
        else if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
        {
            for ( Iterator notifierIterator = project.getNotifiers().iterator(); notifierIterator.hasNext(); )
            {
                ProjectNotifier notifier = (ProjectNotifier) notifierIterator.next();

                if ( notifier.getId() == new Integer( notifierId ).intValue() &&
                    notifier.getConfiguration().containsKey( ADDRESS_FIELD ) )
                {
                    addNotifierAdresses( notifier, recipients );
                }
            }
        }

        if ( recipients.isEmpty() )
        {
            return Collections.EMPTY_SET;
        }
        else
        {
            return recipients;
        }
    }

    private void addNotifierAdresses( ProjectNotifier notifier, Set recipients )
    {
        if ( notifier.getConfiguration() != null )
        {
            String addressField = (String) notifier.getConfiguration().get( ADDRESS_FIELD );

            if ( StringUtils.isNotEmpty( addressField ) )
            {
                String[] addresses = StringUtils.split( addressField, "," );

                for ( int i = 0; i < addresses.length; i++ )
                {
                    recipients.add( addresses[i].trim() );
                }
            }
        }
    }
}
