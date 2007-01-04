package org.apache.maven.continuum.execution.maven.m1;

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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.ContinuumRecipientSource;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.apache.maven.continuum.execution.maven.m1.MavenOneMetadataHelper"
 *   role-hint="default"
 */
public class DefaultMavenOneMetadataHelper
    extends AbstractLogEnabled
    implements MavenOneMetadataHelper
{
    // ----------------------------------------------------------------------
    // MavenOneMetadataHelper Implementation
    // ----------------------------------------------------------------------

    /**
     * @deprecated Use {@link #mapMetadata(ContinuumProjectBuildingResult,File,Project)} instead
     */
    public void mapMetadata( File metadata, Project project )
        throws MavenOneMetadataHelperException
    {
        mapMetadata( new ContinuumProjectBuildingResult(), metadata, project );
    }

    public void mapMetadata( ContinuumProjectBuildingResult result, File metadata, Project project )
        throws MavenOneMetadataHelperException
    {
        Xpp3Dom mavenProject;

        try
        {
            mavenProject = Xpp3DomBuilder.build( new FileReader( metadata ) );
        }
        catch ( XmlPullParserException e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_XML_PARSE );
            
            getLogger().info( "Error while reading maven POM (" + e.getMessage() + ").", e );
            
            return;
        }
        catch ( FileNotFoundException e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_POM_NOT_FOUND );

            getLogger().info( "Error while reading maven POM (" + e.getMessage() + ").", e );

            return;
        }
        catch ( IOException e )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_UNKNOWN );
            
            getLogger().info( "Error while reading maven POM (" + e.getMessage() + ").", e );
            
            return;
        }

        // ----------------------------------------------------------------------
        // We cannot deal with projects that use the <extend/> element because
        // we don't have the whole source tree and we might be missing elements
        // that are present in the parent.
        // ----------------------------------------------------------------------

        String extend = getValue( mavenProject, "extend", null );

        if ( extend != null )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_EXTEND );
            
            getLogger().info( "Cannot use a POM with an 'extend' element." );
            
            return;
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
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_GROUPID );

                getLogger().info( "Missing 'groupId' element in the POM." );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }

            artifactId = getValue( mavenProject, "artifactId", project.getArtifactId() );

            if ( StringUtils.isEmpty( artifactId ) )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_ARTIFACTID );

                getLogger().info( "Missing 'artifactId' element in the POM." );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }
        }

        // ----------------------------------------------------------------------
        // version
        // ----------------------------------------------------------------------

        String version = getValue( mavenProject, "currentVersion", project.getVersion() );

        if ( StringUtils.isEmpty( project.getVersion() ) && StringUtils.isEmpty( version ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_VERSION );
            
            // Do not throw an exception or return here, gather up as many results as possible first.
        }

        // ----------------------------------------------------------------------
        // name
        // ----------------------------------------------------------------------

        String name = getValue( mavenProject, "name", project.getName() );

        if ( StringUtils.isEmpty( project.getName() ) && StringUtils.isEmpty( name ) )
        {
            result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_NAME );
            
            // Do not throw an exception or return here, gather up as many results as possible first.
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

        String scmConnection = null;

        if ( repository == null )
        {
            if ( !StringUtils.isEmpty( project.getScmUrl() ) )
            {
                scmConnection = project.getScmUrl();
            }
            else
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_REPOSITORY );

                // Do not throw an exception or return here, gather up as many results as possible first.
            }
        }
        else
        {
            scmConnection = getValue( repository, "developerConnection", project.getScmUrl() );

            scmConnection = getValue( repository, "connection", scmConnection );

            if ( StringUtils.isEmpty( scmConnection ) )
            {
                result.addError( ContinuumProjectBuildingResult.ERROR_MISSING_SCM );
                
                // Do not throw an exception or return here, gather up as many results as possible first.
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

        List notifiers = new ArrayList();

        // Add project Notifier
        if ( build != null )
        {
            String nagEmailAddress = getValue( build, "nagEmailAddress", null );

            if ( nagEmailAddress != null )
            {
                Properties props = new Properties();

                props.put( ContinuumRecipientSource.ADDRESS_FIELD, nagEmailAddress );

                ProjectNotifier notifier = new ProjectNotifier();

                notifier.setConfiguration( props );

                notifier.setFrom( ProjectNotifier.FROM_PROJECT );

                notifiers.add( notifier );
            }
        }

        // Add all user notifiers
        if ( project.getNotifiers() != null && !project.getNotifiers().isEmpty() )
        {
            for ( Iterator i = project.getNotifiers().iterator(); i.hasNext(); )
            {
                ProjectNotifier notif = (ProjectNotifier) i.next();

                if ( notif.isFromUser() )
                {
                    notifiers.add( notif );
                }
            }
        }
        
        // ----------------------------------------------------------------------
        // Handle Errors / Results
        // ----------------------------------------------------------------------
        
        if ( result.hasErrors() )
        {
            // prevent project creation if there are errors.
            return;
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
