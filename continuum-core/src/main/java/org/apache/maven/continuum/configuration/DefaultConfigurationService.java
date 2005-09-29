package org.apache.maven.continuum.configuration;

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

import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class DefaultConfigurationService
    extends AbstractLogEnabled
    implements ConfigurationService
{
    /**
     * @plexus.configuration
     */
    private File source;

    /**
     * @plexus.configuration
     */
    private File applicationHome;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Xpp3Dom configuration;

    private SystemConfiguration systemConf;

    // ----------------------------------------------------------------------
    // Continuum specifics we'll refactor out later
    // ----------------------------------------------------------------------

    private boolean inMemoryMode;

    private boolean initialized;

    private String url;

    private File buildOutputDirectory;

    private File workingDirectory;

    private Map jdks;

    private String companyLogoUrl;

    private String companyName;

    private String companyUrl;

    private boolean guestAccountEnabled;

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setInitialized( boolean initialized )
    {
        systemConf.setInitialized( initialized );
    }

    public boolean isInitialized()
    {
        return systemConf.isInitialized();
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public File getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public void setJdks( Map jdks )
    {
        this.jdks = jdks;
    }

    public void setInMemoryMode( boolean inMemoryMode )
    {
        this.inMemoryMode = inMemoryMode;
    }

    public boolean inMemoryMode()
    {
        return inMemoryMode;
    }

    public String getCompanyLogo()
    {
        return companyLogoUrl;
    }

    public void setCompanyLogo( String companyLogoUrl )
    {
        this.companyLogoUrl = companyLogoUrl;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName( String companyName )
    {
        this.companyName = companyName;
    }

    public String getCompanyUrl()
    {
        return companyUrl;
    }

    public void setCompanyUrl( String companyUrl )
    {
        this.companyUrl = companyUrl;
    }

    public boolean isGuestAccountEnabled()
    {
        return systemConf.isGuestAccountEnabled();
    }

    public void setGuestAccountEnabled( boolean enabled )
    {
        systemConf.setGuestAccountEnabled( enabled );
    }

    public String getBuildOutput( int buildId, int projectId )
        throws ConfigurationException
    {
        File file = getBuildOutputFile( buildId, projectId );

        try
        {
            return FileUtils.fileRead( file.getAbsolutePath() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Error reading build output for build '" + buildId + "'.", e );

            return null;
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public File getBuildOutputFile( int buildId, int projectId )
        throws ConfigurationException
    {
        File dir = new File( getBuildOutputDirectory(), Integer.toString( projectId ) );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ConfigurationException(
                "Could not make the build output directory: " + "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, buildId + ".log.txt" );
    }

    // ----------------------------------------------------------------------
    // Process configuration to glean application specific values
    // ----------------------------------------------------------------------

    protected void processInboundConfiguration()
        throws ConfigurationLoadingException
    {
        Xpp3Dom urlDom = configuration.getChild( CONFIGURATION_URL );

        if ( urlDom != null )
        {
            url = urlDom.getValue();
        }

        Xpp3Dom buildOutputDirectoryDom = configuration.getChild( CONFIGURATION_BUILD_OUTPUT_DIRECTORY );

        if ( buildOutputDirectoryDom != null )
        {
            buildOutputDirectory = getFile( configuration, CONFIGURATION_BUILD_OUTPUT_DIRECTORY );
        }

        workingDirectory = getFile( configuration, CONFIGURATION_WORKING_DIRECTORY );

        Xpp3Dom companyLogoUrlDom = configuration.getChild( CONFIGURATION_COMPANY_LOGO );

        if ( companyLogoUrlDom != null )
        {
            companyLogoUrl = companyLogoUrlDom.getValue();
        }

        Xpp3Dom companyNameDom = configuration.getChild( CONFIGURATION_COMPANY_NAME );

        if ( companyNameDom != null )
        {
            companyName = companyNameDom.getValue();
        }

        Xpp3Dom companyUrlDom = configuration.getChild( CONFIGURATION_COMPANY_URL );

        if ( companyUrlDom != null )
        {
            companyUrl = companyUrlDom.getValue();
        }
    }

    private File getFile( Xpp3Dom configuration, String elementName )
        throws ConfigurationLoadingException
    {
        Xpp3Dom element = configuration.getChild( elementName );

        if ( element == null )
        {
            throw new ConfigurationLoadingException( "Missing required element '" + elementName + "'." );
        }

        String value = element.getValue();

        if ( StringUtils.isEmpty( value ) )
        {
            throw new ConfigurationLoadingException( "Missing required element '" + elementName + "'." );
        }

        File f = new File( value );

        if ( !f.isAbsolute() )
        {
            f = new File( applicationHome, value );
        }

        return f;
    }

    protected void processOutboundConfiguration()
    {
        configuration = new Xpp3Dom( CONFIGURATION );

        if ( url != null )
        {
            configuration.addChild( createDom( CONFIGURATION_URL, url ) );
        }

        if ( buildOutputDirectory != null )
        {
            configuration.addChild( createFileDom( CONFIGURATION_BUILD_OUTPUT_DIRECTORY, buildOutputDirectory ) );
        }

        if ( workingDirectory != null )
        {
            configuration.addChild( createFileDom( CONFIGURATION_WORKING_DIRECTORY, workingDirectory ) );
        }

        if ( companyLogoUrl != null )
        {
            configuration.addChild( createDom( CONFIGURATION_COMPANY_LOGO, companyLogoUrl ) );
        }

        if ( companyName != null )
        {
            configuration.addChild( createDom( CONFIGURATION_COMPANY_NAME, companyName ) );
        }

        if ( companyUrl != null )
        {
            configuration.addChild( createDom( CONFIGURATION_COMPANY_URL, companyUrl ) );
        }
    }

    protected Xpp3Dom createDom( String elementName, String value )
    {
        Xpp3Dom dom = new Xpp3Dom( elementName );

        dom.setValue( value );

        return dom;
    }

    private Xpp3Dom createFileDom( String elementName, File file )
    {
        String path = file.getAbsolutePath();

        if ( path.startsWith( applicationHome.getAbsolutePath() ) )
        {
            path = path.substring( applicationHome.getAbsolutePath().length() + 1 );
        }

        return createDom( elementName, path );
    }

    // ----------------------------------------------------------------------
    // Load and Store
    // ----------------------------------------------------------------------

    public void load()
        throws ConfigurationLoadingException
    {
        try
        {
            systemConf = store.getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = store.addSystemConfiguration( systemConf );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ConfigurationLoadingException( "Error reading configuration from database.", e );
        }

        if ( inMemoryMode )
        {
            return;
        }

        try
        {
            configuration = Xpp3DomBuilder.build( new FileReader( source ) );
        }
        catch ( FileNotFoundException e )
        {
            throw new ConfigurationLoadingException(
                "Specified location of configuration '" + source + "' doesn't exist." );
        }
        catch ( IOException e )
        {
            throw new ConfigurationLoadingException( "Error reading configuration '" + source + "'.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationLoadingException( "Error parsing configuration '" + source + "'.", e );
        }

        processInboundConfiguration();
    }

    public void store()
        throws ConfigurationStoringException
    {
        try
        {
            store.updateSystemConfiguration( systemConf );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ConfigurationStoringException( "Error writting configuration to database.", e );
        }

        if ( inMemoryMode )
        {
            return;
        }

        processOutboundConfiguration();

        try
        {
            File backup = new File( source.getAbsolutePath() + ".backup" );

            FileUtils.rename( source, backup );

            Writer writer = new FileWriter( source );

            writer.write( "<!-- Written by Continuum on " + new Date() + " -->" + LS );

            Xpp3DomWriter.write( writer, configuration );

            writer.flush();

            writer.close();
        }
        catch ( IOException e )
        {
            throw new ConfigurationStoringException( "Error writting configuration '" + source + "'.", e );
        }
    }
}
