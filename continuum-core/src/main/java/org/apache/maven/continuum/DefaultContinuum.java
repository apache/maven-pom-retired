package org.apache.maven.continuum;

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

import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.project.builder.maven.MavenOneContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.workflow.WorkflowEngine;

import java.util.Collection;
import java.util.Properties;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class DefaultContinuum
    extends AbstractLogEnabled
    implements Continuum
{
    private ContinuumCore core;

    private WorkflowEngine workflowEngine;

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    public Collection getProjects()
        throws ContinuumException
    {
        return core.getProjects();
    }

    // TODO: i realize these are horribly inefficient and I will correct using JDO
    // properly after the alpha-2 release.

    public Collection getProjectsWithFailures()
        throws ContinuumException
    {
        List list = new ArrayList();

        for ( Iterator i = core.getProjects().iterator(); i.hasNext(); )
        {
            ContinuumProject p = (ContinuumProject) i.next();

            if ( p.getState() == ContinuumProjectState.FAILED )
            {
                list.add( p );
            }
        }

        return list;
    }

    public Collection getProjectsWithErrors()
        throws ContinuumException
    {
        List list = new ArrayList();

        for ( Iterator i = core.getProjects().iterator(); i.hasNext(); )
        {
            ContinuumProject p = (ContinuumProject) i.next();

            if ( p.getState() == ContinuumProjectState.ERROR )
            {
                list.add( p );
            }
        }

        return list;
    }

    public ContinuumBuild getLatestBuildForProject( String id )
        throws ContinuumException
    {
        return core.getLatestBuildForProject( id );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void updateProjectFromScm( String projectId )
        throws ContinuumException
    {
        core.updateProjectFromScm( projectId );
    }

    public void updateProjectConfiguration( String projectId, Properties configuration )
        throws ContinuumException
    {
        core.updateProjectConfiguration( projectId, configuration );
    }

    public void removeProject( String projectId )
        throws ContinuumException
    {
        core.removeProject( projectId );
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumException
    {
        return core.getProject( projectId );
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return core.getAllProjects( start, end );
    }

    public CheckOutScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumException
    {
        return core.getCheckOutScmResultForProject( projectId );
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public void buildProjects()
        throws ContinuumException
    {
        buildProjects( true );
    }

    public void buildProjects( boolean force )
        throws ContinuumException
    {
        for ( Iterator i = getProjects().iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            buildProject( project.getId(), force );
        }
    }

    public void buildProject( String projectId )
        throws ContinuumException
    {
        core.buildProject( projectId, true );
    }

    public void buildProject( String projectId, boolean force )
        throws ContinuumException
    {
        core.buildProject( projectId, force );
    }

    // ----------------------------------------------------------------------
    // Build inforation
    // ----------------------------------------------------------------------

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumException
    {
        return core.getBuild( buildId );
    }

    public Collection getBuildsForProject( String projectId )
        throws ContinuumException
    {
        return core.getBuildsForProject( projectId );
    }

    public ContinuumBuildResult getBuildResultForBuild( String buildId )
        throws ContinuumException
    {
        return core.getBuildResultForBuild( buildId );
    }

    public Collection getChangedFilesForBuild( String buildId )
        throws ContinuumException
    {
        return core.getChangedFilesForBuild( buildId );
    }

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    public void addAntProject( AntProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_TARGETS, project.getTargets() );

        core.addProjectFromScm( project.getScmUrl(),
                                AntBuildExecutor.ID,
                                project.getName(),
                                project.getNagEmailAddress(),
                                project.getVersion(),
                                project.getCommandLineArguments(),
                                configuration );
    }

    public AntProject getAntProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        AntProject ap = new AntProject();

        copyProject( p, ap );

        ap.setTargets( p.getConfiguration().getProperty( AntBuildExecutor.CONFIGURATION_TARGETS ) );

        ap.setExecutable( p.getConfiguration().getProperty( AntBuildExecutor.CONFIGURATION_EXECUTABLE ) );

        return ap;
    }

    public void updateAntProject( AntProject project )
        throws ContinuumException
    {
        updateProject( project );

        // ----------------------------------------------------------------------
        // The configuration will be null here because the "executable" and
        // "targets" fields in the AntProject are used to create the
        // configuration. We probably don't even need the configuration.
        // ----------------------------------------------------------------------

        Properties configuration = new Properties();

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        configuration.setProperty( AntBuildExecutor.CONFIGURATION_TARGETS, project.getTargets() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public void addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        core.addProjectsFromUrl( metadataUrl, MavenOneContinuumProjectBuilder.ID );
    }

    public void addMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        core.addProjectFromScm( project.getScmUrl(),
                                MavenOneBuildExecutor.ID,
                                project.getName(),
                                project.getNagEmailAddress(),
                                project.getVersion(),
                                project.getCommandLineArguments(),
                                configuration );
    }

    public MavenOneProject getMavenOneProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        MavenOneProject mp = new MavenOneProject();

        copyProject( p, mp );

        mp.setGoals( p.getConfiguration().getProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS ) );

        return mp;
    }

    public void updateMavenOneProject( MavenOneProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( MavenOneBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public void addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        core.addProjectsFromUrl( metadataUrl, MavenTwoContinuumProjectBuilder.ID );
    }

    public void addMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        core.addProjectFromScm( project.getScmUrl(),
                                MavenTwoBuildExecutor.ID,
                                project.getName(),
                                project.getNagEmailAddress(),
                                project.getVersion(),
                                project.getCommandLineArguments(),
                                configuration );
    }

    public MavenTwoProject getMavenTwoProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        MavenTwoProject mp = new MavenTwoProject();

        copyProject( p, mp );

        mp.setGoals( p.getConfiguration().getProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS ) );

        return mp;
    }

    public void updateMavenTwoProject( MavenTwoProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS, project.getGoals() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    public void addShellProject( ShellProject project )
        throws ContinuumException
    {
        Properties configuration = new Properties();

        configuration.setProperty( ShellBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        core.addProjectFromScm( project.getScmUrl(),
                                ShellBuildExecutor.ID,
                                project.getName(),
                                project.getNagEmailAddress(),
                                project.getVersion(),
                                project.getCommandLineArguments(),
                                configuration );
    }

    public ShellProject getShellProject( String id )
        throws ContinuumException
    {
        ContinuumProject p = getProject( id );

        ShellProject sp = new ShellProject();

        copyProject( p, sp );

        sp.setExecutable( p.getConfiguration().getProperty( ShellBuildExecutor.CONFIGURATION_EXECUTABLE ) );

        return sp;
    }

    public void updateShellProject( ShellProject project )
        throws ContinuumException
    {
        updateProject( project );

        Properties configuration = new Properties();

        configuration.setProperty( ShellBuildExecutor.CONFIGURATION_EXECUTABLE, project.getExecutable() );

        updateProjectConfiguration( project.getId(), configuration );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void updateProject( ContinuumProject project )
        throws ContinuumException
    {
        core.updateProject( project.getId(),
                            project.getName(),
                            project.getScmUrl(),
                            project.getNagEmailAddress(),
                            project.getVersion(),
                            project.getCommandLineArguments() );
    }

    private void copyProject( ContinuumProject p1, ContinuumProject p2 )
    {
        p2.setId( p1.getId() );

        p2.setName( p1.getName() );

        p2.setScmUrl( p1.getScmUrl() );

        p2.setNagEmailAddress( p1.getNagEmailAddress() );

        p2.setVersion( p1.getVersion() );

        p2.setExecutorId( p1.getExecutorId() );
    }
}
