package org.apache.maven.continuum.configuration;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.apache.maven.continuum.profile.ContinuumJdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;

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

    private String url;

    private File buildOutputDirectory;

    private Map jdks;

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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

    public Map getJdks()
    {
        return jdks;
    }

    public void addJdk( ContinuumJdk jdk )
    {
        if ( jdks == null )
        {
            jdks = new TreeMap();
        }

        jdks.put( jdk.getVersion(), jdk );
    }

    public void setJdks( Map jdks )
    {
        this.jdks = jdks;
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

        Xpp3Dom jdksElement = configuration.getChild( CONFIGURATION_JDKS );

        if ( jdksElement != null )
        {
            jdks = new TreeMap();

            Xpp3Dom[] jdkElements = jdksElement.getChildren( CONFIGURATION_JDK );

            for ( int i = 0; i < jdkElements.length; i++ )
            {
                Xpp3Dom jdkElement = jdkElements[i];

                String version = jdkElement.getChild( CONFIGURATION_JDK_VERSION ).getValue();

                String home = jdkElement.getChild( CONFIGURATION_JDK_HOME ).getValue();

                if ( version != null & home != null )
                {
                    ContinuumJdk jdk = new ContinuumJdk();

                    jdk.setVersion( version );

                    jdk.setHome( home );

                    jdks.put( version, jdk );
                }
            }
        }
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
        configuration = new Xpp3Dom( CONFIGURATION );

        if ( url != null )
        {
            configuration.addChild( createDom( CONFIGURATION_URL, url ) );
        }

        if ( buildOutputDirectory != null )
        {
            configuration.addChild( createFileDom( CONFIGURATION_BUILD_OUTPUT_DIRECTORY, buildOutputDirectory ) );
        }

        if ( jdks != null )
        {
            Xpp3Dom jdksDom = new Xpp3Dom( CONFIGURATION_JDKS );

            for ( Iterator i = jdks.keySet().iterator(); i.hasNext(); )
            {
                String version = (String) i.next();

                ContinuumJdk jdk = (ContinuumJdk) jdks.get( version );

                Xpp3Dom jdkDom = new Xpp3Dom( CONFIGURATION_JDK );

                jdkDom.addChild( createDom( CONFIGURATION_JDK_VERSION, version ) );

                jdkDom.addChild( createDom( CONFIGURATION_JDK_HOME, jdk.getHome() ) );

                jdksDom.addChild( jdkDom );
            }

            configuration.addChild( jdksDom );
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
