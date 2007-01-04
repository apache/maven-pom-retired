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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;

/**
 * Common base class for all Project Group notifier edit actions.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 */
public abstract class AbstractGroupNotifierEditAction
    extends AbstractNotifierEditActionSupport
{

    /**
     * {@link ProjectGroup} identifier for which the notifier is being edited.
     */
    private int projectGroupId;

    /** 
     * Creates or updates the {@link ProjectNotifier} instance for the 
     * {@link ProjectGroup} here.<p>
     * This is used by the subclasses that create/obtain an instance of 
     * {@link ProjectNotifier} to be saved.
     * 
     * @see org.apache.maven.continuum.web.action.notifier.AbstractNotifierEditActionSupport#saveNotifier(ProjectNotifier)
     */
    protected void saveNotifier( ProjectNotifier notifier )
        throws ContinuumException
    {
        boolean isNew = notifier.getId() <= 0;
        if ( !isNew )
        {            
            getContinuum().updateGroupNotifier( projectGroupId, notifier );
        }
        else
        {         
            getContinuum().addGroupNotifier( projectGroupId, notifier );
        }
    }

    /**
     * @return the notifier
     * @throws ContinuumException 
     */
    protected ProjectNotifier getNotifier()
        throws ContinuumException
    {
        return getContinuum().getGroupNotifier( projectGroupId, getNotifierId() );
    }

    /**
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

}
