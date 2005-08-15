package org.apache.maven.continuum.it;

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
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.xmlrpc.XmlRpcHelper;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class ContinuumXmlRpcClient
    implements Continuum
{
    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    private String host;

    private int port;

    private XmlRpcHelper helper;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private XmlRpcClient client;

    private static final String EOL = System.getProperty( "line.separator" );

    public ContinuumXmlRpcClient( String host, int port, XmlRpcHelper helper )
        throws MalformedURLException
    {
        this.host = host;

        this.port = port;

        this.helper = helper;

        client = new XmlRpcClientLite( host, port );
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    // ----------------------------------------------------------------------
    // Project
    // ----------------------------------------------------------------------

    public void removeProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void checkoutProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return (Collection) invoke( "getProjects", new Object[]{}, ContinuumProject.class );
    }

    public List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public ScmResult getScmResultForProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public ScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public Collection getProjects()
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public BuildResult getLatestBuildResultForProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Queues
    // ----------------------------------------------------------------------

    public boolean isInBuildingQueue( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Building
    // ----------------------------------------------------------------------

    public void buildProjects()
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void buildProjects( boolean force )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void buildProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void buildProject( String projectId, boolean force )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Maven 2.x projects.
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public String addMavenTwoProject( MavenTwoProject mavenTwoProject )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public MavenTwoProject getMavenTwoProject( String projectId )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void updateMavenTwoProject( MavenTwoProject mavenTwoProject )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Maven 1.x projects
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        return (ContinuumProjectBuildingResult) invoke( "addMavenOneProject", new Object[]{metadataUrl,},
                                                        ContinuumProjectBuildingResult.class );
    }

    public String addMavenOneProject( MavenOneProject mavenOneProject )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public MavenOneProject getMavenOneProject( String s )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void updateMavenOneProject( MavenOneProject mavenOneProject )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Ant Projects
    // ----------------------------------------------------------------------

    public String addAntProject( AntProject antProject )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public AntProject getAntProject( String s )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void updateAntProject( AntProject antProject )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    public ProjectNotifier getNotifier( String projectId, String notifierType )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void updateNotifier( String projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void addNotifier( String projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    public void removeNotifier( String projectId, String notifierType )
        throws ContinuumException
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Schedules
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Object invoke( String method, Object[] arguments, Class returnType )
        throws ContinuumException
    {
        Vector vector = new Vector( arguments.length );

        for ( int i = 0; i < arguments.length; i++ )
        {
            Object argument = arguments[i];

            vector.add( argument );
        }

        Hashtable returnValue;

        try
        {
            returnValue = (Hashtable) client.execute( "continuum." + method, vector );
        }
        catch ( XmlRpcException e )
        {
            throw new ContinuumException( "Error while invoking method '" + method + "'.", e );
        }
        catch ( IOException e )
        {
            throw new ContinuumException( "Error while invoking method '" + method + "'.", e );
        }

        String result = (String) returnValue.get( "result" );

        if ( StringUtils.isEmpty( result ) || !result.equals( "ok" ) )
        {
            String remoteMethod = (String) returnValue.get( "method" );

            String message = (String) returnValue.get( "message" );

            String stackTrace = (String) returnValue.get( "stackTrace" );

            throw new ContinuumException( "Error while calling the remote method '" + method + "'. " + EOL +
                "Result code: " + result + EOL + "Remote method: " + remoteMethod + EOL + "Message: " + message + "." +
                EOL + "Stack trace: " + stackTrace );
        }

        System.err.println( "return value: " + returnValue );

        Object object;

        try
        {
            object = returnType.newInstance();
        }
        catch ( InstantiationException e )
        {
            throw new ContinuumException( "Could not instantiate the return type '" + returnType.getName() + "'. " +
                "Make sure this type has a empy public constructor.", e );
        }
        catch ( IllegalAccessException e )
        {
            throw new ContinuumException( "Could not instantiate the return type '" + returnType.getName() + "'. " +
                "Make sure this type has a empy public constructor.", e );
        }

        try
        {
            helper.hashtableToObject( returnValue, object );
        }
        catch ( IntrospectionException e )
        {
            throw new ContinuumException( "Error while building the return object. " + "XMLRPC return value type: " +
                returnValue.getClass().getName() + ". " + "Method return type: " + returnType.getName() + ".", e );
        }
        catch ( IllegalAccessException e )
        {
            throw new ContinuumException( "Error while building the return object. " + "XMLRPC return value type: " +
                returnValue.getClass().getName() + ". " + "Method return type: " + returnType.getName() + ".", e );
        }
        catch ( InvocationTargetException e )
        {
            throw new ContinuumException( "Error while building the return object. " + "XMLRPC return value type: " +
                returnValue.getClass().getName() + ". " + "Method return type: " + returnType.getName() + ".", e );
        }

        return object;
    }
}
