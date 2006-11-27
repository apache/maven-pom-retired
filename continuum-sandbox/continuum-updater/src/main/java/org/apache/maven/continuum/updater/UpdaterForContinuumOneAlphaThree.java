package org.apache.maven.continuum.updater;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.v1_0_alpha_3.AntProject;
import org.apache.maven.continuum.project.v1_0_alpha_3.ContinuumBuild;
import org.apache.maven.continuum.project.v1_0_alpha_3.ContinuumDeveloper;
import org.apache.maven.continuum.project.v1_0_alpha_3.ContinuumNotifier;
import org.apache.maven.continuum.project.v1_0_alpha_3.ContinuumProject;
import org.apache.maven.continuum.project.v1_0_alpha_3.ContinuumJPoxStore;
import org.apache.maven.continuum.project.v1_0_alpha_3.MavenOneProject;
import org.apache.maven.continuum.project.v1_0_alpha_3.MavenTwoProject;
import org.apache.maven.continuum.project.v1_0_alpha_3.ShellProject;
import org.apache.maven.continuum.scm.v1_0_alpha_3.CheckOutScmResult;
import org.apache.maven.continuum.scm.v1_0_alpha_3.UpdateScmResult;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.updater.exception.UpdaterException;
import org.codehaus.plexus.jdo.JdoFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.updater.Updater"
 *   role-hint="updateTo1.0-alpha-4"
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class UpdaterForContinuumOneAlphaThree
    implements Updater
{
    private ContinuumStore oldStore;

    private ContinuumStore store;

    public String getReleaseUrl()
    {
        return "http://www.apache.org/dist/maven/binaries/continuum-1.0-alpha-4-bin.zip";
    }

    public File getOldApplicationXml( File continuumHome )
    {
        return new File( continuumHome, "apps/continuum/conf/application.xml" );
    }

    public File getNewApplicationXml( File continuumHome )
    {
        return new File( continuumHome, "apps/continuum/conf/application.xml" );
    }

    public void updateDatabase( JdoFactory oldFactory, JdoFactory newFactory )
        throws UpdaterException
    {
        //throw new UpdaterException( "Update database not implemented." );
    }

    private Project convertProject( ContinuumProject oldProject )
        throws ContinuumStoreException
    {
        Project project = new Project();

        project.setId( new Integer( oldProject.getId() ).intValue() );

        project.setName( oldProject.getName() );

        project.setScmUrl( oldProject.getScmUrl() );

        project.setVersion( oldProject.getVersion() );

        project.setWorkingDirectory( oldProject.getWorkingDirectory() );

        project.setState( oldProject.getState() );

        project.setExecutorId( oldProject.getExecutorId() );

        project.setLatestBuildId( new Integer( oldProject.getLastBuildId() ).intValue() );

        //oldProject.getPreviousBuildId() => don't exist in new model

        project.setBuildNumber( oldProject.getBuildNumber() );

        for ( Iterator i = oldProject.getBuilds().iterator(); i.hasNext(); )
        {
            ContinuumBuild oldBuildResult = (ContinuumBuild) i.next();

            project.addBuildResult( convertBuildResult( oldBuildResult, project ) );
        }

        project.setCheckoutResult( convertScmResult( oldProject.getCheckOutScmResult() ) );

        //oldProject.getCheckOutErrorMessage() => don't exist in new model
        //oldProject.getCheckOutErrorException() => don't exist in new model
        //mailType => don't exist in new model

        project.addBuildDefinition( convertBuildDefinition( oldProject ) );

        project.setUrl( oldProject.getUrl() );

        project.setGroupId( oldProject.getGroupId() );

        //oldProject.getTestOutputDirectory() => don't exist in new model

        for ( Iterator i = oldProject.getDevelopers().iterator(); i.hasNext(); )
        {
            ContinuumDeveloper oldDeveloper = (ContinuumDeveloper) i.next();

            project.addDeveloper( convertDeveloper( oldDeveloper ) );
        }

        for ( Iterator i = oldProject.getNotifiers().iterator(); i.hasNext(); )
        {
            ContinuumNotifier oldNotifier = (ContinuumNotifier) i.next();

            project.addNotifier( convertNotifier( oldNotifier ) );
        }

        project.setProjectGroup( store.getDefaultProjectGroup() );
        return project;
    }

    private BuildDefinition convertBuildDefinition( ContinuumProject oldProject )
    {
        String executorId = oldProject.getExecutorId();

        BuildDefinition bd = new BuildDefinition();

        bd.setArguments( oldProject.getCommandLineArguments() );

        if ( "maven-1".equals( executorId ) )
        {
            bd.setBuildFile( "project.xml" );
        }
        else if ( "maven2".equals( executorId ) )
        {
            bd.setBuildFile( "pom.xml" );
        }

        if ( oldProject instanceof MavenOneProject )
        {
            MavenOneProject project = (MavenOneProject) oldProject;

            bd.setGoals( project.getGoals() );
        }
        if ( oldProject instanceof MavenTwoProject )
        {
            MavenTwoProject project = (MavenTwoProject) oldProject;

            bd.setGoals( project.getGoals() );
        }
        if ( oldProject instanceof AntProject )
        {
            AntProject project = (AntProject) oldProject;

            bd.setBuildFile( project.getExecutable() );

            bd.setGoals( project.getTargets() );
        }
        if ( oldProject instanceof ShellProject )
        {
            ShellProject project = (ShellProject) oldProject;

            bd.setBuildFile( project.getExecutable() );
        }

        //bd.setSchedule(); ==> TODO

        return bd;
    }

    private ProjectDeveloper convertDeveloper( ContinuumDeveloper oldDeveloper )
    {
        ProjectDeveloper developer = new ProjectDeveloper();

        developer.setScmId( oldDeveloper.getId() );

        developer.setName( oldDeveloper.getName() );

        developer.setEmail( oldDeveloper.getEmail() );

        return developer;
    }

    private ProjectNotifier convertNotifier( ContinuumNotifier oldNotifier )
    {
        ProjectNotifier notifier = new ProjectNotifier();

        notifier.setType( oldNotifier.getType());

        Map oldConf = oldNotifier.getConfiguration();

        Map conf = new HashMap();

        for ( Iterator i = oldConf.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();

            conf.put( key, oldConf.get( key ) );
        }

        notifier.setConfiguration( conf );

        return notifier;
    }

    private BuildResult convertBuildResult( ContinuumBuild oldBuildResult, Project newProject )
    {
        BuildResult buildResult = new BuildResult();

        buildResult.setProject( newProject );

        buildResult.setId( new Integer( oldBuildResult.getId() ).intValue() );

        buildResult.setState( oldBuildResult.getState() );

        if ( oldBuildResult.isForced() )
        {
            buildResult.setTrigger( 1 );
        }

        buildResult.setStartTime( oldBuildResult.getStartTime() );

        buildResult.setEndTime( oldBuildResult.getEndTime() );

        buildResult.setError( oldBuildResult.getError() );

        buildResult.setSuccess( oldBuildResult.isSuccess() );

        //standardOutput => don't exist in new model
        //standardError => don't exist in new model

        buildResult.setExitCode( oldBuildResult.getExitCode() );

        buildResult.setScmResult( convertScmResult( oldBuildResult.getUpdateScmResult() ) );

        return buildResult;
    }

    private ScmResult convertScmResult( CheckOutScmResult oldScmResult )
    {
        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( oldScmResult.isSuccess() );

        scmResult.setProviderMessage( oldScmResult.getProviderMessage() );

        scmResult.setCommandOutput( oldScmResult.getCommandOutput() );

        return scmResult;
    }

    private ScmResult convertScmResult( UpdateScmResult oldScmResult )
    {
        ScmResult scmResult = new ScmResult();

        scmResult.setSuccess( oldScmResult.isSuccess() );

        scmResult.setProviderMessage( oldScmResult.getProviderMessage() );

        scmResult.setCommandOutput( oldScmResult.getCommandOutput() );

        return scmResult;
    }
}
