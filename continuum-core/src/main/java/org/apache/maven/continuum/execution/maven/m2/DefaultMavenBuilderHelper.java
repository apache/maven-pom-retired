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

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectDeveloper;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.SettingsUtils;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultMavenBuilderHelper
    extends AbstractLogEnabled
    implements MavenBuilderHelper, Contextualizable, Initializable
{
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

    private PlexusContainer container;

    private Settings settings;

    // ----------------------------------------------------------------------
    // MavenBuilderHelper Implementation
    // ----------------------------------------------------------------------

    public void mapMetadataToProject( File metadata, Project continuumProject )
        throws MavenBuilderHelperException
    {
        mapMavenProjectToContinuumProject( getMavenProject( metadata ), continuumProject );
    }

    public void mapMavenProjectToContinuumProject( MavenProject mavenProject, Project continuumProject )
        throws MavenBuilderHelperException
    {
        continuumProject.setName( getProjectName( mavenProject ) );

        continuumProject.setScmUrl( getScmUrl( mavenProject ) );

        if ( !"HEAD".equals( mavenProject.getScm().getTag() ) )
        {
            continuumProject.setScmTag( mavenProject.getScm().getTag() );
        }

        continuumProject.setVersion( getVersion( mavenProject ) );

        // ----------------------------------------------------------------------
        // GroupId
        // ----------------------------------------------------------------------

        if ( !StringUtils.isEmpty( mavenProject.getGroupId() ) )
        {
            continuumProject.setGroupId( mavenProject.getGroupId() );
        }
        else
        {
            throw new MavenBuilderHelperException( "Missing 'groupId' element in the POM." );
        }

        // ----------------------------------------------------------------------
        // artifactId
        // ----------------------------------------------------------------------

        if ( !StringUtils.isEmpty( mavenProject.getArtifactId() ) )
        {
            continuumProject.setArtifactId( mavenProject.getArtifactId() );
        }
        else
        {
            throw new MavenBuilderHelperException( "Missing 'artifactId' element in the POM." );
        }

        // ----------------------------------------------------------------------
        // Project Url
        // ----------------------------------------------------------------------

        if ( !StringUtils.isEmpty( mavenProject.getUrl() ) )
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
        // Parent
        // ----------------------------------------------------------------------

        if ( mavenProject.getParent() != null )
        {
            MavenProject parentProject = mavenProject.getParent();

            ProjectDependency parent = new ProjectDependency();

            parent.setGroupId( parentProject.getGroupId() );

            parent.setArtifactId( parentProject.getArtifactId() );

            parent.setVersion( parentProject.getVersion() );

            continuumProject.setParent( parent );
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

        List userNotifiers = new ArrayList();

        if ( continuumProject.getNotifiers() != null )
        {
            for ( int i = 0; i < continuumProject.getNotifiers().size(); i++ )
            {
                ProjectNotifier notifier = (ProjectNotifier) continuumProject.getNotifiers().get( i );

                if ( notifier.isFromUser() )
                {
                    ProjectNotifier userNotifier = new ProjectNotifier();

                    userNotifier.setType( notifier.getType() );

                    userNotifier.setConfiguration( notifier.getConfiguration() );

                    userNotifier.setFrom( notifier.getFrom() );

                    userNotifiers.add( userNotifier );
                }
            }
        }

        continuumProject.setNotifiers( getNotifiers( mavenProject ) );

        for ( Iterator i = userNotifiers.iterator(); i.hasNext(); )
        {
            ProjectNotifier notifier = (ProjectNotifier) i.next();

            continuumProject.addNotifier( notifier );
        }
    }

    public MavenProject getMavenProject( File file )
        throws MavenBuilderHelperException
    {
        MavenProject project;

        try
        {
            //   TODO: This seems like code that is shared with DefaultMaven, so it should be moved to the project
            //   builder perhaps

            if ( getLogger().isDebugEnabled() )
            {
                writeSettings( settings );
            }

            ProfileManager profileManager = new DefaultProfileManager( container );

            loadSettingsProfiles( profileManager, settings );

            project = projectBuilder.build( file, getRepository( settings ), profileManager, false );

            if ( getLogger().isDebugEnabled() )
            {
                writePom( project );
                writeActiveProfileStatement( project );
            }

        }
        catch ( Exception e )
        {
            String msg = "Cannot build maven project from " + file + " (" + e.getMessage() + ").";

            getLogger().error( msg, e );

            throw new MavenBuilderHelperException( msg, e );
        }

        // ----------------------------------------------------------------------
        // Validate the MavenProject using some Continuum rules
        // ----------------------------------------------------------------------

        // SCM connection
        Scm scm = project.getScm();

        if ( scm == null )
        {
            throw new MavenBuilderHelperException(
                "Missing 'scm' element in the " + getProjectName( project ) + " POM." );
        }

        String url = scm.getConnection();

        if ( StringUtils.isEmpty( url ) )
        {
            throw new MavenBuilderHelperException(
                "Missing 'connection' element in the 'scm' element in the " + getProjectName( project ) + " POM." );
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

        if ( mavenProject.getCiManagement() != null && mavenProject.getCiManagement().getNotifiers() != null )
        {
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

                notifier.setFrom( ProjectNotifier.FROM_PROJECT );

                notifier.setSendOnSuccess( projectNotifier.isSendOnSuccess() );

                notifier.setSendOnFailure( projectNotifier.isSendOnFailure() );

                notifier.setSendOnError( projectNotifier.isSendOnError() );

                notifier.setSendOnWarning( projectNotifier.isSendOnWarning() );

                notifiers.add( notifier );
            }
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

    private Settings getSettings()
        throws SettingsConfigurationException
    {
        try
        {
            return mavenSettingsBuilder.buildSettings();
        }
        catch ( IOException e )
        {
            throw new SettingsConfigurationException( "Error reading settings file", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new SettingsConfigurationException( e.getMessage(), e.getDetail(), e.getLineNumber(),
                                                      e.getColumnNumber() );
        }
    }

    private ArtifactRepository getRepository( Settings settings )
    {
        // ----------------------------------------------------------------------
        // Set our configured location as the default but try to use the defaults
        // as returned by the MavenSettings component.
        // ----------------------------------------------------------------------

        String localRepo = localRepository;

        if ( !( StringUtils.isEmpty( settings.getLocalRepository() ) ) )
        {
            localRepo = settings.getLocalRepository();
        }

        return artifactRepositoryFactory.createArtifactRepository( "local", "file://" + localRepo, repositoryLayout,
                                                                   null, null );
    }

    private void loadSettingsProfiles( ProfileManager profileManager, Settings settings )
    {
        List settingsProfiles = settings.getProfiles();

        if ( settingsProfiles != null && !settingsProfiles.isEmpty() )
        {
            List settingsActiveProfileIds = settings.getActiveProfiles();

            profileManager.explicitlyActivate( settingsActiveProfileIds );

            for ( Iterator it = settings.getProfiles().iterator(); it.hasNext(); )
            {
                org.apache.maven.settings.Profile rawProfile = (org.apache.maven.settings.Profile) it.next();

                Profile profile = SettingsUtils.convertFromSettingsProfile( rawProfile );

                profileManager.addProfile( profile );
            }
        }
    }

    private void writeSettings( Settings settings )
    {
        StringWriter sWriter = new StringWriter();

        SettingsXpp3Writer settingsWriter = new SettingsXpp3Writer();

        try
        {
            settingsWriter.write( sWriter, settings );

            StringBuffer message = new StringBuffer();

            message.append( "\n************************************************************************************" );
            message.append( "\nEffective Settings" );
            message.append( "\n************************************************************************************" );
            message.append( "\n" );
            message.append( sWriter.toString() );
            message.append( "\n************************************************************************************" );
            message.append( "\n\n" );

            getLogger().debug( message.toString() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Cannot serialize POM to XML.", e );
        }
    }

    private void writePom( MavenProject project )
    {
        StringBuffer message = new StringBuffer();

        Model pom = project.getModel();

        StringWriter sWriter = new StringWriter();

        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        try
        {
            pomWriter.write( sWriter, pom );

            message.append( "\n************************************************************************************" );
            message.append( "\nEffective POM for project \'" ).append( project.getId() ).append( "\'" );
            message.append( "\n************************************************************************************" );
            message.append( "\n" );
            message.append( sWriter.toString() );
            message.append( "\n************************************************************************************" );
            message.append( "\n\n" );

            getLogger().debug( message.toString() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Cannot serialize POM to XML.", e );
        }
    }

    private void writeActiveProfileStatement( MavenProject project )
    {
        List profiles = project.getActiveProfiles();

        StringBuffer message = new StringBuffer();

        message.append( "\n" );

        message.append( "\n************************************************************************************" );
        message.append( "\nActive Profiles for Project \'" ).append( project.getId() ).append( "\'" );
        message.append( "\n************************************************************************************" );
        message.append( "\n" );

        if ( profiles == null || profiles.isEmpty() )
        {
            message.append( "There are no active profiles." );
        }
        else
        {
            message.append( "The following profiles are active:\n" );

            for ( Iterator it = profiles.iterator(); it.hasNext(); )
            {
                Profile profile = (Profile) it.next();

                message.append( "\n - " ).append( profile.getId() ).append( " (source: " )
                    .append( profile.getSource() ).append( ")" );
            }

        }

        message.append( "\n************************************************************************************" );
        message.append( "\n\n" );

        getLogger().debug( message.toString() );
    }

    /**
     * @todo [BP] this might not be required if there is a better way to pass
     * them in. It doesn't feel quite right.
     * @todo [JC] we should at least provide a mapping of protocol-to-proxy for
     * the wagons, shouldn't we?
     */
    private void resolveParameters( Settings settings )
        throws ComponentLookupException, ComponentLifecycleException, SettingsConfigurationException
    {
        WagonManager wagonManager = (WagonManager) container.lookup( WagonManager.ROLE );

        try
        {
            Proxy proxy = settings.getActiveProxy();

            if ( proxy != null )
            {
                if ( proxy.getHost() == null )
                {
                    throw new SettingsConfigurationException( "Proxy in settings.xml has no host" );
                }

                wagonManager.addProxy( proxy.getProtocol(), proxy.getHost(), proxy.getPort(), proxy.getUsername(),
                                       proxy.getPassword(), proxy.getNonProxyHosts() );
            }

            for ( Iterator i = settings.getServers().iterator(); i.hasNext(); )
            {
                Server server = (Server) i.next();

                wagonManager.addAuthenticationInfo( server.getId(), server.getUsername(), server.getPassword(),
                                                    server.getPrivateKey(), server.getPassphrase() );

                wagonManager.addPermissionInfo( server.getId(), server.getFilePermissions(),
                                                server.getDirectoryPermissions() );

                if ( server.getConfiguration() != null )
                {
                    wagonManager.addConfiguration( server.getId(), (Xpp3Dom) server.getConfiguration() );
                }
            }

            for ( Iterator i = settings.getMirrors().iterator(); i.hasNext(); )
            {
                Mirror mirror = (Mirror) i.next();

                wagonManager.addMirror( mirror.getId(), mirror.getMirrorOf(), mirror.getUrl() );
            }
        }
        finally
        {
            container.release( wagonManager );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            settings = getSettings();

            resolveParameters( settings );
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Can't initialize '" + getClass().getName() + "'", e );
        }
    }
}
