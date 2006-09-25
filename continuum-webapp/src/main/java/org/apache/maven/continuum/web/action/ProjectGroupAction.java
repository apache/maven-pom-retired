package org.apache.maven.continuum.web.action;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.ContinuumException;

/**
 * ProjectGroupAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="projectGroup"
 */
public class ProjectGroupAction
    extends ContinuumConfirmAction
{
    private int projectGroupId;

    private ProjectGroup projectGroup;

    private boolean confirmed;

    public String summary()
        throws ContinuumException
    {
        projectGroup = getContinuum().getProjectGroup( projectGroupId );

        return SUCCESS;
    }

    public String members()
        throws ContinuumException
    {
        return summary();
    }

    public String buildDefinitions()
        throws ContinuumException
    {
        return summary();
    }

    public String notifiers()
        throws ContinuumException
    {
        return summary();
    }

    public String remove()
        throws ContinuumException
    {
        //todo add confirm page like removing build definition
        if ( confirmed )
        {
            getContinuum().removeProjectGroup( projectGroupId );
        }
        else
        {
            return CONFIRM;
        }

        return SUCCESS;
    }

    public String build()
        throws ContinuumException
    {
        getContinuum().buildProjectGroup( projectGroupId );

        return SUCCESS;
    }


    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }
}
