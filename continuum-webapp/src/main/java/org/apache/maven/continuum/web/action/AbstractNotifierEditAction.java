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

import com.opensymphony.xwork.ActionSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractNotifierEditAction
    extends ActionSupport
{
    private Continuum continuum;

    private ProjectNotifier notifier;

    private int projectId;

    private int notifierId;

    private boolean sendOnSuccess;

    private boolean sendOnFailure;
    
    private boolean sendOnError;

    private boolean sendOnWarning;

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

            notifier.setSendOnSuccess( sendOnSuccess );

            notifier.setSendOnFailure( sendOnFailure );

            notifier.setSendOnError( sendOnError );

            notifier.setSendOnWarning( sendOnWarning );

            setNotifierConfiguration( notifier );

            if ( !isNew )
            {
                continuum.updateNotifier( projectId, notifier );
            }
            else
            {
                continuum.addNotifier( projectId, notifier );
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
            notifier = getNotifier();

            if ( notifier == null )
            {
                notifier = new ProjectNotifier();
            }
        }
        catch ( ContinuumException e )
        {
            addActionMessage( "Can't get notifier informations (id=" + notifierId + ") for project " + projectId + " : " + e.getMessage() );

            e.printStackTrace();

            return ERROR;
        }

        sendOnSuccess = notifier.isSendOnSuccess();

        sendOnFailure = notifier.isSendOnFailure();

        sendOnError =notifier.isSendOnError();

        sendOnWarning = notifier.isSendOnWarning();

        initConfiguration( notifier.getConfiguration() );

        return INPUT;
    }

    protected abstract void initConfiguration( Map configuration );

    protected abstract void setNotifierConfiguration( ProjectNotifier notifier );

    private ProjectNotifier getNotifier()
        throws ContinuumException
    {
        return continuum.getNotifier( projectId, notifierId );
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

    public boolean isSendOnSuccess()
    {
        return sendOnSuccess;
    }

    public void setSendOnSuccess( boolean sendOnSuccess )
    {
        this.sendOnSuccess = sendOnSuccess;
    }

    public boolean isSendOnFailure()
    {
        return sendOnFailure;
    }

    public void setSendOnFailure( boolean sendOnFailure )
    {
        this.sendOnFailure = sendOnFailure;
    }

    public boolean isSendOnError()
    {
        return sendOnError;
    }

    public void setSendOnError( boolean sendOnError )
    {
        this.sendOnError = sendOnError;
    }

    public boolean isSendOnWarning()
    {
        return sendOnWarning;
    }

    public void setSendOnWarning( boolean sendOnWarning )
    {
        this.sendOnWarning = sendOnWarning;
    }
}
