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

import java.util.Collection;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExecuteBuilderContinuumAction
    extends AbstractContinuumAction
{
    protected void doExecute()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Get parameters from the context
        // ----------------------------------------------------------------------

        ContinuumProject project = getProject();

        boolean forced = isForced();

        UpdateScmResult updateScmResult = getUpdateScmResult();

        ContinuumBuildExecutor buildExecutor = getCore().getBuildExecutor( project.getExecutorId() );

        // ----------------------------------------------------------------------
        // This is really a precondition for this action to execute
        // ----------------------------------------------------------------------

        if ( updateScmResult.getUpdatedFiles().size() == 0 &&
             !forced &&
             !isNew( project ) )
        {
            getLogger().info( "No files updated, not building. Project id '" + project.getId() + "'." );

            getStore().setBuildNotExecuted( getProjectId() );

            return;
        }

        // ----------------------------------------------------------------------
        // Make the build result
        // ----------------------------------------------------------------------

        String buildId = getStore().buildingProject( getProjectId(), forced, updateScmResult );

        putContext( KEY_BUILD_ID, buildId );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        getNotifier().runningGoals( project, getBuild() );

        ContinuumBuildResult result = buildExecutor.build( project );

        int state = result.isSuccess() ?
                    ContinuumProjectState.OK : ContinuumProjectState.FAILED;

        getStore().setBuildResult( buildId,
                                   state,
                                   result,
                                   updateScmResult,
                                   null );
    }

    protected void handleException( Throwable throwable )
        throws ContinuumStoreException
    {
        getStore().setBuildError( getBuildId(),
                                  getUpdateScmResult( null ),
                                  throwable );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private boolean isNew( ContinuumProject project )
        throws ContinuumException
    {
        Collection builds = getCore().getBuildsForProject( project.getId() );

        return builds.size() == 0;
    }
}
