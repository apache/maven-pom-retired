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

import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.scm.manager.NoSuchScmProviderException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckOutProjectContinuumAction
    extends AbstractContinuumAction
{
    protected void doExecute( Map context )
        throws Exception
    {
        String projectId = getProjectId();

        ContinuumProject project = getStore().getProject( projectId );

        File workingDirectory = getWorkingDirectory();

        CheckOutScmResult result;

        result = getScm().checkOut( project, workingDirectory );

        getStore().setCheckoutDone( projectId, result, null, null );

        putContext( KEY_CHECKOUT_SCM_RESULT, result );
    }

    protected void handleException( Throwable throwable )
        throws ContinuumStoreException
    {
        String errorMessage = null;

        // TODO: Dissect the scm exception to be able to give better feedback
        Throwable cause = throwable.getCause();

        if ( cause instanceof NoSuchScmProviderException )
        {
            errorMessage = cause.getMessage();

            throwable = null;
        }

        getStore().setCheckoutDone( getProjectId(),
                                    getCheckOutResult(),
                                    errorMessage,
                                    throwable );
    }
}
