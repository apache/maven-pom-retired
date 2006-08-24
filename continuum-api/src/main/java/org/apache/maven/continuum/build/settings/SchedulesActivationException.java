package org.apache.maven.continuum.build.settings;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class SchedulesActivationException
    extends Exception
{
    public SchedulesActivationException( String message )
    {
        super( message );
    }

    public SchedulesActivationException( Throwable cause )
    {
        super( cause );
    }

    public SchedulesActivationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
