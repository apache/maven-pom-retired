package org.apache.maven.continuum.release;

/**
 * @author Jason van Zyl
 */
public class ContinuumReleaseException
    extends Exception
{
    public ContinuumReleaseException( String id )
    {
        super( id );
    }

    public ContinuumReleaseException( String id,
                                      Throwable throwable )
    {
        super( id, throwable );
    }

    public ContinuumReleaseException( Throwable throwable )
    {
        super( throwable );
    }
}
