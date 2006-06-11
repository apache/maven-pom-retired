package org.apache.maven.continuum.release;

import org.apache.maven.plugins.release.model.Release;

/**
 * The Continuum Release Manager is responsible for performing releases based on a release descriptor
 * that has been received by the Maven Release Plugin.
 *
 * @author Jason van Zyl
 */
//TODO:JW You can probably test this in isolation and then we can add methods to the main Continuum API for
//        releasing. The Core Continuum component would then have a dependency on this component and just delegate
//        to this component for release management.
public interface ContinuumReleaseManager
{
    String ROLE = ContinuumReleaseManager.class.getName();

    /**
     * Perform a release based on a release descriptor received by the Maven Release Plugin.
     *
     * @param release
     * @throws ContinuumReleaseException
     */
    void release( Release release )
        throws ContinuumReleaseException;
}
