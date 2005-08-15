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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.execution.shell.ShellBuildExecutor;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
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

    public Hashtable removeProject( String projectId )
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

    public Hashtable getProject( String projectId )
    {
        try
        {
            ContinuumProject project = continuum.getProject( projectId );

            return makeHashtable( "project", convertContinuumProject( project, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProject()", "Project id: '" + projectId + "'.", e );
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
                results.add( convertContinuumProject( it.next(), true ) );
            }

            return makeHashtable( "projects", results );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProjects()", null, e );
        }
    }

    public Hashtable getScmResultForProject( String projectId )
    {
        try
        {
            ScmResult result = continuum.getScmResultForProject( projectId );

            return makeHashtable( "scmResult", convertScmResult( result ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getScmResultForProject()", "Project id: '" + projectId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Build handling
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
                ContinuumProject project = (ContinuumProject) it.next();

                projectIds.add( project.getId() );
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
            MavenTwoProject project = new MavenTwoProject();

            xmlRpcHelper.hashtableToObject( mavenTwoProject, project );

            String projectId = continuum.addMavenTwoProject( project );

            Collection projectIds = new Vector();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenTwoProject()", null, e );
        }
    }

    public Hashtable updateMavenTwoProject( Hashtable mavenTwoProject )
    {
        String id = getId( mavenTwoProject );

        try
        {
            MavenTwoProject project = continuum.getMavenTwoProject( id );

            xmlRpcHelper.hashtableToObject( mavenTwoProject, project );

            continuum.updateMavenTwoProject( project );

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
                ContinuumProject project = (ContinuumProject) it.next();

                projectIds.add( project.getId() );
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
            MavenOneProject project = new MavenOneProject();

            xmlRpcHelper.hashtableToObject( mavenOneProject, project );

            String projectId = continuum.addMavenOneProject( project );

            Collection projectIds = new ArrayList();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addMavenOneProject()", null, e );
        }
    }

    public Hashtable updateMavenOneProject( Hashtable mavenOneProject )
    {
        String id = getId( mavenOneProject );

        try
        {
            MavenOneProject project = continuum.getMavenOneProject( id );

            xmlRpcHelper.hashtableToObject( mavenOneProject, project );

            continuum.updateMavenOneProject( project );

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
            AntProject project = new AntProject();

            xmlRpcHelper.hashtableToObject( antProject, project );

            String projectId = continuum.addAntProject( project );

            Collection projectIds = new ArrayList();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addAntProject()", null, e );
        }
    }

    public Hashtable updateAntProject( Hashtable antProject )
    {
        String id = getId( antProject );

        try
        {
            AntProject project = continuum.getAntProject( id );

            xmlRpcHelper.hashtableToObject( antProject, project );

            continuum.updateAntProject( project );

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
            ContinuumProject project = new ContinuumProject();

            xmlRpcHelper.hashtableToObject( shellProject, project );

            String projectId = continuum.addProject( project, ShellBuildExecutor.SHELL_EXECUTOR_ID );

            Collection projectIds = new ArrayList();

            projectIds.add( projectId );

            return makeHashtable( "projectIds", xmlRpcHelper.collectionToVector( projectIds, false ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.ShellProject()", null, e );
        }
    }

    public Hashtable updateShellProject( Hashtable shellProject )
    {
        String id = getId( shellProject );

        try
        {
            ContinuumProject project = continuum.getProject( id );

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

    private String getId( Hashtable hashtable )
    {
        return (String) hashtable.get( "id" );
    }

    // ----------------------------------------------------------------------
    // Object to Hashtable converters
    // ----------------------------------------------------------------------

    private Hashtable convertContinuumProject( Object object, boolean summary )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "builds" );

        if ( summary )
        {
            excludedProperties.add( "developers" );

            excludedProperties.add( "notifiers" );

            excludedProperties.add( "checkOutScmResult" );
        }

        ContinuumProject project = (ContinuumProject) object;

        Hashtable hashtable = xmlRpcHelper.objectToHashtable( project, excludedProperties );

        return hashtable;
    }

    private Hashtable convertContinuumBuild( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        excludedProperties.add( "project" );

        return xmlRpcHelper.objectToHashtable( object, excludedProperties );
    }

    private Hashtable convertScmFile( Object object )
        throws IllegalAccessException, InvocationTargetException
    {
        Set excludedProperties = new HashSet();

        return xmlRpcHelper.objectToHashtable( object, excludedProperties );
    }

    private Hashtable convertScmResult( Object object )
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
