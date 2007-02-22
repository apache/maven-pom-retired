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

/**
 * Action to add a {@link ProjectNotifier} for a specified {@link Project}.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: AddNotifierAction.java 466640 2006-10-22 13:11:30Z jmcconnell $
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="addProjectNotifier"
 * @since 1.1
 */
public class AddProjectNotifierAction
    extends ContinuumActionSupport
{
    /**
     * Identifier for the {@link Project} instance.
     */
    private int projectId;

    /**
     * Identifier for the {@link ProjectGroup} instance that the current {@link Project} is a member of.
     */
    private int projectGroupId;

    /**
     * Type for a {@link ProjectNotifier}.
     */
    private String notifierType;

    /**
     * Detemines if the save operation returns to the project group notifier page or not.<p>
     * <code>true</code> implies return to the project group notifier page.
     */
    private boolean fromGroupPage = false;

    private String projectGroupName = "";

    /**
     * Default method executed when no specific method is specified
     * for invocation.
     *
     * @return result as a String value to determines the control flow.
     */
    public String execute()
        throws ContinuumException
    {
        try
        {
            checkAddProjectNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return notifierType + "_" + INPUT;
    }

    /**
     * TODO: document!
     */
    public String doDefault()
        throws ContinuumException
    {
        try
        {
            checkAddProjectNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return INPUT;
    }

    /**
     * Returns the type for the {@link ProjectNotifier}.
     *
     * @return Notifier type as String.
     */
    public String getNotifierType()
    {
        return notifierType;
    }

    /**
     * Sets the type for the {@link ProjectNotifier}.
     *
     * @param notifierType Notifier type to set.
     */
    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
    }

    /**
     * Identifier for the Project being edited.
     *
     * @return project id.
     */
    public int getProjectId()
    {
        return projectId;
    }

    /**
     * Sets the identifier for the Project to be edited for
     * project notifiers.
     *
     * @param projectId
     */
    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    /**
     * Returns the identifier for the {@link ProjectGroup} that the
     * {@link Project} is a member of.
     *
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * Sets the identifier for the {@link ProjectGroup} that the
     * {@link Project} is a member of.
     *
     * @param projectGroupId the identifier to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    /**
     * @return the fromGroupPage
     */
    public boolean isFromGroupPage()
    {
        return fromGroupPage;
    }

    /**
     * @param fromGroupPage the fromGroupPage to set
     */
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
