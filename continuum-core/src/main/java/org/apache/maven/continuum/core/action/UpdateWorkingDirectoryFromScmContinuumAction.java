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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.store.ContinuumStore;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UpdateWorkingDirectoryFromScmContinuumAction
    extends AbstractContinuumAction
{
    private ContinuumNotificationDispatcher notifier;

    private ContinuumScm scm;

    private ContinuumStore store;

    public void execute( Map context )
        throws Exception
    {
        Project project = store.getProject( getProjectId( context ) );

        int state = project.getState();

        project.setState( ContinuumProjectState.UPDATING );

        store.updateProject( project );

        ScmResult scmResult;

        try
        {
            notifier.checkoutStarted( project );

            scmResult = scm.updateProject( project );

            context.put( KEY_UPDATE_SCM_RESULT, scmResult );
        }
        finally
        {
            project.setState( state );

            store.updateProject( project );

            notifier.checkoutComplete( project );
        }
    }
}
