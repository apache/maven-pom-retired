package org.apache.maven.continuum.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
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

    private Xpp3Dom configuration;

    // ----------------------------------------------------------------------
    // Continuum specifics we'll refactor out later
    // ----------------------------------------------------------------------

    private String url;

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    private File buildOutputDirectory;

    public File getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( File buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    // ----------------------------------------------------------------------
    // Process configuration to glean application specific values
    // ----------------------------------------------------------------------

    protected void processInboundConfiguration()
        throws ConfigurationLoadingException
    {
        url = configuration.getChild( CONFIGURATION_URL ).getValue();

        buildOutputDirectory = getFile( configuration, CONFIGURATION_BUILD_OUTPUT_DIRECTORY );
    }

    private File getFile( Xpp3Dom configuration, String elementName )
        throws ConfigurationLoadingException
    {
        String value = configuration.getChild( elementName ).getValue();

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
        configuration = new Xpp3Dom( "configuration" );

        configuration.addChild( createDom( CONFIGURATION_URL, url ) );

        configuration.addChild( createFileDom( CONFIGURATION_BUILD_OUTPUT_DIRECTORY, buildOutputDirectory ) );
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
        try
        {
            configuration = Xpp3DomBuilder.build( new FileReader( source ) );
        }
        catch ( FileNotFoundException e )
        {
            throw new ConfigurationLoadingException( "Specified location of configuration '" + source + "' doesn't exist." );
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
        processOutboundConfiguration();

        try
        {
            File backup = new File( source.getName() + ".backup" );

            FileUtils.rename( source, backup );

            Writer writer = new FileWriter( source );

            Xpp3DomWriter.write( writer, configuration );
        }
        catch ( IOException e )
        {
            throw new ConfigurationStoringException( "Error reading configuration '" + source + "'.", e );
        }
    }
}
