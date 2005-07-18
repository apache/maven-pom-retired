package org.apache.maven.continuum.configuration;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class ConfigurationLoadingException
    extends Exception
{
    public ConfigurationLoadingException( String message )
    {
        super( message );
    }

    public ConfigurationLoadingException( Throwable cause )
    {
        super( cause );
    }

    public ConfigurationLoadingException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
