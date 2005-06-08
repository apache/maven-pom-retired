package org.apache.maven.continuum.xmlrpc;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.core.ContinuumCore;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.maven.MavenOneContinuumProjectBuilder;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.UpdateScmResult;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.xmlrpc.ContinuumXmlRpc"
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumXmlRpc
    extends AbstractLogEnabled
    implements ContinuumXmlRpc
{
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    /**
     * @plexus.requirement
     */
    private ContinuumCore core;

    /**
     * @plexus.requirement
     */
    private XmlRpcHelper xmlRpcHelper;

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public Hashtable addMavenOneProject( String url )
    {
        try
        {
            Collection projectIds = core.addProjectsFromUrl( url, MavenOneContinuumProjectBuilder.ID );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenOneProject()",
                                    "URL: '" + url + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public Hashtable addMavenTwoProject( String url )
    {
        try
        {
            // TODO: Get the added projects and return the IDs
            continuum.addMavenTwoProject( url );

//            Collection projectIds = core.addProjectsFromUrl( url, MavenTwoContinuumProjectBuilder.ID );

//            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( new Vector(), false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenTwoProject()",
                                    "URL: '" + url + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Ant projects
    // ----------------------------------------------------------------------

    public Hashtable addAntProject( String scmUrl,
                                    String projectName,
                                    String nagEmailAddress,
                                    String version,
                                    String commandLineArguments,
                                    Hashtable configuration )
    {
        return addProjectFromScm( scmUrl,
                                  AntBuildExecutor.ID,
                                  projectName,
                                  nagEmailAddress,
                                  version,
                                  commandLineArguments,
                                  configuration );
    }

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    public Hashtable addShellProject( String scmUrl,
                                      String projectName,
                                      String nagEmailAddress,
                                      String version,
                                      String commandLineArguments,
                                      Hashtable configuration )
    {
        return addProjectFromScm( scmUrl,
                                  ShellBuildExecutor.ID,
                                  projectName,
                                  nagEmailAddress,
                                  version,
                                  commandLineArguments,
                                  configuration );
    }

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    private Hashtable addProjectFromScm( String scmUrl,
                                         String executorId,
                                         String projectName,
                                         String nagEmailAddress,
                                         String version,
                                         String commandLineArguments,
                                         Hashtable configuration )
    {
        try
        {
            Properties configurationProperties = new Properties();

            for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();

                configurationProperties.put( entry.getKey().toString(), entry.getValue().toString() );
            }

            String projectId = core.addProjectFromScm( scmUrl,
                                                       executorId,
                                                       projectName,
                                                       nagEmailAddress,
                                                       version,
                                                       commandLineArguments,
                                                       configurationProperties );

            return makeHashtable( "projectId", projectId );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addProjectFromScm()", null, e );
        }
    }

    public Hashtable getProject( String projectId )
    {
        try
        {
            Set excludedProperties = new HashSet();

            excludedProperties.add( "configuration" );

            ContinuumProject project = continuum.getProject( projectId );

            Hashtable hashtable = xmlRpcHelper.objectToHashtable( project, excludedProperties );

            Properties configuration = project.getConfiguration();

            Hashtable configurationHashtable = new Hashtable();

            for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();

                configurationHashtable.put( entry.getKey().toString(), entry.getValue().toString() );
            }

            hashtable.put( "configuration", configurationHashtable );

            CheckOutScmResult result = continuum.getCheckOutScmResultForProject( projectId );

            if ( result != null )
            {
                hashtable.put( "checkOutScmResult", xmlRpcHelper.objectToHashtable( result ) );
            }

            return makeHashtable( "project", hashtable );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable updateProject( String projectId,
                                    String name,
                                    String scmUrl,
                                    String nagEmailAddress,
                                    String version,
                                    String commandLineArguments )
    {
        try
        {
            ContinuumProject project = continuum.getProject( projectId );

            if ( project.getExecutorId().equals( MavenOneBuildExecutor.ID ) )
            {
                project = continuum.getMavenOneProject( projectId );
            }
            else if ( project.getExecutorId().equals( MavenTwoBuildExecutor.ID ) )
            {
                project = continuum.getMavenTwoProject( projectId );
            }
            else if ( project.getExecutorId().equals( AntBuildExecutor.ID ) )
            {
                project = continuum.getAntProject( projectId );
            }
            else if ( project.getExecutorId().equals( ShellBuildExecutor.ID ) )
            {
                project = continuum.getShellProject( projectId );
            }

            project.setName( name );

            project.setScmUrl( scmUrl );

            project.setNagEmailAddress( nagEmailAddress );

            project.setVersion( version );

            project.setCommandLineArguments( commandLineArguments );

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            if ( project instanceof MavenOneProject )
            {
                continuum.updateMavenOneProject( (MavenOneProject) project );
            }
            else if ( project instanceof MavenTwoProject )
            {
                continuum.updateMavenTwoProject( (MavenTwoProject) project );
            }
            else if ( project instanceof AntProject )
            {
                continuum.updateAntProject( (AntProject) project );
            }
            else if ( project instanceof ShellProject )
            {
                continuum.updateShellProject( (ShellProject) project );
            }
            else
            {
                return handleException( "ContinuumXmlRpc.updateProject()",
                                        "Project id: '" + projectId + "'.", new ContinuumException( "Unknown project type: " + project.getClass().getName() ) );
            }

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable updateProjectFromScm( String projectId )
    {
        try
        {
            continuum.updateProjectFromScm( projectId );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateProjectFromScm()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable updateProjectConfiguration( String projectId,
                                                 Hashtable configuration )
    {
        try
        {
            Properties configurationProperties = new Properties();

            for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();

                configurationProperties.put( entry.getKey().toString(), entry.getValue().toString() );
            }

            continuum.updateProjectConfiguration( projectId, configurationProperties );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateProjectConfiguration()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getAllProjects()
    {
        try
        {
            Vector projects = new Vector();

            for ( Iterator it = continuum.getAllProjects( 0, 0 ).iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                projects.add( xmlRpcHelper.objectToHashtable( project ) );
            }

            return makeHashtable( "projects", projects );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getAllProjects()", null, e );
        }
    }

    public Hashtable removeProject( String projectId )
    {
        try
        {
            continuum.removeProject( projectId );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.removeProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Builds
    // ----------------------------------------------------------------------

    public Hashtable buildProject( String projectId, boolean force )
    {
        try
        {
            continuum.buildProject( projectId, force );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.buildProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getBuildsForProject( String projectId, int start, int end )
    {
        try
        {
            // TODO: use start and end
            Iterator it = continuum.getBuildsForProject( projectId ).iterator();

            Vector builds = new Vector();

            Set excludedProperties = new HashSet();

            excludedProperties.add( "project" );

            while ( it.hasNext() )
            {
                ContinuumBuild continuumBuild = (ContinuumBuild) it.next();

                Hashtable build = xmlRpcHelper.objectToHashtable( continuumBuild, excludedProperties );

                UpdateScmResult result = continuumBuild.getUpdateScmResult();

                if ( result != null )
                {
                    build.put( "updateScmResult", xmlRpcHelper.objectToHashtable( result ) );
                }

                builds.add( build );
            }

            return makeHashtable( "builds", builds );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildsForProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getBuild( String buildId )
    {
        try
        {
            ContinuumBuild continuumBuild = continuum.getBuild( buildId );

            Set excludedProperties = new HashSet();

            excludedProperties.add( "project" );

            Hashtable build = makeHashtable( "build",
                                             xmlRpcHelper.objectToHashtable( continuumBuild, excludedProperties ) );

            UpdateScmResult result = continuumBuild.getUpdateScmResult();

            if ( result != null )
            {
                build.put( "updateScmResult", xmlRpcHelper.objectToHashtable( result ) );
            }

            return build;
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuild()",
                                    "Build id: '" + buildId + "'.", e );
        }
    }

    public Hashtable getBuildResult( String buildId )
    {
        try
        {
            ContinuumBuildResult result = continuum.getBuildResultForBuild( buildId );

            Set excludedProperties = new HashSet();

            excludedProperties.add( "build" );

            return makeHashtable( "buildResult", xmlRpcHelper.objectToHashtable( result, excludedProperties ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildResult()",
                                    "Build id: '" + buildId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Hashtable makeHashtable()
    {
        Hashtable hashtable = new Hashtable();

        hashtable.put( "result", "ok" );

        return hashtable;
    }

    private Hashtable makeHashtable( String property, Object object )
    {
        Hashtable hashtable = makeHashtable();

        hashtable.put( property, object );

        return hashtable;
    }

    private Hashtable handleException( String method, String message, Throwable throwable )
    {
        Hashtable hashtable = new Hashtable();

        hashtable.put( "result", "failure" );

        if ( throwable.getMessage() != null )
        {
            hashtable.put( "message", throwable.getMessage() );
        }
        else
        {
            hashtable.put( "message", "" );
        }

        if ( StringUtils.isEmpty( message ) )
        {
            hashtable.put( "method", method );
        }
        else
        {
            hashtable.put( "method", method + ": " + message );
        }

        hashtable.put( "stackTrace", getExceptionStackTrace( throwable ) );

        return hashtable;
    }

    private static String getExceptionStackTrace( Throwable ex )
    {
        StringWriter string = new StringWriter();

        PrintWriter writer = new PrintWriter( string );

        ex.printStackTrace( writer );

        writer.flush();

        return string.getBuffer().toString();
    }
 }
