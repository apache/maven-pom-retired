package org.apache.maven.continuum.web.action.notifier;

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
import org.apache.maven.continuum.model.project.ProjectNotifier;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: AbstractNotifierEditAction.java 467122 2006-10-23 20:50:19Z jmcconnell $
 */
public abstract class AbstractProjectNotifierEditAction
    extends AbstractNotifierEditActionSupport
{

    /**
     * Identifier for the {@link Project} who's {@link ProjectNotifier} is being edited.
     */
    private int projectId;

    /**
     * Save the notifier for the {@link Project} here.<p>
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
            getContinuum().updateNotifier( projectId, notifier );
        }
        else
        {
            getContinuum().addNotifier( projectId, notifier );
        }
    }

    /**
     * @see org.apache.maven.continuum.web.action.notifier.AbstractNotifierEditActionSupport#getNotifier()
     */
    protected ProjectNotifier getNotifier()
        throws ContinuumException
    {
        return getContinuum().getNotifier( projectId, getNotifierId() );
    }

    /**
     * Returns the identifier for the current project.
     * 
     * @return current project's id.
     */
    public int getProjectId()
    {
        return projectId;
    }

    /**
     * Sets the id of the current project for this action.
     * 
     * @param projectId current project's id.
     */
    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

}
