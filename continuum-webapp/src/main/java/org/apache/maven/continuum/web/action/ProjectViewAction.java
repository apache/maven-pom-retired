package org.apache.maven.continuum.web.action;

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
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="projectView"
 */
public class ProjectViewAction
    extends ContinuumActionSupport
    implements SecureAction
{

    private Project project;

    private int projectId;

    /**
     * Target {@link ProjectGroup} to view.
     */
    private ProjectGroup projectGroup;

    public String execute()
        throws ContinuumException
    {
        projectGroup = getProjectGroup();

        project = getContinuum().getProjectWithAllDetails( projectId );
        return SUCCESS;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public int getProjectId()
    {
        return projectId;
    }

    /**
     * Returns the {@link ProjectGroup} instance obtained for 
     * the specified project group Id, or null if it were not set.
     * 
     * @return the projectGroup
     */
    public ProjectGroup getProjectGroup()
            throws ContinuumException
    {
        return getContinuum().getProjectGroupByProjectId( projectId );
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        
        try
        {
            bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION,
                getProjectGroup().getName() );
        }
        catch ( ContinuumException ce )
        {

        }

        return bundle;
    }


}
