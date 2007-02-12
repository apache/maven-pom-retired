/**
 *
 */
package org.apache.maven.continuum.web.action.component;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.NotifierSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Component Action that prepares and provides Project Group Notifier and
 * Project Notifier summaries.
 *
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="notifierSummary"
 */
public class NotifierSummaryAction
    extends ContinuumActionSupport
{
    /**
     * Identifier for the {@link ProjectGroup} for which the Notifier summary
     * needs to be prepared for.
     */
    private int projectGroupId;

    /**
     * Identifier for the {@link Project} for which the Notifier summary needs
     * to be prepared for.
     */
    private int projectId;

    /**
     * {@link ProjectGroup} instance to obtain the Notifier summary for.
     */
    private ProjectGroup projectGroup;

    private List projectGroupNotifierSummaries = new ArrayList();

    private List projectNotifierSummaries = new ArrayList();

    /**
     * Prepare Notifier summary for a {@link Project}.
     *
     * @return
     */
    public String summarizeForProject()
    {
        getLogger().debug( "Obtaining summary for Project Id: " + projectId );

        try
        {
            projectNotifierSummaries = summarizeForProject( projectId );
        }
        catch ( ContinuumException e )
        {
            getLogger().error( "Unable to prepare Notifier summaries for Project Id: " + projectId, e );
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Prepare Notifier summary for a {@link Project}.
     *
     * @param projectId The project id.
     * @return
     */
    private List summarizeForProject( int projectId )
        throws ContinuumException
    {
        return gatherProjectNotifierSummaries( projectId );
    }

    /**
     * Prepare Notifier summary for a {@link ProjectGroup}.
     *
     * @return
     */
    public String summarizeForProjectGroup()
    {
        getLogger().debug( "Obtaining summary for ProjectGroup Id:" + projectGroupId );

        try
        {
            projectGroupNotifierSummaries = gatherGroupNotifierSummaries();

            Collection projects = getContinuum().getProjectsInGroup( projectGroupId );
            if ( projects != null )
            {
                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    Project p = (Project) i.next();
                    projectNotifierSummaries.addAll( summarizeForProject( p.getId() ) );
                }
            }
        }
        catch ( ContinuumException e )
        {
            getLogger().error( "Unable to prepare Notifier summaries for ProjectGroup Id: " + projectGroupId, e );
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Prepares and returns a list of Notifier summaries for the specified Project Id.
     *
     * @param projectId The project id.
     * @return List of {@link NotifierSummary} instance for the specified project.
     * @throws ContinuumException if there was an error obtaining
     *                            and preparing Notifier Summary list for the project
     */
    private List gatherProjectNotifierSummaries( int projectId )
        throws ContinuumException
    {
        List summaryList = new ArrayList();
        Project project = getContinuum().getProjectWithAllDetails( projectId );

        for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
        {
            NotifierSummary ns = generateProjectNotifierSummary( (ProjectNotifier) i.next(), project );
            summaryList.add( ns );
        }

        return summaryList;
    }

    /**
     * Prepares and returns {@link ProjectGroup} summaries for the specified project group Id.
     *
     * @return
     * @throws ContinuumException if there was an error fetching the {@link ProjectGroup} for specified Id.
     */
    private List gatherGroupNotifierSummaries()
        throws ContinuumException
    {
        List summaryList = new ArrayList();
        projectGroup = getContinuum().getProjectGroupWithBuildDetails( projectGroupId );

        for ( Iterator i = projectGroup.getNotifiers().iterator(); i.hasNext(); )
        {
            NotifierSummary ns = generateGroupNotifierSummary( (ProjectNotifier) i.next() );
            summaryList.add( ns );
        }

        return summaryList;
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     *
     * @param notifier
     * @return
     */
    private NotifierSummary generateProjectNotifierSummary( ProjectNotifier notifier, Project project )
    {
        return generateNotifierSummary( notifier, projectGroupId, project );
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     *
     * @param notifier
     * @return
     */
    private NotifierSummary generateGroupNotifierSummary( ProjectNotifier notifier )
    {
        return generateNotifierSummary( notifier, projectGroupId, null );
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     *
     * @param notifier
     * @return
     */
    private NotifierSummary generateNotifierSummary( ProjectNotifier notifier, int projectGroupId, Project project )
    {
        NotifierSummary ns = new NotifierSummary();
        ns.setId( notifier.getId() );
        ns.setType( notifier.getType() );
        ns.setProjectGroupId( projectGroupId );
        if ( project != null )
        {
            ns.setProjectId( project.getId() );
            ns.setProjectName( project.getName() );
        }

        if ( notifier.isFromProject() )
        {
            ns.setFromProject( true );
        }
        else
        {
            ns.setFromProject( false );
        }

        // Source the recipient 
        Map configuration = notifier.getConfiguration();

        String recipient = "unknown";

        if ( ( "mail".equals( notifier.getType() ) ) || ( "msn".equals( notifier.getType() ) ) ||
            ( "jabber".equals( notifier.getType() ) ) )
        {
            recipient = (String) configuration.get( "address" );
        }

        if ( "irc".equals( notifier.getType() ) )
        {
            recipient = (String) configuration.get( "host" );

            if ( configuration.get( "port" ) != null )
            {
                recipient = recipient + ":" + (String) configuration.get( "port" );
            }

            recipient = recipient + ":" + (String) configuration.get( "channel" );
        }

        if ( "wagon".equals( notifier.getType() ) )
        {
            recipient = (String) configuration.get( "url" );
        }

        ns.setRecipient( recipient );

        // XXX: Hack - just for testing :)
        StringBuffer sb = new StringBuffer();
        if ( notifier.isSendOnError() )
        {
            sb.append( "Error" );
        }
        if ( notifier.isSendOnFailure() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "Failure" );
        }
        if ( notifier.isSendOnSuccess() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "Success" );
        }
        if ( notifier.isSendOnWarning() )
        {
            if ( sb.length() > 0 )
            {
                sb.append( '/' );
            }
            sb.append( "Warning" );
        }
        ns.setEvents( sb.toString() );

        ns.setEnabled( notifier.isEnabled() );
        return ns;
    }

    // property accessors 

    /**
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    /**
     * @return the projectId
     */
    public int getProjectId()
    {
        return projectId;
    }

    /**
     * @param projectId the projectId to set
     */
    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    /**
     * @return the projectGroup
     */
    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    /**
     * @param projectGroup the projectGroup to set
     */
    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    /**
     * @return the projectGroupNotifierSummaries
     */
    public List getProjectGroupNotifierSummaries()
    {
        return projectGroupNotifierSummaries;
    }

    /**
     * @param projectGroupNotifierSummaries the projectGroupNotifierSummaries to set
     */
    public void setProjectGroupNotifierSummaries( List projectGroupNotifierSummaries )
    {
        this.projectGroupNotifierSummaries = projectGroupNotifierSummaries;
    }

    /**
     * @return the projectNotifierSummaries
     */
    public List getProjectNotifierSummaries()
    {
        return projectNotifierSummaries;
    }

    /**
     * @param projectNotifierSummaries the projectNotifierSummaries to set
     */
    public void setProjectNotifierSummaries( List projectNotifierSummaries )
    {
        this.projectNotifierSummaries = projectNotifierSummaries;
    }
}
