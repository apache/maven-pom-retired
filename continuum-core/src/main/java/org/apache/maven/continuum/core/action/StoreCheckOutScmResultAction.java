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
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.action.Action"
 *   role-hint="store-checkout-scm-result"
 */
public class StoreCheckOutScmResultAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    public void execute( Map context )
        throws TaskExecutionException
    {
        try
        {
            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            ScmResult scmResult = AbstractContinuumAction.getCheckoutResult( context, null );

            Project project = store.getProject( getProjectId( context ) );

            project.setCheckoutResult( scmResult );

            store.updateProject( project );
        }
        catch ( ContinuumStoreException e )
        {
            throw new TaskExecutionException( "Error while storing the checkout result.", e );
        }
    }
}
