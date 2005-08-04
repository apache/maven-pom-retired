package org.apache.maven.continuum.configuration;

import org.apache.maven.continuum.profile.ContinuumJdk;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setInitialized( boolean initialized )
    {
        getLogger().info( "Setting the initialization state to " + initialized );

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

    public void setInMemoryMode( boolean inMemoryMode )
    {
        this.inMemoryMode = inMemoryMode;
    }

    public boolean inMemoryMode()
    {
        return inMemoryMode;
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

            initialized = booleanString.equals( "true" ) || booleanString.equals( "1" );
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
