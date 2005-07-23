package org.apache.maven.continuum.build.settings;

import org.apache.maven.continuum.Continuum;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public interface BuildSettingsActivator
{
    String ROLE = BuildSettingsActivator.class.getName();

    /**
     * Grab all the stored {@link org.apache.maven.continuum.project.ContinuumBuildSettings} objects
     * and activate them by looking at the scheduling information contained within and submitting a
     * Job to the scheduler.
     *
     * @throws BuildSettingsActivationException
     */
    void activateBuildSettings( Continuum continuum )
        throws BuildSettingsActivationException;
}
