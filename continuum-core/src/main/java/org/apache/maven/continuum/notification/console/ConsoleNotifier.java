package org.apache.maven.continuum.notification.console;

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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProject;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.notifier.AbstractNotifier;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ConsoleNotifier
    extends AbstractNotifier
{
    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source, Set recipients, Map configuration, Map context )
        throws NotificationException
    {
        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        BuildResult build = (BuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

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
    {
        out( project, null, "Build started." );
    }

    private void checkoutStarted( ContinuumProject project )
    {
        out( project, null, "Checkout started." );
    }

    private void checkoutComplete( ContinuumProject project )
    {
        out( project, null, "Checkout complete." );
    }

    private void runningGoals( ContinuumProject project, BuildResult build )
    {
        out( project, build, "Running goals." );
    }

    private void goalsCompleted( ContinuumProject project, BuildResult build )
    {
        if ( build.getError() == null )
        {
            out( project, build, "Goals completed. state: " + build.getState() );
        }
        else
        {
            out( project, build, "Goals completed." );
        }
    }

    private void buildComplete( ContinuumProject project, BuildResult build )
    {
        if ( build.getError() == null )
        {
            out( project, build, "Build complete. state: " + build.getState() );
        }
        else
        {
            out( project, build, "Build complete." );
        }
    }

    private void out( ContinuumProject project, BuildResult build, String msg )
    {
        System.out.println( "Build event for project '" + project.getName() + "':" + msg );

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
}
