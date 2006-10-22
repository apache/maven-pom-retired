package org.apache.maven.continuum.web.action.notifier;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="addNotifier"
 */
public class AddNotifierAction
    extends ContinuumActionSupport
{
    private int projectId;

    /**
     * Target {@link ProjectGroup} instance to add the Notifier for.
     */
    private int projectGroupId;

    private String notifierType;

    public String execute()
    {
        return notifierType + "_" + INPUT;
    }

    public String doDefault()
    {
        return INPUT;
    }

    public String getNotifierType()
    {
        return notifierType;
    }

    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    /**
     * Returns the current {@link ProjectGroup} Identifier.
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * Sets the Id for the target {@link ProjectGroup}.
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

}
