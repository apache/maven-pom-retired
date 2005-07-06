package org.apache.maven.continuum.scm;

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
import java.util.Iterator;

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumScm
    extends AbstractLogEnabled
    implements ContinuumScm
{
    /** @plexus.requirement */
    private ScmManager scmManager;

    // ----------------------------------------------------------------------
    // ContinuumScm implementation
    // ----------------------------------------------------------------------

    public CheckOutScmResult checkOut( ContinuumProject project, File workingDirectory )
        throws ContinuumScmException
    {
        try
        {
            getLogger().info( "Checking out project: '" + project.getName() + "', " +
                              "id: '" + project.getId() + "' " +
                              "to '" + workingDirectory + "'." );

            ScmRepository repository = scmManager.makeScmRepository( project.getScmUrl() );

            CheckOutScmResult result;

            synchronized ( this )
            {
                if ( !workingDirectory.exists() )
                {
                    if ( !workingDirectory.mkdirs() )
                    {
                        throw new ContinuumScmException( "Could not make directory: " + workingDirectory.getAbsolutePath() );
                    }
                }

                String tag = null;

                ScmFileSet fileSet = new ScmFileSet( workingDirectory );

                result = convertCheckOutScmResult( scmManager.checkOut( repository, fileSet, tag ) );
            }

            if ( !result.isSuccess() )
            {
                getLogger().warn( "Error while checking out the code for project: '" + project.getName() + "', id: '" + project.getId() + "' to '" + workingDirectory.getAbsolutePath() + "'." );

                getLogger().warn( "Command output: " + result.getCommandOutput() );

                getLogger().warn( "Provider message: " + result.getProviderMessage());

                throw new ContinuumScmException( "Error while checking out the project.", result );
            }

            getLogger().info( "Checked out " + result.getCheckedOutFiles().size() + " files." );

            return result;
        }
        catch ( ScmRepositoryException e )
        {
            throw new ContinuumScmException( "Cannot checkout sources.", e );
        }
        catch ( ScmException e )
        {
            throw new ContinuumScmException( "Cannot checkout sources.", e );
        }
    }

    /**
     * Checks out the sources to the specified directory.
     *
     * @param project The project to check out.
     * @throws ContinuumScmException Thrown in case of a exception while checking out the sources.
     */
    public CheckOutScmResult checkOutProject( ContinuumProject project )
        throws ContinuumScmException
    {
        String workingDirectory = project.getWorkingDirectory();

        if ( workingDirectory == null )
        {
            throw new ContinuumScmException( "The working directory for the project has to be set. Project: '" + project.getName() + "', id: '" + project.getId() + "'.");
        }

        return checkOut( project, new File( workingDirectory ) );
    }

    public UpdateScmResult updateProject( ContinuumProject project )
        throws ContinuumScmException
    {
        try
        {
            getLogger().info( "Updating project: id: '" + project.getId() + "', name '" + project.getName() + "'." );

            File workingDirectory = new File( project.getWorkingDirectory() );

            if ( !workingDirectory.exists() )
            {
                throw new ContinuumScmException( "The working directory for the project doesn't exist (" + project.getWorkingDirectory() + ")." );
            }

            ScmRepository repository = scmManager.makeScmRepository( project.getScmUrl() );

            String tag = null;

            UpdateScmResult result;

            ScmFileSet fileSet = new ScmFileSet( workingDirectory );

            synchronized ( this )
            {
                result = convertUpdateScmResult( scmManager.update( repository, fileSet, tag ) );
            }

            if ( !result.isSuccess() )
            {
                getLogger().warn( "Error while updating the code for project: '" + project.getName() + "', id: '" + project.getId() + "' to '" + workingDirectory.getAbsolutePath() + "'." );

                getLogger().warn( "Command output: " + result.getCommandOutput() );

                getLogger().warn( "Provider message: " + result.getProviderMessage() );

                throw new ContinuumScmException( "Error while checking out the project.", result );
            }

            getLogger().info( "Updated " + result.getUpdatedFiles().size() + " files." );

            return result;
        }
        catch ( ScmRepositoryException ex )
        {
            throw new ContinuumScmException( "Error while update sources.", ex );
        }
        catch ( ScmException ex )
        {
            throw new ContinuumScmException( "Error while update sources.", ex );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private CheckOutScmResult convertCheckOutScmResult( org.apache.maven.scm.command.checkout.CheckOutScmResult scmResult )
    {
        CheckOutScmResult result = new CheckOutScmResult();

        result.setSuccess( scmResult.isSuccess() );

        result.setCommandOutput( scmResult.getCommandOutput() );

        result.setProviderMessage( scmResult.getProviderMessage() );

        if ( scmResult.getCheckedOutFiles() != null )
        {
            for ( Iterator it = scmResult.getCheckedOutFiles().iterator(); it.hasNext(); )
            {
                org.apache.maven.scm.ScmFile scmFile = (org.apache.maven.scm.ScmFile) it.next();

                ScmFile file = new ScmFile();

                file.setPath( scmFile.getPath() );

                result.addCheckedOutFile( file );
            }
        }

        return result;
    }

    private UpdateScmResult convertUpdateScmResult( org.apache.maven.scm.command.update.UpdateScmResult scmResult )
    {
        UpdateScmResult result = new UpdateScmResult();

        result.setSuccess( scmResult.isSuccess() );

        result.setCommandOutput( scmResult.getCommandOutput() );

        result.setProviderMessage( scmResult.getProviderMessage() );

        if ( scmResult.getUpdatedFiles() != null )
        {
            for ( Iterator it = scmResult.getUpdatedFiles().iterator(); it.hasNext(); )
            {
                org.apache.maven.scm.ScmFile scmFile = (org.apache.maven.scm.ScmFile) it.next();

                ScmFile file = new ScmFile();

                file.setPath( scmFile.getPath() );

                result.addUpdatedFile( file );
            }
        }

        return result;
    }
}
