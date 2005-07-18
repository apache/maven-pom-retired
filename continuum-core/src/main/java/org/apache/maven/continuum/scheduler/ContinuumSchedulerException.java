package org.apache.maven.continuum.scheduler;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class ContinuumSchedulerException
    extends Exception
{
    public ContinuumSchedulerException( String message )
    {
        super( message );
    }

    public ContinuumSchedulerException( Throwable cause )
    {
        super( cause );
    }

    public ContinuumSchedulerException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
