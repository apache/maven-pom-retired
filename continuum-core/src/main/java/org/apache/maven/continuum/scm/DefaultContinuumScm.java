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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ChangeFile;
import org.apache.maven.continuum.model.scm.ChangeSet;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumScm
    extends AbstractLogEnabled
    implements ContinuumScm
{
    /**
     * @plexus.requirement
     */
    private ScmManager scmManager;

    /**
     * @plexus.requirement
     */
    private WorkingDirectoryService workingDirectoryService;

    // ----------------------------------------------------------------------
    // ContinuumScm implementation
    // ----------------------------------------------------------------------

    public ScmResult checkOut( Project project, File workingDirectory )
        throws ContinuumScmException
    {
        try
        {
            getLogger().info( "Checking out project: '" + project.getName() + "', " + "id: '" + project.getId() + "' " +
                "to '" + workingDirectory + "'." );

            ScmRepository repository = scmManager.makeScmRepository( project.getScmUrl() );

            ScmResult result;

            synchronized ( this )
            {
                if ( !workingDirectory.exists() )
                {
                    if ( !workingDirectory.mkdirs() )
                    {
                        throw new ContinuumScmException(
                            "Could not make directory: " + workingDirectory.getAbsolutePath() );
                    }
                }
                else
                {
                    try
                    {
                        FileUtils.cleanDirectory( workingDirectory );
                    }
                    catch ( IOException e )
                    {
                        throw new ContinuumScmException(
                            "Could not clean directory : " + workingDirectory.getAbsolutePath(), e );
                    }
                }

                String tag = null;

                ScmFileSet fileSet = new ScmFileSet( workingDirectory );

                result = convertScmResult(
                    scmManager.getProviderByRepository( repository ).checkOut( repository, fileSet, tag ) );
            }

            if ( !result.isSuccess() )
            {
                getLogger().warn( "Error while checking out the code for project: '" + project.getName() + "', id: '" +
                    project.getId() + "' to '" + workingDirectory.getAbsolutePath() + "'." );

                getLogger().warn( "Command output: " + result.getCommandOutput() );

                getLogger().warn( "Provider message: " + result.getProviderMessage() );

                throw new ContinuumScmException( "Error while checking out the project.", result );
            }

            ChangeSet changeSet = (ChangeSet) result.getChanges().get( 0 );
            getLogger().info( "Checked out " + changeSet.getFiles().size() + " files." );

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
    public ScmResult checkOutProject( Project project )
        throws ContinuumScmException
    {
        File workingDirectory = workingDirectoryService.getWorkingDirectory( project );

        if ( workingDirectory == null )
        {
            throw new ContinuumScmException( "The working directory for the project has to be set. Project: '" +
                project.getName() + "', id: '" + project.getId() + "'." );
        }

        return checkOut( project, workingDirectory );
    }

    public ScmResult updateProject( Project project )
        throws ContinuumScmException
    {
        try
        {
            getLogger().info( "Updating project: id: '" + project.getId() + "', name '" + project.getName() + "'." );

            File workingDirectory = workingDirectoryService.getWorkingDirectory( project );

            if ( !workingDirectory.exists() )
            {
                throw new ContinuumScmException( "The working directory for the project doesn't exist " + "(" +
                    workingDirectory.getAbsolutePath() + ")." );
            }

            ScmRepository repository = scmManager.makeScmRepository( project.getScmUrl() );

            String tag = null;

            ScmResult result;

            ScmFileSet fileSet = new ScmFileSet( workingDirectory );

            synchronized ( this )
            {
                result = convertScmResult(
                    scmManager.getProviderByRepository( repository ).update( repository, fileSet, tag ) );
            }

            if ( !result.isSuccess() )
            {
                getLogger().warn( "Error while updating the code for project: '" + project.getName() + "', id: '" +
                    project.getId() + "' to '" + workingDirectory.getAbsolutePath() + "'." );

                getLogger().warn( "Command output: " + result.getCommandOutput() );

                getLogger().warn( "Provider message: " + result.getProviderMessage() );

                throw new ContinuumScmException( "Error while updating the project.", result );
            }

            // TODO: total the number of files in the changesets
//            getLogger().info( "Updated " + result.getFiles().size() + " files." );

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

    private ScmResult convertScmResult( CheckOutScmResult scmResult )
    {
        ScmResult result = new ScmResult();

        result.setSuccess( scmResult.isSuccess() );

        result.setCommandLine( scmResult.getCommandLine() );

        result.setCommandOutput( scmResult.getCommandOutput() );

        result.setProviderMessage( scmResult.getProviderMessage() );

        // TODO: is this valid? Does it ever return a changeset itself?
        ChangeSet changeSet = convertScmFileSetToChangeSet( scmResult.getCheckedOutFiles() );
        result.addChange( changeSet );

        return result;
    }

    private static ChangeSet convertScmFileSetToChangeSet( List files )
    {
        ChangeSet changeSet = null;

        if ( files != null && !files.isEmpty() )
        {
            changeSet = new ChangeSet();

            // TODO: author, etc.
            for ( Iterator it = files.iterator(); it.hasNext(); )
            {
                ScmFile scmFile = (ScmFile) it.next();

                ChangeFile file = new ChangeFile();

                file.setName( scmFile.getPath() );

                // TODO: revision?

                changeSet.addFile( file );
            }
        }
        return changeSet;
    }

    private ScmResult convertScmResult( UpdateScmResult scmResult )
    {
        ScmResult result = new ScmResult();

        result.setSuccess( scmResult.isSuccess() );

        result.setCommandOutput( scmResult.getCommandOutput() );

        result.setProviderMessage( scmResult.getProviderMessage() );

        // TODO: is this valid?
        ChangeSet changeSet = convertScmFileSetToChangeSet( scmResult.getUpdatedFiles() );
        if ( changeSet != null )
        {
            result.addChange( changeSet );
        }

        if ( scmResult.getChanges() != null )
        {
            for ( Iterator it = scmResult.getChanges().iterator(); it.hasNext(); )
            {
                org.apache.maven.scm.ChangeSet scmChangeSet = (org.apache.maven.scm.ChangeSet) it.next();

                ChangeSet change = new ChangeSet();

                change.setAuthor( scmChangeSet.getAuthor() );

                change.setComment( scmChangeSet.getComment() );

                change.setDate( scmChangeSet.getDate().getTime() );

                if ( scmChangeSet.getFile() != null )
                {
                    ChangeFile file = new ChangeFile();

                    file.setName( scmChangeSet.getFile().getName() );

                    file.setRevision( scmChangeSet.getFile().getRevision() );
                }

                result.addChange( change );
            }
        }

        return result;
    }
}
