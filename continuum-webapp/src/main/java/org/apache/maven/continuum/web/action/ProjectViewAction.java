package org.apache.maven.continuum.web.action;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;

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
{

    private Project project;

    private int projectId;

    /**
     * Target {@link ProjectGroup} to view.
     */
    private ProjectGroup projectGroup;

    /**
     * Identifier for the target {@link ProjectGroup} to obtain for 
     * viewing.
     */
    private int projectGroupId;

    public String execute()
        throws ContinuumException
    {
        projectGroup = getContinuum().getProjectGroup( projectGroupId );

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
     * Return the identifier for the {@link ProjectGroup} to view.
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * Sets the {@link ProjectGroup} identifier to obtain for viewing.
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    /**
     * Returns the {@link ProjectGroup} instance obtained for 
     * the specified project group Id, or null if it were not set.
     * 
     * @return the projectGroup
     */
    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

}
