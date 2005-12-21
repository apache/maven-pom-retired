package org.apache.maven.continuum.web.action;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionSupport;

import java.util.Map;

/**
 * @author Nick Gonzalez
 * @version $Id$
 */
public class BuildProjectAction
    extends ActionSupport
{
    private Continuum continuum;

    private int projectId;

    public String execute()
    {
        try
        {
            if ( projectId > 0 )
            {
                continuum.buildProject( projectId );
            }
            else
            {
                continuum.buildProjects();
            }
        }
        catch ( ContinuumException e )
        {
            addActionMessage( "Can't build project (id=" + projectId + ") : " + e.getMessage() );

            e.printStackTrace();

            return ERROR;
        }

        return SUCCESS;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectId()
    {
        return projectId;
    }
}