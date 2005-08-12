package org.apache.maven.continuum.execution.maven.m2;

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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultMavenBuilderHelper
    extends AbstractLogEnabled
    implements MavenBuilderHelper
{
    public static final String DEFAULT_TEST_OUTPUT_DIRECTORY = "target/surefire-reports";

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @plexus.requirement
     */
    private ArtifactRepositoryLayout repositoryLayout;

    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder mavenSettingsBuilder;

    /**
     * @plexus.configuration
     */
    private String localRepository;

    // ----------------------------------------------------------------------
    // MavenBuilderHelper Implementation
    // ----------------------------------------------------------------------

    public void mapMetadataToProject( File metadata, MavenTwoProject continuumProject )
        throws MavenBuilderHelperException
    {
        mapMavenProjectToContinuumProject( getMavenProject( metadata ), continuumProject );
    }

    public void mapMavenProjectToContinuumProject( MavenProject mavenProject, MavenTwoProject continuumProject )
        throws MavenBuilderHelperException
    {
        continuumProject.setName( getProjectName( mavenProject ) );

        continuumProject.setScmUrl( getScmUrl( mavenProject ) );

        continuumProject.setVersion( getVersion( mavenProject ) );

        if ( StringUtils.isEmpty( continuumProject.getCommandLineArguments() ) )
        {
            // ----------------------------------------------------------------------
            // Run in non-interactive mode and non-recursive mode
            // ----------------------------------------------------------------------

            continuumProject.setCommandLineArguments( "-N -B" );
        }

        if ( StringUtils.isEmpty( continuumProject.getGoals() ) )
        {
            continuumProject.setGoals( "clean:clean install" );
        }

        // ----------------------------------------------------------------------
        // GroupId
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( mavenProject.getGroupId() ) )
        {
            continuumProject.setGroupId( mavenProject.getGroupId() );
        }

        // ----------------------------------------------------------------------
        // artifactId
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( mavenProject.getArtifactId() ) )
        {
            continuumProject.setArtifactId( mavenProject.getArtifactId() );
        }

        // ----------------------------------------------------------------------
        // Project Url
        // ----------------------------------------------------------------------

        if ( StringUtils.isEmpty( mavenProject.getUrl() ) )
        {
            continuumProject.setUrl( mavenProject.getUrl() );
        }

        // ----------------------------------------------------------------------
        // Developers
        // ----------------------------------------------------------------------

        if ( mavenProject.getDevelopers() != null )
        {
            List developers = new ArrayList();

            for ( Iterator i = mavenProject.getDevelopers().iterator(); i.hasNext(); )
            {
                Developer d = (Developer) i.next();

                ProjectDeveloper cd = new ProjectDeveloper();

                cd.setScmId( d.getId() );

                cd.setName( d.getName() );

                cd.setEmail( d.getEmail() );

                developers.add( cd );
            }

            continuumProject.setDevelopers( developers );
        }

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        if ( mavenProject.getDependencies() != null )
        {
            List dependencies = new ArrayList();

            for ( Iterator i = mavenProject.getDependencies().iterator(); i.hasNext(); )
            {
                Dependency dependency = (Dependency) i.next();

                ProjectDependency cd = new ProjectDependency();

                cd.setGroupId( dependency.getGroupId() );

                cd.setArtifactId( dependency.getArtifactId() );

                cd.setVersion( dependency.getVersion() );

                dependencies.add( cd );
            }

            continuumProject.setDependencies( dependencies );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        continuumProject.setNotifiers( getNotifiers( mavenProject ) );
    }

    public MavenProject getMavenProject( File file )
        throws MavenBuilderHelperException
    {
        MavenProject project;

        try
        {
            // TODO: we probably need to pass in some profiles here, perhaps from settings.xml
            //   This seems like code that is shared with DefaultMaven, so it should be mobed to the project
            //   builder perhaps
            project = projectBuilder.build( file, getRepository(), Collections.EMPTY_LIST );
        }
        catch ( Exception e )
        {
            String msg = "Cannot build maven project from " + file + ".";

            getLogger().error( msg, e );

            throw new MavenBuilderHelperException( msg, e );
        }

        // ----------------------------------------------------------------------
        // Validate the MavenProject using some Continuum rules
        // ----------------------------------------------------------------------

        // Nag email address
        CiManagement ciManagement = project.getCiManagement();

        if ( ciManagement == null )
        {
            throw new MavenBuilderHelperException( "Missing 'ciManagement' element in the POM." );
        }

        if ( getNotifiers( project ).isEmpty() )
        {
            throw new MavenBuilderHelperException(
                "Missing 'notifiers' element in the 'ciManagement' element in the POM." );
        }

        // SCM connection
        Scm scm = project.getScm();

        if ( scm == null )
        {
            throw new MavenBuilderHelperException( "Missing 'scm' element in the POM." );
        }

        String url = scm.getConnection();

        if ( StringUtils.isEmpty( url ) )
        {
            throw new MavenBuilderHelperException( "Missing 'connection' element in the 'scm' element in the POM." );
        }

        return project;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String getProjectName( MavenProject project )
    {
        String name = project.getName();

        if ( StringUtils.isEmpty( name ) )
        {
            return project.getId();
        }

        return name;
    }

    private String getScmUrl( MavenProject project )
    {
        return project.getScm().getConnection();
    }

    private List getNotifiers( MavenProject mavenProject )
        throws MavenBuilderHelperException
    {
        List notifiers = new ArrayList();

        for ( Iterator i = mavenProject.getCiManagement().getNotifiers().iterator(); i.hasNext(); )
        {
            Notifier projectNotifier = (Notifier) i.next();

            ProjectNotifier notifier = new ProjectNotifier();

            if ( StringUtils.isEmpty( projectNotifier.getType() ) )
            {
                throw new MavenBuilderHelperException( "Missing type from notifier." );
            }

            notifier.setType( projectNotifier.getType() );

            if ( projectNotifier.getConfiguration() == null )
            {
                throw new MavenBuilderHelperException( "Notifier configuration cannot be null." );
            }

            notifier.setConfiguration( projectNotifier.getConfiguration() );

            notifiers.add( notifier );
        }

        return notifiers;
    }

    private String getVersion( MavenProject project )
    {
        return project.getVersion();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ArtifactRepository getRepository()
    {
        // ----------------------------------------------------------------------
        // Set our configured location as the default but try to use the defaults
        // as returned by the MavenSettings component.
        // ----------------------------------------------------------------------

        String localRepository = this.localRepository;

        try
        {
            Settings settings = mavenSettingsBuilder.buildSettings();

            localRepository = settings.getLocalRepository();
        }
        catch ( IOException e )
        {
            getLogger().warn( "Error while building Maven settings.", e );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().warn( "Error while building Maven settings.", e );
        }

        return artifactRepositoryFactory.createArtifactRepository( "local", "file://" + localRepository,
                                                                   repositoryLayout, null, null );
    }
}
