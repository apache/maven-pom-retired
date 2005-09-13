package org.apache.maven.continuum.updater;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.updater.exception.UpdaterException;
import org.apache.maven.continuum.updater.model.UpdaterModel;
import org.apache.maven.continuum.updater.model.Version;
import org.apache.maven.continuum.updater.model.io.xpp3.ContinuumUpdaterXpp3Reader;
import org.apache.maven.continuum.updater.util.WagonManager;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.component.repository.io.PlexusTools;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.updater.UpdaterManager"
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultUpdaterManager
    extends AbstractLogEnabled
    implements UpdaterManager, Contextualizable
{
    /**
     * @plexus.requirement
     *   role="org.apache.maven.continuum.updater.Updater"
     */
    private Map updaters;

    /**
     * @plexus.requirement
     *   role="org.apache.maven.continuum.updater.util.WagonManager"
     */
    private WagonManager wagonManager;

    private PlexusContainer container;

    public void execute( String userVersion, File continuumHome )
        throws UpdaterException
    {
        InputStream confAsStream;

        confAsStream = DefaultUpdaterManager.class.getClassLoader().getResourceAsStream( "updater.xml" );

        InputStreamReader reader = new InputStreamReader( confAsStream );

        ContinuumUpdaterXpp3Reader xpp3Reader = new ContinuumUpdaterXpp3Reader();

        UpdaterModel model = null;

        try
        {
            model = xpp3Reader.read( reader );
        }
        catch ( Exception e )
        {
            throw new UpdaterException( "Can't find updater configuration file.", e );
        }

        boolean versionFounded = false;

        for( Iterator i = model.getVersions().iterator(); i.hasNext(); )
        {
            Version v = (Version) i.next();

            if ( v.getName().equals( userVersion ) )
            {
                versionFounded = true;
            }

            if ( versionFounded && v.getComponentRole() != null )
            {
                update( userVersion, v, continuumHome, model );
            }
        }

        if ( !versionFounded )
        {
            getLogger().warn( "There are no updater for your version(" + userVersion + ")\n" + getAvailableVersions( model ) );
        }
    }

    private String getAvailableVersions( UpdaterModel model )
    {
        StringBuffer sb = new StringBuffer();

        for( Iterator i = model.getVersions().iterator(); i.hasNext(); )
        {
            Version v = (Version) i.next();

            if ( v.getComponentRole() != null )
            {
                if ( sb.length() != 0 )
                {
                    sb.append( "\n" );
                }

                sb.append( v.getName() );
            }
        }

        return sb.toString();
    }

    private File downloadContinuum( UpdaterModel model, File continuumHome )
        throws UpdaterException
    {
        File downloadedFile = null;

        try
        {
            String latestUpdaterComponentRole = ( (Version) model.getVersions().get( model.getVersions().size() - 2 ) )
                .getComponentRole();

            Updater latestUpdater = (Updater) updaters.get( latestUpdaterComponentRole );

            URL url = new URL( latestUpdater.getReleaseUrl() );

            String filename = url.getFile().substring( url.getFile().lastIndexOf( "/" ) + 1 );

            downloadedFile = new File( continuumHome.getParentFile(), filename );

            getLogger().info( "==> Download " + url + " in " + downloadedFile.getAbsolutePath() );

            if ( downloadedFile.exists() )
            {
                downloadedFile.delete();
            }

            wagonManager.getFile( url, downloadedFile, "warn" );
        }
        catch ( Exception e )
        {
            throw new UpdaterException( "Can't download the new release", e );
        }

        return downloadedFile;
    }

    private void update( String userVersion, Version version, File continuumHome, UpdaterModel model )
        throws UpdaterException
    {
        getLogger().info( "************************************************************************" );
        getLogger().info( "Update version " + version.getName() + " with component " + version.getComponentRole() );
        getLogger().info( "************************************************************************" );

        Updater updater = (Updater) updaters.get( version.getComponentRole() );

        if ( updater == null )
        {
            throw new UpdaterException( "Updater " + version.getComponentRole() + " doesn't exist." );
        }

        try
        {
            backup( continuumHome, version.getName() );

            updateContinuumFiles( continuumHome, model );

            getLogger().info( "==> Update database" );

            File oldApplicationXml = updater.getOldApplicationXml( getBackupDirectory( continuumHome,
                                                                                       version.getName() ) );

            FileUtils.copyFile( oldApplicationXml, getApplicationXmlBackup( oldApplicationXml ) );

            String applicationXmlContent = FileUtils.fileRead( oldApplicationXml );

            StringUtils.replace( applicationXmlContent, "<role>org.codehaus.plexus.jdo.JdoFactory</role>",
                "<role>org.codehaus.plexus.jdo.JdoFactory</role>\n<role-hint>"+ version.getName() + "</role-hint>");

            FileUtils.fileWrite( oldApplicationXml.getAbsolutePath(), applicationXmlContent );

            //Load old application.xml
            addConfiguration( oldApplicationXml );

            File newApplicationXml = updater.getNewApplicationXml( continuumHome );

            FileUtils.copyFile( newApplicationXml, getApplicationXmlBackup( newApplicationXml ) );

            applicationXmlContent = FileUtils.fileRead( newApplicationXml );

            StringUtils.replace( applicationXmlContent, "<role>org.codehaus.plexus.jdo.JdoFactory</role>",
                "<role>org.codehaus.plexus.jdo.JdoFactory</role>\n<role-hint>"+ version.getName() + "-new</role-hint>");

            FileUtils.fileWrite( newApplicationXml.getAbsolutePath(), applicationXmlContent );

            //Load new application.xml
            addConfiguration( newApplicationXml );

            try
            {
                //load jdo factories
                JdoFactory oldFactory = (JdoFactory) container.lookup( JdoFactory.ROLE, version.getName() );

                JdoFactory newFactory = (JdoFactory) container.lookup( JdoFactory.ROLE, version.getName() + "-new" );

                updater.updateDatabase( oldFactory, newFactory );
            }
            finally
            {
                FileUtils.copyFile( getApplicationXmlBackup( oldApplicationXml ), oldApplicationXml );

                getApplicationXmlBackup( oldApplicationXml ).delete();

                FileUtils.copyFile( getApplicationXmlBackup( newApplicationXml ), newApplicationXml );

                getApplicationXmlBackup( newApplicationXml ).delete();
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();

            restore( continuumHome, userVersion );

            System.exit( 1 );
        }
    }

    private void backup( File continuumHome, String version )
        throws UpdaterException
    {
        File backupDir = getBackupDirectory( continuumHome, version );

        getLogger().info( "==> Backup Continuum " + version + " to " + backupDir.getAbsolutePath() );

        try
        {
            FileUtils.deleteDirectory( backupDir);

            FileUtils.copyDirectoryStructure( continuumHome, backupDir);
        }
        catch( IOException e )
        {
            throw new UpdaterException( "Can't create a continuum backup.", e );
        }
    }

    private void restore( File continuumHome, String userVersion )
        throws UpdaterException
    {
        getLogger().info( "==> Restore Continuum " + userVersion );

        try
        {
            FileUtils.cleanDirectory( continuumHome );

            FileUtils.copyDirectoryStructure( getBackupDirectory( continuumHome, userVersion ),
                                              continuumHome );
        }
        catch( IOException e )
        {
            throw new UpdaterException( "Can't restore continuum.", e );
        }
    }

    private File getBackupDirectory( File continuumHome, String version )
    {
        return new File( continuumHome.getParentFile(), "continuum-" + version );
    }

    private File getApplicationXmlBackup( File applicationXml )
    {
        return new File( applicationXml.getParentFile(), "application.xml.backup" );
    }

    private void updateContinuumFiles( File continuumHome, UpdaterModel model )
        throws UpdaterException
    {
        File newContinuumFile = downloadContinuum( model, continuumHome );

        getLogger().info( "==> Update Continuum files." );

        try
        {
            FileUtils.cleanDirectory( continuumHome );

            // Extract new continuum version
            UnArchiver unArchiver = (UnArchiver) container.lookup( UnArchiver.ROLE,
                                                                   FileUtils.extension( newContinuumFile.getName() ) );

            unArchiver.setSourceFile( newContinuumFile );

            File destDir = new File( newContinuumFile.getParentFile(),
                                    FileUtils.basename( newContinuumFile.getName() ) + "tmp" );

            destDir.mkdirs();

            unArchiver.setDestDirectory( destDir );

            unArchiver.extract();

            newContinuumFile.delete();

            String[] listDirs = destDir.list();

            if ( listDirs.length != 1 && !new File( destDir, listDirs[0] ).isDirectory() )
            {
                throw new UpdaterException( "This archive is in a wrong format." );
            }

            FileUtils.copyDirectoryStructure( new File(  destDir, listDirs[0] ), continuumHome );

            FileUtils.deleteDirectory( destDir );

            // Extract continuum app
            File appsDir = new File( continuumHome, "apps" );

            listDirs = appsDir.list();

            if ( listDirs.length != 1 && new File( appsDir, listDirs[0] ).isDirectory()
                 && !"jar".equals( FileUtils.extension( listDirs[0] ) ) )
            {
                throw new UpdaterException( "Don't have a jar in apps directory." );
            }

            File appsJar = new File( appsDir, listDirs[0] );

            unArchiver.setSourceFile( appsJar );

            File continuumAppDir = new File( appsDir, "continuum" );

            continuumAppDir.mkdirs();

            unArchiver.setDestDirectory( continuumAppDir );

            unArchiver.extract();
        }
        catch ( Exception e )
        {
            throw new UpdaterException( "Can't update continuum files.", e );
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    private void addConfiguration( File config )
        throws Exception
    {
        PlexusConfiguration appConfig = PlexusTools.buildConfiguration( config.getPath(), new FileReader( config ) );

        //TODO: Add configuration to the container
    }
}
