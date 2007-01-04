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

import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;

/**
 * Common base class that consolidates the common properties used by extending 
 * <code>XXXEditAction</code> implementations and defines a contract expected of 
 * the extending clases.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 */
public abstract class AbstractNotifierEditActionSupport
    extends ContinuumActionSupport
{

    /**
     * Identifier for the {@link ProjectNotifier} instance being edited.
     */
    private int notifierId;

    /**
     * Type of {@link ProjectNotifier} tagged as a String value.
     */
    private String notifierType;

    /**
     * Detemines if the notifier should fire when build was successful.<p>
     * <code>true</code> implies notifier executes on a successful build.
     */
    private boolean sendOnSuccess;

    /**
     * Detemines if the notifier should fire when build failed.<p>
     * <code>true</code> implies notifier executes for a failed build.
     */
    private boolean sendOnFailure;

    /**
     * Detemines if the notifier should fire when build resulted in any error(s).<p>
     * <code>true</code> implies notifier executes when any error(s) is/are detected 
     * for the build.
     */
    private boolean sendOnError;

    /**
     * Detemines if the notifier should fire when build resulted in any warning(s).<p>
     * <code>true</code> implies notifier executes when any warning(s) is/are detected 
     * for the build.
     */
    private boolean sendOnWarning;

    /**
     * Obtain and return the {@link ProjectNotifier} instance for editing.
     * @return {@link ProjectNotifier} instance.
     * 
     * @throws ContinuumException if there was error retrieving 
     *              the target {@link ProjectNotifier} instance.
     */
    protected abstract ProjectNotifier getNotifier()
        throws ContinuumException;

    /**
     * Persists update to the {@link ProjectNotifier} instance being edited.
     * @param notifier {@link ProjectNotifier} to save.
     * 
     * @throws ContinuumException if there was an error saving the 
     *                              {@link ProjectNotifier} instance.
     */
    protected abstract void saveNotifier( ProjectNotifier notifier )
        throws ContinuumException;

    /**
     * Creates or updates {@link ProjectNotifier} instance.
     * 
     * @return result as String.
     * @throws ContinuumException
     */
    public String save()
        throws ContinuumException
    {
        ProjectNotifier notifier = getNotifier();

        boolean isNew = ( notifier == null || getNotifierId() == 0 );

        if ( isNew )
        {
            notifier = new ProjectNotifier();
        }

        notifier.setType( getNotifierType() );

        notifier.setSendOnSuccess( isSendOnSuccess() );

        notifier.setSendOnFailure( isSendOnFailure() );

        notifier.setSendOnError( isSendOnError() );

        notifier.setSendOnWarning( isSendOnWarning() );

        setNotifierConfiguration( notifier );

        saveNotifier( notifier );

        return SUCCESS;
    }

    /**
     * Obtains the {@link ProjectNotifier} instance for edit purposes.
     * 
     * @return result as String.
     * @throws ContinuumException
     */
    public String edit()
        throws ContinuumException
    {
        ProjectNotifier notifier = getNotifier();

        if ( notifier == null )
        {
            notifier = new ProjectNotifier();
        }

        // setup Action fields 
        setNotifierType( notifier.getType() );

        setSendOnSuccess( notifier.isSendOnSuccess() );

        setSendOnFailure( notifier.isSendOnFailure() );

        setSendOnError( notifier.isSendOnError() );

        setSendOnWarning( notifier.isSendOnWarning() );

        initConfiguration( notifier.getConfiguration() );

        return SUCCESS;
    }

    public int getNotifierId()
    {
        return notifierId;
    }

    /**
     * @return the notifierType
     */
    public String getNotifierType()
    {        
        return notifierType;
    }

    /**
     * @param notifierType the notifierType to set
     */
    public void setNotifierType( String notifierType )
    {        
        this.notifierType = notifierType;
    }

    /**
     * @return the sendOnSuccess
     */
    public boolean isSendOnSuccess()
    {
        return sendOnSuccess;
    }

    /**
     * @param sendOnSuccess the sendOnSuccess to set
     */
    public void setSendOnSuccess( boolean sendOnSuccess )
    {
        this.sendOnSuccess = sendOnSuccess;
    }

    /**
     * @return the sendOnFailure
     */
    public boolean isSendOnFailure()
    {
        return sendOnFailure;
    }

    /**
     * @param sendOnFailure the sendOnFailure to set
     */
    public void setSendOnFailure( boolean sendOnFailure )
    {
        this.sendOnFailure = sendOnFailure;
    }

    /**
     * @return the sendOnError
     */
    public boolean isSendOnError()
    {
        return sendOnError;
    }

    /**
     * @param sendOnError the sendOnError to set
     */
    public void setSendOnError( boolean sendOnError )
    {
        this.sendOnError = sendOnError;
    }

    /**
     * @return the sendOnWarning
     */
    public boolean isSendOnWarning()
    {
        return sendOnWarning;
    }

    /**
     * @param sendOnWarning the sendOnWarning to set
     */
    public void setSendOnWarning( boolean sendOnWarning )
    {
        this.sendOnWarning = sendOnWarning;
    }

    /**
     * @param notifierId the notifierId to set
     */
    public void setNotifierId( int notifierId )
    {
        this.notifierId = notifierId;
    }

    /**
     * Initialises the configuration map that the {@link ProjectNotifier} 
     * instance is to be inited with.
     * 
     * @param configuration map of configuration key-value pairs.
     */
    protected abstract void initConfiguration( Map configuration );

    /**
     * Sets the configuration for the specified {@link ProjectNotifier} 
     * instance.
     * @param notifier
     * @see #initConfiguration(Map)
     */
    protected abstract void setNotifierConfiguration( ProjectNotifier notifier );

}