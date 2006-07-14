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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="deleteNotification"
 */
public class DeleteNotifierAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    private int projectId;

    private int notifierId;

    private String notifierType;

    public String execute()
    {
        try
        {
            continuum.removeNotifier( projectId, notifierId );
        }
        catch ( ContinuumException e )
        {
            addActionMessage( "Can't delete notifier (id=" + notifierId + ") for project " + projectId + " : " + e.getMessage() );

            e.printStackTrace();

            return ERROR;
        }

        return SUCCESS;
    }

    public String doDefault()
    {
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
}
