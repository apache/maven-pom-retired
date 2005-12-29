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
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Project;

import com.opensymphony.xwork.ActionSupport;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class NotifierEditAction
    extends ActionSupport
{
    private Continuum continuum;

    private ProjectNotifier notifier;

    private Project project;

    private int projectId;

    private int notifierId;

    public String execute()
    {
        try
        {
            boolean isNew = false;

            notifier = getNotifier();

            if ( notifier == null || notifierId == 0 )
            {
                notifier = new ProjectNotifier();

                isNew = true;
            }

            if ( !isNew )
            {
                //continuum.updateNotifier( projectId, notifier );
            }
            else
            {
                //continuum.addNotifier( projectId, notifier );
            }
        }
        catch ( ContinuumException e )
        {
            addActionMessage( "Can't update notifier (id=" + notifierId + ") for project " + projectId + " : " + e.getMessage() );

            e.printStackTrace();

            return ERROR;
        }

        return SUCCESS;
    }

    public String doDefault()
    {
        try
        {
            project = continuum.getProject( projectId );

            notifier = getNotifier();
        }
        catch ( ContinuumException e )
        {
            addActionMessage( "Can't get notifier informations (id=" + notifierId + ") for project " + projectId + " : " + e.getMessage() );

            e.printStackTrace();

            return ERROR;
        }

        return INPUT;
    }

    private ProjectNotifier getNotifier()
        throws ContinuumException
    {
        return continuum.getNotifier( projectId, notifierId );
    }

    public Project getProject()
    {
        return project;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getNotifierId()
    {
        return notifierId;
    }

    public void setNotifierId( int notifierId )
    {
        this.notifierId = notifierId;
    }
}
