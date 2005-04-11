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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.continuum.project.ContinuumProject;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.RecipientSource;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ContinuumRecipientSource.java,v 1.2 2005/04/01 22:55:52 trygvis Exp $
 */
public class ContinuumRecipientSource
    extends AbstractLogEnabled
    implements RecipientSource, Initializable
{
    /** @configuration */
    private String toOverride;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // To address
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( toOverride ) )
        {
            getLogger().info( "To override address is not configured, will use the nag email address from the project." );
        }
        else
        {
            getLogger().warn( "Using '" + toOverride + "' as the to address for all emails." );
        }
    }

    // ----------------------------------------------------------------------
    // RecipientSource Implementation
    // ----------------------------------------------------------------------

    public Set getRecipients( String notifierType, String messageId, Map context )
        throws NotificationException
    {
        if ( notifierType.equals( "console" ) )
        {
            return Collections.EMPTY_SET;
        }
        else if ( notifierType.equals( "mail" ) )
        {
            return getMailRecipients( context );
        }

        getLogger().warn( "Unknown notifier type '" + notifierType + "'." );

        return Collections.EMPTY_SET;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Set getMailRecipients( Map context )
        throws NotificationException
    {
        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        if ( project == null )
        {
            throw new NotificationException( "Missing project from the notification context." );
        }

        Set recipients = new HashSet();

        if ( !StringUtils.isEmpty( toOverride ) )
        {
            recipients.add( toOverride );
        }
        else if ( !StringUtils.isEmpty( project.getNagEmailAddress() ) )
        {
            recipients.add( project.getNagEmailAddress() );
        }

        return recipients;
    }
}
