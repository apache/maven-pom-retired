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

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumDeveloper;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Developer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.MavenSettingsBuilder;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultMavenBuilderHelper
    extends AbstractLogEnabled
    implements MavenBuilderHelper
{
    public static final String DEFAULT_TEST_OUTPUT_DIRECTORY = "target/surefire-reports";

    /** @requirement */
    private MavenProjectBuilder projectBuilder;

    /** @requirement */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /** @requirement */
    private MavenSettingsBuilder settingsBuilder;

    /** @requirement */
    private ArtifactRepositoryLayout repositoryLayout;

    /** @configuration */
    private String localRepository;

    // ----------------------------------------------------------------------
    // MavenBuilderHelper Implementation
    // ----------------------------------------------------------------------

    public void mapMetadataToProject( File metadata, ContinuumProject continuumProject )
        throws MavenBuilderHelperException
    {
        mapMavenProjectToContinuumProject( getMavenProject( metadata ), continuumProject );
    }

    public void mapMavenProjectToContinuumProject( MavenProject mavenProject, ContinuumProject continuumProject )
    {
        continuumProject.setNagEmailAddress( getNagEmailAddress( mavenProject ) );

        continuumProject.setName( getProjectName( mavenProject ) );

        continuumProject.setScmUrl( getScmUrl( mavenProject ) );

        continuumProject.setVersion( getVersion( mavenProject ) );

        if ( StringUtils.isEmpty( continuumProject.getCommandLineArguments() ) )
        {
            continuumProject.setCommandLineArguments( "-N" );
        }

        Properties configuration = continuumProject.getConfiguration();

        if ( StringUtils.isEmpty( configuration.getProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS ) ) )
        {
            configuration.setProperty( MavenTwoBuildExecutor.CONFIGURATION_GOALS, "clean:clean install" );
        }

        // ----------------------------------------------------------------------
        // Group
        // ----------------------------------------------------------------------

        System.out.println( "mavenProject.getGroupId() = " + mavenProject.getGroupId() );

        if ( mavenProject.getGroupId() != null )
        {
           continuumProject.setGroupId( mavenProject.getGroupId() );
        }

        // ----------------------------------------------------------------------
        // Project Url
        // ----------------------------------------------------------------------

        System.out.println( "mavenProject.getUrl() = " + mavenProject.getUrl() );

        if ( mavenProject.getUrl() != null )
        {
           continuumProject.setUrl( mavenProject.getUrl() );
        }

        // ----------------------------------------------------------------------
        // Test output directory
        // ----------------------------------------------------------------------

        continuumProject.setTestOutputDirectory( DEFAULT_TEST_OUTPUT_DIRECTORY );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        if ( mavenProject.getDevelopers() != null )
        {
            for ( Iterator i = mavenProject.getDevelopers().iterator(); i.hasNext(); )
            {
                Developer d = (Developer) i.next();

                ContinuumDeveloper cd = new ContinuumDeveloper();

                cd.setId( d.getId() );

                cd.setName( d.getName() );

                cd.setEmail( d.getEmail() );

                continuumProject.addDeveloper( cd );
            }
        }
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
            throw new MavenBuilderHelperException( "Cannot build maven project from " + file, e );
        }

        // ----------------------------------------------------------------------
        // Validate the MavenProject using some Continuum rules
        // ----------------------------------------------------------------------

        // Nag email address
        CiManagement ciManagement = project.getCiManagement();

        if ( ciManagement == null )
        {
            throw new MavenBuilderHelperException( "Missing CiManagement from the project descriptor." );
        }

        if ( StringUtils.isEmpty( getNagEmailAddress( project ) ) )
        {
            throw new MavenBuilderHelperException( "Missing nag email address from the continuous integration info." );
        }

        // SCM connection
        Scm scm = project.getScm();

        if ( scm == null )
        {
            throw new MavenBuilderHelperException( "Missing Scm from the project descriptor." );
        }

        String url = scm.getConnection();

        if ( StringUtils.isEmpty( url ) )
        {
            throw new MavenBuilderHelperException( "Missing anonymous scm connection url." );
        }

        // Version
        if ( StringUtils.isEmpty( project.getVersion() ) )
        {
            throw new MavenBuilderHelperException( "Missing version from the project descriptor." );
        }

        return project;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String getProjectName( MavenProject project )
    {
        String name = project.getName();

        return name;
    }

    private String getScmUrl( MavenProject project )
    {
        return project.getScm().getConnection();
    }

    private String getNagEmailAddress( MavenProject project )
    {
        for ( Iterator it = project.getCiManagement().getNotifiers().iterator(); it.hasNext(); )
        {
            Notifier notifier = (Notifier) it.next();

            if ( notifier.getType().equals( "mail" ) )
            {
                return notifier.getAddress();
            }
        }

        return null;
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
        Repository repository = new Repository();

        return artifactRepositoryFactory.createArtifactRepository( "local",
                                                                   "file://" + localRepository,
                                                                   repositoryLayout,
                                                                   repository.getSnapshotPolicy() );
    }
}
