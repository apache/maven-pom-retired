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

import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.builder.maven.m2.MavenShellBuilder;
import org.apache.maven.continuum.builder.maven.m1.Maven1Builder;
import org.apache.maven.continuum.builder.ant.AntBuilder;
import org.apache.maven.continuum.builder.shell.ShellBuilder;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.UpdateScmResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumBuildResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.utils.ContinuumUtils;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: DefaultContinuumXmlRpc.java,v 1.3 2005/04/04 15:25:13 trygvis Exp $
 */
public class DefaultContinuumXmlRpc
    extends AbstractLogEnabled
    implements ContinuumXmlRpc
{
    /** @requirement */
    private Continuum continuum;

    /** @requirement */
    private ContinuumStore store;

    /** @requirement */
    private XmlRpcHelper xmlRpcHelper;

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public Hashtable addMavenTwoProject( String url )
    {
        try
        {
            String projectId = continuum.addProjectFromUrl( url, MavenShellBuilder.ID );

            return makeHashtable( "projectId", projectId );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addProjectFromScm(): url: '" + url + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects
    // ----------------------------------------------------------------------

    public Hashtable addMavenOneProject( String url )
    {
        try
        {
            String projectId = continuum.addProjectFromUrl( url, Maven1Builder.ID );

            return makeHashtable( "projectId", projectId );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addProjectFromScm(): url: '" + url + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Ant projects
    // ----------------------------------------------------------------------

    public Hashtable addAntProject( String scmUrl,
                                    String projectName,
                                    String nagEmailAddress,
                                    String version,
                                    Hashtable configuration )
    {
        return addProjectFromScm( scmUrl, AntBuilder.ID, projectName, nagEmailAddress, version, configuration );
    }

    // ----------------------------------------------------------------------
    // Shell projects
    // ----------------------------------------------------------------------

    public Hashtable addShellProject( String scmUrl,
                                      String projectName,
                                      String nagEmailAddress,
                                      String version,
                                      Hashtable configuration )
    {
        return addProjectFromScm( scmUrl, ShellBuilder.ID, projectName, nagEmailAddress, version, configuration );
    }

    // ----------------------------------------------------------------------
    // Projects
    // ----------------------------------------------------------------------

    protected Hashtable addProjectFromUrl( String url, String builderType )
    {
        try
        {
            String projectId = continuum.addProjectFromUrl( new URL( url ), builderType );

            return makeHashtable( "projectId", projectId );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addProjectFromScm(): url: '" + url + "'.", e );
        }
    }

    protected Hashtable addProjectFromScm( String scmUrl,
                                           String builderType,
                                           String projectName,
                                           String nagEmailAddress,
                                           String version,
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

            String projectId = continuum.addProjectFromScm( scmUrl,
                                                            builderType,
                                                            projectName,
                                                            nagEmailAddress,
                                                            version,
                                                            configurationProperties );

            return makeHashtable( "projectId", projectId );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.addProjectFromScm().", e );
        }
    }

    public Hashtable getProject( String projectId )
    {
        try
        {
            Set excludedProperties = new HashSet();

            excludedProperties.add( "configuration" );

            ContinuumProject project = store.getProject( projectId );

            Hashtable hashtable = xmlRpcHelper.objectToHashtable( project, excludedProperties );

            Properties configuration = project.getConfiguration();

            Hashtable configurationHashtable = new Hashtable();

            for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();

                configurationHashtable.put( entry.getKey().toString(), entry.getValue().toString() );
            }

            hashtable.put( "configuration", configurationHashtable );

            CheckOutScmResult result = store.getCheckOutScmResultForProject( projectId );

            if ( result != null )
            {
                hashtable.put( "checkOutScmResult", xmlRpcHelper.objectToHashtable( result ) );
            }

            return makeHashtable( "project", hashtable );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getProject(): project id: '" + projectId + "'.", e );
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
            return handleException( "ContinuumXmlRpc.updateProjectFromScm(): Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable updateProjectConfiguration( String projectId, Hashtable configuration )
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
            return handleException( "ContinuumXmlRpc.updateProjectConfiguration(): Project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getAllProjects()
    {
        try
        {
            Vector projects = new Vector();

            for ( Iterator it = store.getAllProjects(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                projects.add( xmlRpcHelper.objectToHashtable( project ) );
            }

            return makeHashtable( "projects", projects );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getAllProjects().", e );
        }
    }

    public Hashtable removeProject( String projectId )
    {
        try
        {
            store.removeProject( projectId );

            return makeHashtable();
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.removeProject(). Project id: '" + projectId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Builds
    // ----------------------------------------------------------------------

    public Hashtable buildProject( String projectId )
    {
        try
        {
            return makeHashtable( "buildId", continuum.buildProject( projectId ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.buildProject(): project id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getBuildsForProject( String projectId, int start, int end )
    {
        try
        {
            Iterator it = store.getBuildsForProject( projectId, start, end );

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
            return handleException( "ContinuumXmlRpc.getBuildsForProject(): id: '" + projectId + "'.", e );
        }
    }

    public Hashtable getBuild( String buildId )
    {
        try
        {
            ContinuumBuild continuumBuild = store.getBuild( buildId );

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
            return handleException( "ContinuumXmlRpc.getBuild(): id: '" + buildId + "'.", e );
        }
    }

    public Hashtable getBuildResult( String buildId )
    {
        try
        {
            ContinuumBuildResult result = store.getBuildResultForBuild( buildId );

            Set excludedProperties = new HashSet();

            excludedProperties.add( "build" );

            return makeHashtable( "buildResult", xmlRpcHelper.objectToHashtable( result, excludedProperties ) );
        }
        catch ( Throwable e )
        {
            return handleException( "ContinuumXmlRpc.getBuildResult(): id: '" + buildId + "'.", e );
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

    private Hashtable handleException( String method, Throwable throwable )
    {
//        getLogger().error( "Error while executing '" + method + "'.", throwable );

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

        hashtable.put( "method", method );

        hashtable.put( "stackTrace", ContinuumUtils.getExceptionStackTrace( throwable ) );

        return hashtable;
    }
}
