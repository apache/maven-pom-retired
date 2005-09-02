package org.apache.maven.continuum.execution.maven.m1;

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

import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.notification.ContinuumRecipientSource;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultMavenOneMetadataHelper
    extends AbstractLogEnabled
    implements MavenOneMetadataHelper
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // MavenOneMetadataHelper Implementation
    // ----------------------------------------------------------------------

    public void mapMetadata( File metadata, Project project )
        throws MavenOneMetadataHelperException
    {
        Xpp3Dom mavenProject;

        try
        {
            mavenProject = Xpp3DomBuilder.build( new FileReader( metadata ) );
        }
        catch ( Exception e )
        {
            throw new MavenOneMetadataHelperException( "Error while reading maven POM.", e );
        }

        // ----------------------------------------------------------------------
        // We cannot deal with projects that use the <extend/> element because
        // we don't have the whole source tree and we might be missing elements
        // that are present in the parent.
        // ----------------------------------------------------------------------

        String extend = getValue( mavenProject, "extend", null );

        if ( extend != null )
        {
            throw new MavenOneMetadataHelperException( "Cannot use a POM with an 'extend' element." );
        }

        // ----------------------------------------------------------------------
        // Artifact and group id
        // ----------------------------------------------------------------------

        String groupId;

        String artifactId;

        String id = getValue( mavenProject, "id", null );

        if ( !StringUtils.isEmpty( id ) )
        {
            groupId = id;

            artifactId = id;
        }
        else
        {
            groupId = getValue( mavenProject, "groupId", project.getGroupId() );

            if ( StringUtils.isEmpty( groupId ) )
            {
                throw new MavenOneMetadataHelperException( "Missing 'groupId' element in the POM." );
            }

            artifactId = getValue( mavenProject, "artifactId", project.getArtifactId() );

            if ( StringUtils.isEmpty( artifactId ) )
            {
                throw new MavenOneMetadataHelperException( "Missing 'artifactId' element in the POM." );
            }
        }

        // ----------------------------------------------------------------------
        // version
        // ----------------------------------------------------------------------

        String version = getValue( mavenProject, "currentVersion", project.getVersion() );

        if ( StringUtils.isEmpty( project.getVersion() ) && StringUtils.isEmpty( version ) )
        {
            throw new MavenOneMetadataHelperException( "Missing 'version' element in the POM." );
        }

        // ----------------------------------------------------------------------
        // name
        // ----------------------------------------------------------------------

        String name = getValue( mavenProject, "name", project.getName() );

        if ( StringUtils.isEmpty( project.getName() ) && StringUtils.isEmpty( name ) )
        {
            throw new MavenOneMetadataHelperException( "Missing 'name' element in POM." );
        }

        // ----------------------------------------------------------------------
        // scm
        // ----------------------------------------------------------------------

        Xpp3Dom repository = mavenProject.getChild( "repository" );

        String scmConnection;

        if ( repository == null )
        {
            if ( !StringUtils.isEmpty( project.getScmUrl() ) )
            {
                scmConnection = project.getScmUrl();
            }
            else
            {
                throw new MavenOneMetadataHelperException( "Missing 'repository' element in the POM." );
            }
        }
        else
        {
            scmConnection = getValue( repository, "developerConnection", project.getScmUrl() );

            scmConnection = getValue( repository, "connection", scmConnection );

            if ( StringUtils.isEmpty( scmConnection ) )
            {
                throw new MavenOneMetadataHelperException(
                    "Missing both anonymous and developer SCM connection URLs." );
            }
        }

        // ----------------------------------------------------------------------
        // notifiers
        // ----------------------------------------------------------------------

        Xpp3Dom build = mavenProject.getChild( "build" );

        List notifiers = null;

        ProjectNotifier notifier = new ProjectNotifier();

        if ( build == null )
        {
            if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
            {
                notifiers = project.getNotifiers();
            }
            else
            {
                throw new MavenOneMetadataHelperException( "Missing 'build' element in the POM." );
            }
        }
        else
        {
            String currentNagEmailAddress = null;

            if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
            {
                for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
                {
                    ProjectNotifier notif = (ProjectNotifier) i.next();

                    // Can we have an other type for maven 1 project?
                    if ( "mail".equals( notif.getType() ) )
                    {
                        currentNagEmailAddress = (String) notif.getConfiguration().get(
                            ContinuumRecipientSource.ADDRESS_FIELD );
                    }
                }
            }

            String nagEmailAddress = getValue( build, "nagEmailAddress", currentNagEmailAddress );

            if ( nagEmailAddress != null )
            {
                Properties props = new Properties();

                props.put( ContinuumRecipientSource.ADDRESS_FIELD, nagEmailAddress );

                notifier.setConfiguration( props );
            }
        }

        if ( notifiers == null && notifier.getConfiguration().isEmpty() )
        {
            throw new MavenOneMetadataHelperException(
                "Missing 'nagEmailAddress' element in the 'build' element in the POM." );
        }
        else
        {
            if ( notifiers == null )
            {
                notifiers = new ArrayList();
            }

            notifiers.add( notifier );
        }

        // ----------------------------------------------------------------------
        // Make the project
        // ----------------------------------------------------------------------

        project.setGroupId( groupId );

        project.setArtifactId( artifactId );

        project.setVersion( version );

        project.setName( name );

        project.setScmUrl( scmConnection );

        project.setNotifiers( notifiers );

        BuildDefinition bd = new BuildDefinition();

        bd.setArguments( "" );

        bd.setGoals( "clean:clean jar:install" );

        bd.setBuildFile( "project.xml" );

        try
        {
            Schedule schedule = store.getScheduleByName( DefaultContinuumInitializer.DEFAULT_SCHEDULE_NAME );

            bd.setSchedule( schedule );
        }
        catch ( ContinuumStoreException e )
        {
            getLogger().warn( "Can't get default schedule.", e );
        }

        project.addBuildDefinition( bd );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String getValue( Xpp3Dom dom, String key, String defaultValue )
    {
        Xpp3Dom child = dom.getChild( key );

        if ( child == null )
        {
            return defaultValue;
        }

        return child.getValue();
    }
}
