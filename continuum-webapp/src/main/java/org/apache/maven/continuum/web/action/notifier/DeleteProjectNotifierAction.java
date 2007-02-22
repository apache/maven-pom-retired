package org.apache.maven.continuum.web.action.notifier;

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
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;

/**
 * Action that deletes a {@link ProjectNotifier} from a specified {@link Project}.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: DeleteNotifierAction.java 467122 2006-10-23 20:50:19Z jmcconnell $
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="deleteProjectNotifier"
 */
public class DeleteProjectNotifierAction
    extends ContinuumActionSupport
{
    private int projectId;

    /**
     * Identifier for the {@link ProjectGroup} that the current {@link Project} is a member of.
     */
    private int projectGroupId;

    private int notifierId;

    private String notifierType;

    private String recipient;

    private boolean fromGroupPage = false;

    private String projectGroupName = "";

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkRemoveProjectNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        getContinuum().removeNotifier( projectId, notifierId );

        if ( fromGroupPage )
        {
            return "to_group_page";
        }

        return SUCCESS;
    }

    public String doDefault()
        throws ContinuumException
    {
        try
        {
            checkRemoveProjectNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        ProjectNotifier notifier = getContinuum().getNotifier( projectId, notifierId );

        Map configuration = notifier.getConfiguration();

        notifierType = notifier.getType();

        if ( ( "mail".equals( notifierType ) ) || ( "msn".equals( notifierType ) ) ||
            ( "jabber".equals( notifierType ) ) )
        {
            recipient = (String) configuration.get( "address" );
        }

        if ( "irc".equals( notifierType ) )
        {
            recipient = (String) configuration.get( "host" );

            if ( configuration.get( "port" ) != null )
            {
                recipient = recipient + ":" + (String) configuration.get( "port" );
            }

            recipient = recipient + ":" + (String) configuration.get( "channel" );
        }

        return "delete";
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setNotifierId( int notifierId )
    {
        this.notifierId = notifierId;
    }

    public int getNotifierId()
    {
        return notifierId;
    }

    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
    }

    public String getNotifierType()
    {
        return notifierType;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getRecipient()
    {
        return recipient;
    }

    public void setRecipient( String recipient )
    {
        this.recipient = recipient;
    }

    public boolean isFromGroupPage()
    {
        return fromGroupPage;
    }

    public void setFromGroupPage( boolean fromGroupPage )
    {
        this.fromGroupPage = fromGroupPage;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            if ( projectGroupId != 0 )
            {
                projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
            }
            else
            {
                projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
            }
        }

        return projectGroupName;
    }
}
