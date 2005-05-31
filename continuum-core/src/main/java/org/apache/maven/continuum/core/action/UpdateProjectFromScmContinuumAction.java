package org.apache.maven.continuum.core.action;

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

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UpdateProjectFromScmContinuumAction
    extends AbstractContinuumAction
{
    protected void doExecute()
        throws Exception
    {
        ContinuumProject project = getProject();

        String projectId = project.getId();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        getStore().setIsUpdating( projectId );

        getNotifier().checkoutStarted( project );

        UpdateScmResult updateScmResult = getScm().updateProject( project );

        putContext( KEY_UPDATE_SCM_RESULT, updateScmResult );

        getStore().setUpdateDone( projectId );
    }

    protected void handleException( Throwable throwable )
        throws ContinuumStoreException
    {
        getLogger().fatalError( "Error while updating from SCM. Project id '" + getProjectId() + "'." );

        getStore().setBuildError( getBuildId(),
                                  getUpdateScmResult( null ),
                                  throwable );
    }

    protected void doFinally()
        throws ContinuumStoreException
    {
        getNotifier().checkoutComplete( getProject(), getUpdateScmResult() );
    }
}
