package org.apache.maven.continuum.store;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.ScmFile;

import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ModelloJPoxContinuumStore.java,v 1.2 2005/04/03 21:31:33 trygvis Exp $
 */
public class ModelloJPoxContinuumStore
    extends AbstractContinuumStore
    implements ContinuumStore, Initializable
{
    /** @requirement */
    private JdoFactory jdoFactory;

    private ContinuumJPoxStore store;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );
    }

    // ----------------------------------------------------------------------
    // ContinuumStore Implementation
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Database methods
    // ----------------------------------------------------------------------

    public void createDatabase()
        throws ContinuumStoreException
    {
    }

    public void deleteDatabase()
        throws ContinuumStoreException
    {
    }

    // ----------------------------------------------------------------------
    // ContinuumProject
    // ----------------------------------------------------------------------

    public String addProject( String name, String scmUrl, String nagEmailAddress, String version, String builderId,
                              String workingDirectory, Properties configuration )
        throws ContinuumStoreException
    {
        ContinuumProject project = new ContinuumProject();

        project.setName( name );
        project.setScmUrl( scmUrl );
        project.setNagEmailAddress( nagEmailAddress );
        project.setVersion( version );
        project.setBuilderId( builderId );
        project.setWorkingDirectory( workingDirectory );
        project.setState( ContinuumProjectState.NEW );
        project.setConfiguration( configuration );

        try
        {
            Object id = store.addContinuumProject( project );

            project = store.getContinuumProjectByJdoId( id, true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while adding a project.", e );
        }

        return project.getId();
    }

    public void removeProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

//            System.err.println( "**********************************" );
//            System.err.println( "**********************************" );
//            System.err.println( "**********************************" );

//            System.err.println( "getProject()" );
            ContinuumProject project = store.getContinuumProject( projectId, false );

            // TODO: This is dumb.
            PersistenceManager pm = store.getThreadState().getPersistenceManager();

//            System.err.println( "getBuilds()" );
            for ( Iterator it = project.getBuilds().iterator(); it.hasNext(); )
            {
                ContinuumBuild build = (ContinuumBuild) it.next();

//                System.err.println( "getBuildResult()" );
                ContinuumBuildResult result = build.getBuildResult();

//                System.err.println( "result.getChangedFiles()" );
                List changedFiles = result.getChangedFiles();

//                System.err.println( "changedFiles.clear()" );
                changedFiles.clear();

//                System.err.println( "pm.deletePersistentAll( changedFiles )" );
                pm.deletePersistentAll( changedFiles );

//                System.err.println( "result.setBuild( null )" );
                result.setBuild( null );

//                System.err.println( "pm.deletePersistent( result )" );
                pm.deletePersistent( result );
            }

//            System.err.println( "project.getBuilds()" );
            List builds = new ArrayList( project.getBuilds() );

            for ( Iterator it = builds.iterator(); it.hasNext(); )
            {
                ContinuumBuild build = (ContinuumBuild) it.next();

//                System.err.println( "build.setProject( null )" );
                build.setProject( null );
            }

//            System.err.println( "pm.deletePersistentAll( builds )" );
            pm.deletePersistentAll( project.getBuilds() );

//            System.err.println( "store.deleteContinuumProject( projectId )" );
            store.deleteContinuumProject( projectId );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while removing project with id '" + projectId + "'.", e );
        }
    }

    public void setWorkingDirectory( String projectId, String workingDirectory )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setWorkingDirectory( workingDirectory );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting the working directory.", e );
        }
    }

    public void updateProject( String projectId, String name, String scmUrl, String nagEmailAddress, String version )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setName( name );
            project.setScmUrl( scmUrl );
            project.setNagEmailAddress( nagEmailAddress );
            project.setVersion( version );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while updating project.", e );
        }
    }

    public void updateProjectConfiguration( String projectId, Properties configuration )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setConfiguration( configuration );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while updating project configuration.", e );
        }
    }

    public Iterator getAllProjects()
        throws ContinuumStoreException
    {
        try
        {
            Collection projects = store.getContinuumProjectCollection( true, null, "name ascending" );

            return projects.iterator();
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading project set", e );
        }
    }

    public Iterator findProjectsByName( String nameSearchPattern )
        throws ContinuumStoreException
    {
        Iterator it = getAllProjects();

        List hits = new ArrayList();

        while ( it.hasNext() )
        {
            ContinuumProject continuumProject = (ContinuumProject) it.next();

            if ( continuumProject.getName().toLowerCase().indexOf( nameSearchPattern.toLowerCase() ) != -1 )
            {
                hits.add( continuumProject );
            }
        }

        return hits.iterator();
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            ContinuumProject project = store.getContinuumProject( projectId, true );

            return project;
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading project.", e );
        }
    }

    public ContinuumProject getProjectByBuild( String buildId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            Object id = JDOHelper.getObjectId( build.getProject() );

            store.commit();

            return store.getContinuumProjectByJdoId( id, true );
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while loading project.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    public String createBuild( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setState( ContinuumProjectState.BUILD_SIGNALED );

            ContinuumBuild build = new ContinuumBuild();

            build.setStartTime( System.currentTimeMillis() );

            build.setState( ContinuumProjectState.BUILD_SIGNALED );

            build.setProject( project );

            Object id = store.addContinuumBuild( build );

            store.commit();

            build = store.getContinuumBuildByJdoId( id, true );

            return build.getId();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while creating continuum build for project: '" + projectId + "'.", e );
        }
    }

    public void setBuildResult( String buildId, int state, ContinuumBuildResult result, Throwable error )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            ContinuumProject project = build.getProject();

            project.setState( state );

            build.setState( state );

            build.setEndTime( new Date().getTime() );

            build.setError( throwableToString( error ) );

            store.commit();

            // ----------------------------------------------------------------------
            // This double commit seems to be needed for some reason. Not having it
            // seems to result in some foreign key constraint violation.
            // ----------------------------------------------------------------------

            store.begin();

            build = store.getContinuumBuild( buildId, false );

            build.setBuildResult( result );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting build result for build: '" + buildId + "'.", e );
        }
    }

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException
    {
        try
        {
            return store.getContinuumBuild( buildId, true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading build id: '" + buildId + "'.", e );
        }
    }

    public ContinuumBuild getLatestBuildForProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            List builds = store.getContinuumProject( projectId, true ).getBuilds();

            if ( builds.size() == 0 )
            {
                return null;
            }

            return (ContinuumBuild) builds.get( builds.size() - 1 );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading last build for project id: '" + projectId + "'.", e );
        }
    }

    public Iterator getBuildsForProject( String projectId, int start, int end )
        throws ContinuumStoreException
    {
        try
        {
            Collection builds = store.getContinuumBuildCollection( true, "this.project.id == \"" + projectId + "\"", "startTime descending" );

            return builds.iterator();
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while getting builds for project id: '" + projectId + "'.", e );
        }
    }

    public ContinuumBuildResult getBuildResultForBuild( String buildId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            if ( build.getBuildResult() == null )
            {
                store.commit();

                return null;
            }

            Object id = JDOHelper.getObjectId( build.getBuildResult() );

            store.commit();

            ContinuumBuildResult result = store.getContinuumBuildResultByJdoId( id, true );

            return result;
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while getting build result.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ContinuumJPoxStore getStore()
    {
        return store;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void rollback( ContinuumJPoxStore store )
    {
        try
        {
            getLogger().warn( "Rolling back transaction." );

            store.rollback();
        }
        catch ( Exception e )
        {
            getLogger().error( "Error while rolling back tx.", e );
        }
    }
}
