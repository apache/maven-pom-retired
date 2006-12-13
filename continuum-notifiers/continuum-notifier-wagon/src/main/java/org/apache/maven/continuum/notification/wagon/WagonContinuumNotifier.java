package org.apache.maven.continuum.notification.wagon;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.apache.maven.continuum.notification.ContinuumNotificationDispatcher;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Site;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.CommandExecutionException;
import org.apache.maven.wagon.CommandExecutor;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.notification.NotificationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:hisidro@exist.com">Henry Isidro</a>
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 * @plexus.component role="org.codehaus.plexus.notification.notifier.Notifier" role-hint="wagon"
 */
public class WagonContinuumNotifier
    extends AbstractContinuumNotifier
    implements Contextualizable
{
    public static final String KEY_BUILD_DEFINITION = "build-definition";

    public static final String BUILD_OUTPUT_FILE_NAME = "buildresult.txt";
    
    private static final String CONTEXT_MAVEN_PROJECT = "CONTEXT_MAVEN_PROJECT";
    
    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;
    
    /**
     * @plexus.requirement
     */
    private WagonManager wagonManager;
    
    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;
    
    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder settingsBuilder;
    
    /**
     * @plexus.configuration
     */
    private String localRepository;
    
    
    private Settings settings;
    
    private ProfileManager profileManager;
    
    private PlexusContainer container;
    
    
    public void sendNotification( String source, Set recipients, Map configuration, Map context )
        throws NotificationException
    {
        Project project = (Project) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT );

        ProjectNotifier projectNotifier =
            (ProjectNotifier) context.get( ContinuumNotificationDispatcher.CONTEXT_PROJECT_NOTIFIER );

        BuildResult build = (BuildResult) context.get( ContinuumNotificationDispatcher.CONTEXT_BUILD );
        
        BuildDefinition buildDefinition = (BuildDefinition) context.get( KEY_BUILD_DEFINITION );
        
        // ----------------------------------------------------------------------
        // If there wasn't any building done, don't notify
        // ----------------------------------------------------------------------
        if ( build == null )
        {
            return;
        }
        
        // ----------------------------------------------------------------------
        // Deloy build result to given url 
        // ----------------------------------------------------------------------
        try
        {
            /*
             * acquire the MavenProject associated to the Project in context
             */
            MavenProject mavenProject = getMavenProject( project, buildDefinition );
            configuration.put( CONTEXT_MAVEN_PROJECT, mavenProject );
            
            if ( source.equals( ContinuumNotificationDispatcher.MESSAGE_ID_BUILD_COMPLETE ) )
            {
                buildComplete( project, projectNotifier, build, configuration );
            }
        }
        catch ( ContinuumException e )
        {
            throw new NotificationException( "Error while notifiying.", e );
        }
    }
    
    private void buildComplete( Project project, ProjectNotifier projectNotifier, BuildResult build, Map configuration )
        throws ContinuumException
    {
        MavenProject mavenProject = (MavenProject) configuration.get( CONTEXT_MAVEN_PROJECT );
        
        DistributionManagement distributionManagement = mavenProject.getDistributionManagement();
        if ( distributionManagement == null )
        {
            throw new ContinuumException( "Missing distribution management information in the project" );
        }

        Site site = distributionManagement.getSite();
        if ( site == null )
        {
            throw new ContinuumException( "Missing site information in the distribution management element in the project.." );
        }

        String url = site.getUrl();
        String id = site.getId();
        if ( url == null )
        {
            throw new ContinuumException( "The URL to the site is missing in the project descriptor." );
        }

        Repository repository = new Repository( id, url );

        Wagon wagon;
        try
        {
            wagon = wagonManager.getWagon( repository.getProtocol() );
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new ContinuumException( "Unsupported protocol: '" + repository.getProtocol() + "'", e );
        }

        if ( !wagon.supportsDirectoryCopy() )
        {
            throw new ContinuumException( "Wagon protocol '" + repository.getProtocol() + "' doesn't support directory copying" );
        }

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                Debug debug = new Debug();

                wagon.addSessionListener( debug );
                wagon.addTransferListener( debug );
            }

            ProxyInfo proxyInfo = wagonManager.getProxy( repository.getProtocol() );
            
            if ( proxyInfo != null )
            {
                wagon.connect( repository, wagonManager.getAuthenticationInfo( id ), proxyInfo );
            }
            else
            {
                wagon.connect( repository, wagonManager.getAuthenticationInfo( id ) );
            }
            
            File buildOutputFile = configurationService.getBuildOutputFile( build.getId(), build.getProject().getId() );

            wagon.put( buildOutputFile, BUILD_OUTPUT_FILE_NAME );

            // TODO: current wagon uses zip which will use the umask on remote host instead of honouring our settings
            //  Force group writeable
            if ( wagon instanceof CommandExecutor )
            {
                CommandExecutor exec = (CommandExecutor) wagon;
                exec.executeCommand( "chmod -Rf g+w " + repository.getBasedir() );
            }
        }
        catch ( ConfigurationException e )
        {
            throw new ContinuumException( "Error uploading build results to deployed site.", e );
        }
        catch ( ResourceDoesNotExistException e )
        {
            throw new ContinuumException( "Error uploading site", e );
        }
        catch ( TransferFailedException e )
        {
            throw new ContinuumException( "Error uploading site", e );
        }
        catch ( AuthorizationException e )
        {
            throw new ContinuumException( "Error uploading site", e );
        }
        catch ( ConnectionException e )
        {
            throw new ContinuumException( "Error uploading site", e );
        }
        catch ( AuthenticationException e )
        {
            throw new ContinuumException( "Error uploading site", e );
        }
        catch ( CommandExecutionException e )
        {
            throw new ContinuumException( "Error uploading site", e );
        }
        finally
        {
            try
            {
                wagon.disconnect();
            }
            catch ( ConnectionException e )
            {
                getLogger().error( "Error disconnecting wagon - ignored", e );
            }
        }
    }
    
    public void sendNotification( String arg0, Set arg1, Properties arg2 )
        throws NotificationException
    {
        throw new NotificationException( "Not implemented." );
    }
    
    private MavenProject getMavenProject( Project project, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        File projectWorkingDir = new File( configurationService.getWorkingDirectory(), Integer.toString( project.getId() ) );
        File pomFile = new File( projectWorkingDir, buildDefinition.getBuildFile() );
        
        MavenProject mavenProject = null;
        
        try
        {
            mavenProject = projectBuilder.build( pomFile, getLocalRepository(), getProfileManager( settings ) );
        }
        catch ( ProjectBuildingException e )
        {
            throw new ContinuumException( "Unable to acquire the MavenProject in " + pomFile.getAbsolutePath(), e);
        }
        
        return mavenProject;
    }
    
    private Settings getSettings()
    {
        if ( settings == null )
        {
            try
            {
                settings = settingsBuilder.buildSettings();
            }
            catch ( IOException e )
            {
                getLogger().error( "Failed to get Settings" , e);
            }
            catch ( XmlPullParserException e )
            {
                getLogger().error( "Failed to get Settings" , e);
            }
        }
    
        return settings;
    }
    
    private ArtifactRepository getLocalRepository()
    {
        String repo = localRepository;
        
        if ( getSettings() != null && !StringUtils.isEmpty( getSettings().getLocalRepository() ) )
        {
            repo = getSettings().getLocalRepository();
        }
        
        return new DefaultArtifactRepository( "local-repository", "file://" + repo, new DefaultRepositoryLayout() );
    }

    private ProfileManager getProfileManager( Settings settings )
    {
        if ( profileManager == null )
        {
            profileManager = new DefaultProfileManager( container, getSettings() );
        }

        return profileManager;
    }
    
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
