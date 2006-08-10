package org.apache.maven.continuum.project.builder.cc;

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

import org.apache.maven.continuum.execution.ant.AntBuildExecutor;
import org.apache.maven.continuum.execution.maven.m1.MavenOneBuildExecutor;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.AbstractContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.formica.util.MungedHttpsURL;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.project.builder.ContinuumProjectBuilder"
 * role-hint="cc-builder"
 */
public class CruiseControlProjectBuilder
    extends AbstractContinuumProjectBuilder
{
    public static final String ID = "cc-builder";

    private final static String[] IGNORED_MODIFICATION_SET_TYPES = new String[]{"alwaysbuild", "buildstatus",
        "forceonly",};

    private final static String[] IGNORED_SCHEDULE_TYPES = new String[]{"pause",};

    // ----------------------------------------------------------------------
    // ContinuumProjectBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumProjectBuildingResult buildProjectsFromMetadata( URL url, String username, String password )
        throws ContinuumProjectBuilderException
    {
        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        Xpp3Dom dom = downloadAndBuildDom( url, username, password );

        Xpp3Dom[] projects = dom.getChildren( "project" );

        for ( int i = 0; i < projects.length; i++ )
        {
            Xpp3Dom project = projects[i];

            Project continuumProject = findProject( project.getChild( "schedule" ) );

            // ----------------------------------------------------------------------
            // Create the project
            // ----------------------------------------------------------------------

            String name = project.getAttribute( "name" );

            if ( StringUtils.isEmpty( name ) )
            {
                throw new ContinuumProjectBuilderException(
                    "Missing required attribute 'name' from 'project' element." );
            }

            continuumProject.setName( name );

            // ----------------------------------------------------------------------
            // Scm url
            // ----------------------------------------------------------------------

            Xpp3Dom modifactionsets = project.getChild( "modificationset" );

            if ( modifactionsets == null )
            {
                throw new ContinuumProjectBuilderException(
                    "The configuration must contain at a 'modificationset' element." );
            }

            String scmUrl = findScmUrl( modifactionsets.getChildren() );

            continuumProject.setScmUrl( scmUrl );

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            Xpp3Dom publishers = project.getChild( "publishers" );

            if ( publishers != null )
            {
                String emailAddress = findNagEmailAddress( publishers.getChild( "email" ) );

                if ( emailAddress == null )
                {
                    emailAddress = findNagEmailAddress( publishers.getChild( "htmlemail" ) );
                }

                ProjectNotifier notifier = new ProjectNotifier();

                Properties props = new Properties();

                props.put( "address", emailAddress );

                notifier.setConfiguration( props );

                List notifiers = new ArrayList();

                continuumProject.setNotifiers( notifiers );
            }

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            continuumProject.setVersion( "UNKNOWN" );

            result.addProject( continuumProject );
        }

        return result;
    }

    private Project findProject( Xpp3Dom schedule )
        throws ContinuumProjectBuilderException
    {
        if ( schedule == null )
        {
            throw new ContinuumProjectBuilderException( "The configuration has to include a 'schedule' element." );
        }

        Project project = null;

        Xpp3Dom[] children = schedule.getChildren();

        for ( int i = 0; i < children.length; i++ )
        {
            Xpp3Dom child = children[i];

            if ( contains( child.getName(), IGNORED_SCHEDULE_TYPES ) )
            {
                continue;
            }

            if ( child.getName().equals( "ant" ) )
            {
                if ( project != null )
                {
                    throw new ContinuumProjectBuilderException(
                        "A configuration can only have a single 'ant' or 'maven' schedule." );
                }

                project = new Project();

                project.setExecutorId( AntBuildExecutor.ID );

                // TODO: shared construction of the default build definition
                BuildDefinition bd = new BuildDefinition();
                bd.setArguments( "" );
                bd.setGoals( "clean build" );
                bd.setBuildFile( "build.xml" );
                project.addBuildDefinition( bd );
            }
            else if ( child.getName().equals( "maven" ) )
            {
                if ( project != null )
                {
                    throw new ContinuumProjectBuilderException(
                        "A configuration can only have a single 'ant' or 'maven' schedule." );
                }

                project = new Project();

                project.setExecutorId( MavenOneBuildExecutor.ID );

                // TODO: shared construction of the default build definition
                BuildDefinition bd = new BuildDefinition();
                bd.setArguments( "" );
                bd.setGoals( "clean:clean jar:install" );
                bd.setBuildFile( "project.xml" );
                project.addBuildDefinition( bd );
            }
            else
            {
                throw new ContinuumProjectBuilderException( "Can't handle schedule '" + schedule.getName() +
                    "'. Continuum only supports 'ant' and 'maven' schedules." );
            }
        }

        if ( project == null )
        {
            throw new ContinuumProjectBuilderException(
                "There must be exactly one 'ant' or 'maven' build scheduled.." );
        }

        return project;
    }

    private String findScmUrl( Xpp3Dom[] modifactionsets )
        throws ContinuumProjectBuilderException
    {
        String scmUrl = null;

        for ( int j = 0; j < modifactionsets.length; j++ )
        {
            Xpp3Dom modifactionset = modifactionsets[j];

            if ( contains( modifactionset.getName(), IGNORED_MODIFICATION_SET_TYPES ) )
            {
                continue;
            }

            if ( modifactionset.getName().equals( "cvs" ) )
            {
                if ( scmUrl != null )
                {
                    throw new ContinuumProjectBuilderException(
                        "A 'modificationset' element can only contain a single 'cvs' or 'svn' element." );
                }

                String cvsrot = modifactionset.getAttribute( "cvsroot" );

                if ( StringUtils.isEmpty( cvsrot ) )
                {
                    throw new ContinuumProjectBuilderException(
                        "A 'cvsroot' attribute is required when using a cvs modification set. The usage of 'localworkingcopy' is not supported." );
                }

                String tag = modifactionset.getAttribute( "tag" );

                if ( StringUtils.isEmpty( tag ) )
                {
                    throw new ContinuumProjectBuilderException( "Continuum doesn't support CVS tags." );
                }

                scmUrl = "scm:cvs:" + cvsrot;
            }
            else if ( modifactionset.getName().equals( "svn" ) )
            {
                if ( scmUrl != null )
                {
                    throw new ContinuumProjectBuilderException(
                        "A 'modificationset' element can only contain a single 'cvs' or 'svn' element." );
                }

                String repositoryLocation = modifactionset.getAttribute( "repositoryLocation" );

                if ( StringUtils.isEmpty( repositoryLocation ) )
                {
                    throw new ContinuumProjectBuilderException(
                        "A 'repositoryLocation' attribute is required when using a svn modification set. The usage of 'localworkingcopy' is not supported." );
                }

                scmUrl = "scm:svn:" + repositoryLocation;
            }
            else
            {
                throw new ContinuumProjectBuilderException(
                    "Unsupported modification set found '" + modifactionset.getName() + "'." );
            }

            // FIXME: this break ensures that null-checks for scmURL above
            // are useless - scmURL is always null. Either remove the break or drop
            // the checks.
            break;
        }

        if ( scmUrl == null )
        {
            throw new ContinuumProjectBuilderException(
                "The configuration must contain at least one 'modificationset'." );
        }

        return scmUrl;
    }

    private String findNagEmailAddress( Xpp3Dom email )
    {
        if ( email == null )
        {
            return null;
        }

        Xpp3Dom[] failure = email.getChildren( "failure" );

        if ( failure.length == 0 )
        {
            return null;
        }

        String nagEmailAddress = failure[0].getAttribute( "address" );

        if ( StringUtils.isEmpty( nagEmailAddress ) )
        {
            return null;
        }

        return nagEmailAddress;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private boolean contains( String target, String[] data )
    {
        for ( int i = 0; i < data.length; i++ )
        {
            if ( data[i].equals( target ) )
            {
                return true;
            }
        }

        return false;
    }

    private Xpp3Dom downloadAndBuildDom( URL url, String username, String password )
        throws ContinuumProjectBuilderException
    {
        try
        {
            getLogger().info( "Downloading " + url );

            URL u = url;

            if ( url.getProtocol().startsWith("http") )
            {
                u = new MungedHttpsURL( url.toString() ).getURL();
            }

            return Xpp3DomBuilder.build( new InputStreamReader( u.openStream() ) );
        }
        catch ( XmlPullParserException e )
        {
            throw new ContinuumProjectBuilderException( "Error while parsing the CruiseControl configuration file.",
                                                        e );
        }
        catch ( IOException e )
        {
            throw new ContinuumProjectBuilderException( "Error while downloading the CruiseControl configuration file.",
                                                        e );
        }
    }
}
