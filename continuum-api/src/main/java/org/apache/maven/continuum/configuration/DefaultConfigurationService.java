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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Xpp3Dom configuration;

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

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setInitialized( boolean initialized )
    {
        this.initialized = initialized;
    }

    public boolean isInitialized()
    {
        return initialized;
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

    // ----------------------------------------------------------------------
    // Process configuration to glean application specific values
    // ----------------------------------------------------------------------

    protected void processInboundConfiguration()
        throws ConfigurationLoadingException
    {
        Xpp3Dom initializedDom = configuration.getChild( CONFIGURATION_INITIALIZED );

        if ( initializedDom != null )
        {
            String booleanString = initializedDom.getValue();

            initialized = "true".equals( booleanString ) || "1".equals( booleanString );
        }

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

        configuration.addChild( createDom( CONFIGURATION_INITIALIZED, Boolean.toString( initialized ) ) );

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
            path = path.substring( applicationHome.getAbsolutePath().length() );
        }

        return createDom( elementName, path );
    }

    // ----------------------------------------------------------------------
    // Load and Store
    // ----------------------------------------------------------------------

    public void load()
        throws ConfigurationLoadingException
    {
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
            throw new ConfigurationStoringException( "Error reading configuration '" + source + "'.", e );
        }
    }
}
