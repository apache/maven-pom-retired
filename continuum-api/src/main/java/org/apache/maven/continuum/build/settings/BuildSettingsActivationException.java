package org.apache.maven.continuum.build.settings;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class BuildSettingsActivationException
    extends Exception
{
    public BuildSettingsActivationException( String message )
    {
        super( message );
    }

    public BuildSettingsActivationException( Throwable cause )
    {
        super( cause );
    }

    public BuildSettingsActivationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
