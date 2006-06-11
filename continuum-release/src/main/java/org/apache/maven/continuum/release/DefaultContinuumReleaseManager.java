package org.apache.maven.continuum.release;

import org.apache.maven.plugins.release.model.Release;
import org.apache.maven.plugins.release.ReleaseManager;

/**
 * @author Jason van Zyl
 */
public class DefaultContinuumReleaseManager
    implements ContinuumReleaseManager
{
    private ReleaseManager releaseManager;

    public void release( Release release )
        throws ContinuumReleaseException
    {
        //TODO:JW The release manager should be taught to use the release descriptor for the release perform.
        //releaseManager.perform( );
    }
}
