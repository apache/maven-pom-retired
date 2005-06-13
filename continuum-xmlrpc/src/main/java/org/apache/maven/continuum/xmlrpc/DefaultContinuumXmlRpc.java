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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.scm.CheckOutScmResult;

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
    private XmlRpcHelper xmlRpcHelper;

    // ----------------------------------------------------------------------
    // ContinuumXmlRpc Implementation
    // ----------------------------------------------------------------------

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

//    public Hashtable updateProjectConfiguration( String projectId,
//                                                 Hashtable configuration )
//    {
//        try
//        {
//            Properties configurationProperties = new Properties();
//
//            for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
//            {
//                Map.Entry entry = (Map.Entry) it.next();
//
//                configurationProperties.put( entry.getKey().toString(), entry.getValue().toString() );
//            }
//
//            continuum.updateProjectConfiguration( projectId, configurationProperties );
//
//            return makeHashtable();
//        }
//        catch ( Throwable e )
//        {
//            return handleException( "ContinuumXmlRpc.updateProjectConfiguration()",
//                                    "Project id: '" + projectId + "'.", e );
//        }
//    }

    public Hashtable getProject( String projectId )
    {
        try
        {
            return makeHashtable( "project", convertContinuumProject( continuum.getProject( projectId ) ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getProjects()
    {
        try
        {
            Collection projects = continuum.getAllProjects( 0, 0 );

            Vector results = new Vector( projects.size() );

            for ( Iterator it = projects.iterator(); it.hasNext(); )
            {
                results.add( convertContinuumProject( it.next() ) );
            }

            return makeHashtable( "projects", results );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProjects()", null, e );
        }
    }

    public Hashtable getCheckOutScmResultForProject( String projectId )
    {
        try
        {
            CheckOutScmResult result = continuum.getCheckOutScmResultForProject( projectId );

            return makeHashtable( "checkOutScmResult", convertCheckOutScmResult( result ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getCheckOutScmResultForProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getLatestBuildForProject( String projectId )
    {
        try
        {
            ContinuumBuild build = continuum.getLatestBuildForProject( projectId );

            return makeHashtable( "latestBuild", convertContinuumBuild( build ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getCheckOutScmResultForProject()",
                                    "Project id: '" + projectId + "'.", e );
        }

    }

    // ----------------------------------------------------------------------
    // Build handling
    // ----------------------------------------------------------------------

    public Hashtable buildProject( String projectId,
                                   boolean force )
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

    public Hashtable getBuild( String buildId )
    {
        try
        {
            ContinuumBuild build = continuum.getBuild( buildId );

            return makeHashtable( "build", convertContinuumBuild( build ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuild()",
                                    "Build id: '" + buildId + "'.", e );
        }
    }

    public Hashtable getBuildsForProject( String projectId,
                                          int start,
                                          int end )
    {
        try
        {
            if ( start != 0 || end != 0 )
            {
                getLogger().warn( "ContinuumXmlRpc.getBuildsForProject() " +
                                  "doesn't support usage of the start and end parameters yet." );
            }

            // TODO: use start and end
            Collection builds = continuum.getBuildsForProject( projectId );

            Vector result = new Vector( builds.size() );

            for ( Iterator it = builds.iterator(); it.hasNext(); )
            {
                result.add( convertContinuumBuild( it.next() ) );
            }

            return makeHashtable( "builds", result );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildsForProject()",
                                    "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getBuildResultForBuild( String buildId )
    {
        try
        {
            ContinuumBuildResult result = continuum.getBuildResultForBuild( buildId );

            return makeHashtable( "buildResult", convertContinuumBuildResult( result ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildResultForProject()",
                                    "Build id: '" + buildId + "'.", e );
        }
    }

    public Hashtable getChangedFilesForBuild( String buildId )
    {
        try
        {
            Collection changedFiles = continuum.getChangedFilesForBuild( buildId );

            Vector result = new Vector( changedFiles.size() );

            for ( Iterator it = changedFiles.iterator(); it.hasNext(); )
            {
                result.add( convertScmFile( it.next() ) );
            }

            return makeHashtable( "changedFiles", result );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildResultForProject()",
                                    "Build id: '" + buildId + "'.", e );
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
            ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

            Collection projects = result.getProjects();

            Collection projectIds = new Vector( projects.size() );

            for ( Iterator it = projects.iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                projectIds.add( project.getId() );

                getLogger().info( "project id: " + project.getId() );
            }

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenTwoProject()",
                                    "URL: '" + url + "'.", e );
        }
    }

    public Hashtable addMavenTwoProject( Hashtable mavenTwoProject )
    {
        try
        {
            MavenTwoProject project = new MavenTwoProject();

            xmlRpcHelper.hashtableToObject( mavenTwoProject, project );

            String projectId = continuum.addMavenTwoProject( project );

            // TODO: Get the added projects and return the IDs
            Collection projectIds = new Vector();

            projectIds.add( projectId );

            getLogger().info( "project id: " + projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenTwoProject()", null, e );
        }
    }

    public Hashtable updateMavenTwoProject( Hashtable mavenTwoProject )
    {
        MavenTwoProject project = new MavenTwoProject();

        try
        {
            xmlRpcHelper.hashtableToObject( mavenTwoProject, project );

            continuum.updateMavenTwoProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()",
                                    "Project id: " + project.getId(), e );
        }
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public Hashtable addMavenOneProject( String url )
    {
        try
        {
            ContinuumProjectBuildingResult result = continuum.addMavenOneProject( url );

            Collection projects = result.getProjects();

            Collection projectIds = new Vector( projects.size() );

            for ( Iterator it = projects.iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                projectIds.add( project.getId() );

                getLogger().info( "project id: " + project.getId() );
            }

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenOneProject()",
                                    "URL: '" + url + "'.", e );
        }
    }

    public Hashtable addMavenOneProject( Hashtable mavenOneProject )
    {
        try
        {
            MavenOneProject project = new MavenOneProject();

            xmlRpcHelper.hashtableToObject( mavenOneProject, project );

            String projectId = continuum.addMavenOneProject( project );

            Collection projectIds = new ArrayList();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenOneProject()",
                                    null, e );
        }
    }

    public Hashtable updateMavenOneProject( Hashtable mavenOneProject )
    {
        MavenOneProject project = new MavenOneProject();

        try
        {
            xmlRpcHelper.hashtableToObject( mavenOneProject, project );

            continuum.updateMavenOneProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()",
                                    "Project id: " + project.getId(), e );
        }

    }

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    public Hashtable addAntProject( Hashtable antProject )
    {
        try
        {
            AntProject project = new AntProject();

            xmlRpcHelper.hashtableToObject( antProject, project );

            String projectId = continuum.addAntProject( project );

            Collection projectIds = new ArrayList();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addAntProject()",
                                    null, e );
        }
    }

    public Hashtable updateAntProject( Hashtable antProject )
    {
        AntProject project = new AntProject();

        try
        {
            xmlRpcHelper.hashtableToObject( antProject, project );

            continuum.updateAntProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()",
                                    "Project id: " + project.getId(), e );
        }
    }

    // ----------------------------------------------------------------------
    // Shell Projects
    // ----------------------------------------------------------------------

    public Hashtable addShellProject( Hashtable shellProject )
    {
        try
        {
            ShellProject project = new ShellProject();

            xmlRpcHelper.hashtableToObject( shellProject, project );

            String projectId = continuum.addShellProject( project );

            Collection projectIds = new ArrayList();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.ShellProject()",
                                    null, e );
        }
    }

    public Hashtable updateShellProject( Hashtable shellProject )
    {
        ShellProject project = new ShellProject();

        try
        {
            xmlRpcHelper.hashtableToObject( shellProject, project );

            continuum.updateShellProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()",
                                    "Project id: " + project.getId(), e );
        }
    }

    // ----------------------------------------------------------------------
    // Object to Hashtable converters
    // ----------------------------------------------------------------------

    private Hashtable convertContinuumProject( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "configuration" );

        excludedProperties.add( "builds" );

        excludedProperties.add( "developers" );

        ContinuumProject project = (ContinuumProject) object;

        Hashtable hashtable = xmlRpcHelper.objectToHashtable( project, excludedProperties );

//        Properties configuration = project.getConfiguration();
//
//        Hashtable configurationHashtable = new Hashtable();
//
//        for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
//        {
//            Map.Entry entry = (Map.Entry) it.next();
//
//            configurationHashtable.put( entry.getKey().toString(), entry.getValue().toString() );
//        }
//
//        hashtable.put( "configuration", configurationHashtable );

        return hashtable;
    }

    private Hashtable convertContinuumBuild( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "project" );

        return xmlRpcHelper.objectToHashtable( object, excludedProperties );
    }

    private Hashtable convertContinuumBuildResult( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "build" );

        return xmlRpcHelper.objectToHashtable( object, excludedProperties );
    }

    private Hashtable convertScmFile( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        return xmlRpcHelper.objectToHashtable( object, excludedProperties );
    }

    private Hashtable convertCheckOutScmResult( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        return xmlRpcHelper.objectToHashtable( object, excludedProperties );
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
