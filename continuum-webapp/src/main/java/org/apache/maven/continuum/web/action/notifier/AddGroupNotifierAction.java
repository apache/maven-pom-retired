/**
 *
 */
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
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

/**
 * WW action that sets up a new {@link ProjectNotifier} instance for
 * the specified {@link ProjectGroup}.
 *
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="addGroupNotifier"
 * @since 1.1
 */
public class AddGroupNotifierAction
    extends ContinuumActionSupport
{
    /**
     * Target {@link ProjectGroup} instance to add the Notifier for.
     */
    private int projectGroupId;

    /**
     * String based type identifier for the {@link ProjectNotifier}.
     */
    private String notifierType;

    private String projectGroupName = "";

    /**
     * Default action method executed in case no method is specified
     * for invocation.
     *
     * @return a String result that determines the control flow.
     */
    public String execute()
        throws ContinuumException
    {
        try
        {
            checkAddProjectGroupNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return notifierType + "_" + INPUT;
    }

    public String doDefault()
        throws ContinuumException
    {
        try
        {
            checkAddProjectGroupNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return INPUT;
    }

    /**
     * Returns the type identifier for the {@link ProjectNotifier} being
     * edited as String.
     *
     * @return notifier type as String.
     */
    public String getNotifierType()
    {
        return notifierType;
    }

    /**
     * Sets the notifier type for the {@link ProjectNotifier} instance
     * being edited.
     *
     * @param notifierType notifier type to set.
     */
    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
    }

    /**
     * Returns the current {@link ProjectGroup} Identifier.
     *
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * Sets the Id for the target {@link ProjectGroup}.
     *
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
        }

        return projectGroupName;
    }
}
