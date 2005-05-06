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

import java.util.Map;
import java.util.Set;

import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.notification.notifier.Notifier;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ConsoleNotifier.java,v 1.1.1.1 2005/03/29 20:42:01 trygvis Exp $
 */
public class ConsoleNotifier
    extends AbstractLogEnabled
    implements Notifier
{
    // ----------------------------------------------------------------------
    // Notifier Implementation
    // ----------------------------------------------------------------------

    public void sendNotification( String source, Set recipients, Map context )
        throws NotificationException
    {
        ContinuumProject project = (ContinuumProject) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ContinuumBuild build = (ContinuumBuild) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );

        ContinuumBuildResult result = (ContinuumBuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD_RESULT );

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
            goalsCompleted( project, build, result );
        }
        else if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
        {
            buildComplete( project, build, result );
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

    private void runningGoals( ContinuumProject project, ContinuumBuild build )
    {
        out( project, build, "Running goals." );
    }

    private void goalsCompleted( ContinuumProject project, ContinuumBuild build, ContinuumBuildResult result )
    {
        if ( result != null )
        {
            out( project, build, "Goals completed. state: " + build.getState() );
        }
        else
        {
            out( project, build, "Goals completed." );
        }
    }

    private void buildComplete( ContinuumProject project, ContinuumBuild build, ContinuumBuildResult result )
    {
        if ( result != null )
        {
            out( project, build, "Build complete. state: " + build.getState() );
        }
        else
        {
            out( project, build, "Build complete." );
        }
    }

    private void out( ContinuumProject project, ContinuumBuild build, String msg )
    {
        System.out.println( "Build event for project '" + project.getName() + "':" + msg );

        if ( build != null && !StringUtils.isEmpty( build.getError() ) )
        {
            System.out.println( build.getError() );
        }
    }
}
