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
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ContinuumScm;
import org.apache.maven.continuum.scm.ContinuumScmException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.utils.ContinuumUtils;
import org.apache.maven.scm.manager.NoSuchScmProviderException;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckoutProjectContinuumAction
    extends AbstractContinuumAction
{
    private ContinuumScm scm;

    private ContinuumStore store;

    public void execute( Map context )
        throws Exception
    {
        Project project = store.getProject( getProjectId( context ) );

        int state = project.getState();

        project.setState( ContinuumProjectState.CHECKING_OUT );

        store.updateProject( project );

        File workingDirectory = getWorkingDirectory( context );

        // ----------------------------------------------------------------------
        // Check out the project
        // ----------------------------------------------------------------------

        ScmResult result;
        try
        {
            result = scm.checkOut( project, workingDirectory );
        }
        catch ( ContinuumScmException e )
        {
            // TODO: Dissect the scm exception to be able to give better feedback
            Throwable cause = e.getCause();

            if ( cause instanceof NoSuchScmProviderException )
            {
                result = new ScmResult();
                result.setSuccess( false );
                result.setProviderMessage( cause.getMessage() );
            }
            else if ( e.getResult() != null )
            {
                result = e.getResult();
            }
            else
            {
                result = new ScmResult();
                result.setSuccess( false );
                result.setException( ContinuumUtils.throwableMessagesToString( e ) );
            }
        }
        catch ( Throwable t )
        {
            // TODO: do we want this here, or should it be to the logs?
            result = new ScmResult();
            result.setSuccess( false );
            result.setException( ContinuumUtils.throwableMessagesToString( t ) );
        }
        finally
        {
            project.setState( state );

            store.updateProject( project );
        }
        context.put( KEY_CHECKOUT_SCM_RESULT, result );
    }
}
