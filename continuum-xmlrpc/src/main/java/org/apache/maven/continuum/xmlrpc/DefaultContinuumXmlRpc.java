package org.apache.maven.continuum.xmlrpc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.execution.maven.m2.MavenTwoBuildExecutor;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.xmlrpc.ContinuumXmlRpc"
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

    public Hashtable removeProject( int projectId )
    {
        try
        {
            continuum.removeProject( projectId );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.removeProject()", "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getProject( int projectId )
    {
        try
        {
            Project project = continuum.getProjectWithAllDetails( projectId );

            return makeHashtable( "project", convertContinuumProject( project, false ) );
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
            return handleException( "ContinuumXmlRpc.getProject()", "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getProjects()
    {
        try
        {
            Collection projects = continuum.getAllProjectsWithAllDetails( 0, 0 );

            Vector results = new Vector( projects.size() );

            for ( Iterator it = projects.iterator(); it.hasNext(); )
            {
                results.add( convertContinuumProject( it.next(), true ) );
            }

            return makeHashtable( "projects", results );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProjects()", null, e );
        }
    }

    public Hashtable getBuildResult( int buildId )
    {
        try
        {
            BuildResult build = continuum.getBuildResult( buildId );

            return makeHashtable( "build", convertContinuumBuild( build, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildResult()", "Build id: '" + buildId + "'.", e );
        }
    }

    public Hashtable getBuildResultsForProject( int projectId )
    {
        try
        {
            Collection buildResults = continuum.getBuildResultsForProject( projectId );

            Vector results = new Vector( buildResults.size() );

            for ( Iterator it = buildResults.iterator(); it.hasNext(); )
            {
                results.add( convertContinuumBuild( it.next(), true ) );
            }

            return makeHashtable( "builds", results );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildResultsForProject()", "Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getBuildOutput( int projectId, int buildId )
    {
        try
        {
            String output = continuum.getBuildOutput( projectId, buildId );

            return makeHashtable( "buildOutput", output );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildOutput()", "Project id: '" + projectId + "' Build id: '" + buildId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Build handling
    // ----------------------------------------------------------------------

    public Hashtable buildProject( int projectId, int trigger )
    {
        try
        {
            continuum.buildProject( projectId, trigger );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.buildProject()", "Project id: '" + projectId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public Hashtable addMavenTwoProject( String url )
    {
        try
        {
            ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

            Collection projects = result.getProjects();

            Collection projectIds = new Vector( projects.size() );

            for ( Iterator it = projects.iterator(); it.hasNext(); )
            {
                Project project = (Project) it.next();

                projectIds.add( new Integer( project.getId() ) );
            }

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenTwoProject()", "URL: '" + url + "'.", e );
        }
    }

    public Hashtable addMavenTwoProject( Hashtable mavenTwoProject )
    {
        try
        {
            Project project = new Project();

            xmlRpcHelper.hashtableToObject( mavenTwoProject, project );

            int projectId = continuum.addProject( project, MavenTwoBuildExecutor.ID );

            Collection projectIds = new Vector();

            projectIds.add( Integer.toString( projectId ) );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenTwoProject()", null, e );
        }
    }

    public Hashtable updateMavenTwoProject( Hashtable mavenTwoProject )
    {
        int id = getId( mavenTwoProject );

        try
        {
            Project project = continuum.getProject( id );

            xmlRpcHelper.hashtableToObject( mavenTwoProject, project );

            continuum.updateProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()", "Project id: '" + id + "'.", e );
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
                Project project = (Project) it.next();

                projectIds.add( new Integer( project.getId() ) );
            }

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenOneProject()", "URL: '" + url + "'.", e );
        }
    }

    public Hashtable addMavenOneProject( Hashtable mavenOneProject )
    {
        try
        {
            Project project = new Project();

            xmlRpcHelper.hashtableToObject( mavenOneProject, project );

            int projectId = continuum.addProject( project, MavenOneBuildExecutor.ID );

            Collection projectIds = new ArrayList();

            projectIds.add( Integer.toString( projectId ) );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenOneProject()", null, e );
        }
    }

    public Hashtable updateMavenOneProject( Hashtable mavenOneProject )
    {
        int id = getId( mavenOneProject );

        try
        {
            Project project = continuum.getProject( id );

            xmlRpcHelper.hashtableToObject( mavenOneProject, project );

            continuum.updateProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()", "Project id: '" + id + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    public Hashtable addAntProject( Hashtable antProject )
    {
        try
        {
            Project project = new Project();

            xmlRpcHelper.hashtableToObject( antProject, project );

            int projectId = continuum.addProject( project, AntBuildExecutor.ID );

            Collection projectIds = new ArrayList();

            projectIds.add( Integer.toString( projectId ) );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addAntProject()", null, e );
        }
    }

    public Hashtable updateAntProject( Hashtable antProject )
    {
        int id = getId( antProject );

        try
        {
            Project project = continuum.getProject( id );

            xmlRpcHelper.hashtableToObject( antProject, project );

            continuum.updateProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()", "Project id: '" + id + "'", e );
        }
    }

    // ----------------------------------------------------------------------
    // Shell Projects
    // ----------------------------------------------------------------------

    public Hashtable addShellProject( Hashtable shellProject )
    {
        try
        {
            Project project = new Project();

            xmlRpcHelper.hashtableToObject( shellProject, project );

            int projectId = continuum.addProject( project, ShellBuildExecutor.ID );

            Collection projectIds = new ArrayList();

            projectIds.add( Integer.toString( projectId ) );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.ShellProject()", null, e );
        }
    }

    public Hashtable updateShellProject( Hashtable shellProject )
    {
        int id = getId( shellProject );

        try
        {
            Project project = continuum.getProject( id );

            xmlRpcHelper.hashtableToObject( shellProject, project );

            continuum.updateProject( project );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.updateMavenTwoProject()", "Project id: '" + id + "'", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private int getId( Hashtable hashtable )
    {
        return Integer.valueOf( (String) hashtable.get( "id" ) ).intValue();
    }

    // ----------------------------------------------------------------------
    // Object to Hashtable converters
    // ----------------------------------------------------------------------

    private Hashtable convertContinuumProject( Object object, boolean summary )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "buildResults" );

        excludedProperties.add( "projectGroup" );

        excludedProperties.add( "checkoutResult" );

        if ( summary )
        {
            excludedProperties.add( "developers" );

            excludedProperties.add( "notifiers" );

            excludedProperties.add( "checkOutScmResult" );
        }

        Project project = (Project) object;

        Hashtable hashtable = xmlRpcHelper.objectToHashtable( project, excludedProperties );

        return hashtable;
    }

    private Hashtable convertContinuumBuild( Object object, boolean summary )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "project" );

        if ( summary )
        {
            excludedProperties.add( "scmResult" );
            excludedProperties.add( "trigger" );
            excludedProperties.add( "error" );
        }

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
