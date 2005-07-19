package org.apache.maven.continuum.configuration;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ConfigurationStoringException
    extends Exception
{
    public ConfigurationStoringException( String message )
    {
        super( message );
    }

    public ConfigurationStoringException( Throwable cause )
    {
        super( cause );
    }

    public ConfigurationStoringException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
