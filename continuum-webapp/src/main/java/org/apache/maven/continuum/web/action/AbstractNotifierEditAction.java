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

import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectNotifier;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractNotifierEditAction
    extends ContinuumActionSupport
{
    private ProjectNotifier notifier;

    private int projectId;

    private int notifierId;

    private String notifierType;

    private boolean sendOnSuccess;

    private boolean sendOnFailure;

    private boolean sendOnError;

    private boolean sendOnWarning;

    public String save()
        throws ContinuumException
    {
        boolean isNew = false;

        notifier = getNotifier();

        if ( notifier == null || notifierId == 0 )
        {
            notifier = new ProjectNotifier();

            isNew = true;
        }

        notifier.setType( notifierType );

        notifier.setSendOnSuccess( sendOnSuccess );

        notifier.setSendOnFailure( sendOnFailure );

        notifier.setSendOnError( sendOnError );

        notifier.setSendOnWarning( sendOnWarning );

        setNotifierConfiguration( notifier );

        if ( !isNew )
        {
            getContinuum().updateNotifier( projectId, notifier );
        }
        else
        {
            getContinuum().addNotifier( projectId, notifier );
        }

        return SUCCESS;
    }

    public String edit()
        throws ContinuumException
    {
        notifier = getNotifier();

        if ( notifier == null )
        {
            notifier = new ProjectNotifier();
        }

        notifierType = notifier.getType();

        sendOnSuccess = notifier.isSendOnSuccess();

        sendOnFailure = notifier.isSendOnFailure();

        sendOnError = notifier.isSendOnError();

        sendOnWarning = notifier.isSendOnWarning();

        initConfiguration( notifier.getConfiguration() );

        return SUCCESS;
    }

    protected abstract void initConfiguration( Map configuration );

    protected abstract void setNotifierConfiguration( ProjectNotifier notifier );

    private ProjectNotifier getNotifier()
        throws ContinuumException
    {
        return getContinuum().getNotifier( projectId, notifierId );
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

    public String getNotifierType()
    {
        return notifierType;
    }

    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
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
