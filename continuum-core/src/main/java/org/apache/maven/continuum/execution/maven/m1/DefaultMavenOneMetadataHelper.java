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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.ContinuumRecipientSource;
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
            throw new MavenOneMetadataHelperException( "Error while reading maven POM (" + e.getMessage() + ").", e );
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
        // description
        // ----------------------------------------------------------------------

        String shortDescription = getValue( mavenProject, "shortDescription", project.getDescription() );

        String description = getValue( mavenProject, "description", project.getDescription() );

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
                throw new MavenOneMetadataHelperException( "Missing both anonymous and developer SCM connection URLs." );
            }
        }

        // ----------------------------------------------------------------------
        // Developers
        // ----------------------------------------------------------------------

        Xpp3Dom developers = mavenProject.getChild( "developers" );

        if ( developers != null )
        {
            Xpp3Dom[] developersList = developers.getChildren();

            List cds = new ArrayList();

            for ( int i = 0; i < developersList.length; i++ )
            {
                Xpp3Dom developer = developersList[i];

                ProjectDeveloper cd = new ProjectDeveloper();

                cd.setScmId( getValue( developer, "id", null ) );

                cd.setName( getValue( developer, "name", null ) );

                cd.setEmail( getValue( developer, "email", null ) );

                cds.add( cd );
            }

            project.setDevelopers( cds );
        }

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        Xpp3Dom dependencies = mavenProject.getChild( "dependencies" );

        if ( dependencies != null )
        {
            Xpp3Dom[] dependenciesList = dependencies.getChildren();

            List deps = new ArrayList();

            for ( int i = 0; i < dependenciesList.length; i++ )
            {
                Xpp3Dom dependency = dependenciesList[i];

                ProjectDependency cd = new ProjectDependency();

                if ( getValue( dependency, "groupId", null ) != null )
                {
                    cd.setGroupId( getValue( dependency, "groupId", null ) );

                    cd.setArtifactId( getValue( dependency, "artifactId", null ) );
                }
                else
                {
                    cd.setGroupId( getValue( dependency, "id", null ) );

                    cd.setArtifactId( getValue( dependency, "id", null ) );
                }

                cd.setVersion( getValue( dependency, "version", null ) );

                deps.add( cd );
            }

            project.setDependencies( deps );
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
        }
        else
        {
            String nagEmailAddress = getValue( build, "nagEmailAddress", null );

            if ( nagEmailAddress != null )
            {
                Properties props = new Properties();

                props.put( ContinuumRecipientSource.ADDRESS_FIELD, nagEmailAddress );

                notifier.setConfiguration( props );

                notifier.setFrom( ProjectNotifier.FROM_PROJECT );
            }
        }

        if ( notifier == null && ( notifiers == null || notifiers.isEmpty() ) )
        {
        }
        else
        {
            if ( notifiers == null )
            {
                notifiers = new ArrayList();
            }
            notifiers.add( notifier );

            // Add notifier defined by user
            for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
            {
                ProjectNotifier notif = (ProjectNotifier) i.next();

                if ( notif.isFromUser() )
                {
                    ProjectNotifier userNotifier = new ProjectNotifier();

                    userNotifier.setType( notif.getType() );

                    userNotifier.setConfiguration( notif.getConfiguration() );

                    userNotifier.setFrom( notif.getFrom() );

                    notifiers.add( userNotifier );
                }
            }

        }

        // ----------------------------------------------------------------------
        // Make the project
        // ----------------------------------------------------------------------

        project.setGroupId( groupId );

        project.setArtifactId( artifactId );

        project.setVersion( version );

        project.setName( name );

        if ( StringUtils.isEmpty( shortDescription ) )
        {
            project.setDescription( description );
        }
        else
        {
            project.setDescription( shortDescription );
        }

        project.setScmUrl( scmConnection );

        project.setNotifiers( notifiers );
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
