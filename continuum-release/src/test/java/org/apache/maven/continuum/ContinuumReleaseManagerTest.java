package org.apache.maven.continuum;

import org.codehaus.plexus.PlexusTestCase;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.plugins.release.model.Release;

/**
 * @author Jason van Zyl
 */
public class ContinuumReleaseManagerTest
    extends PlexusTestCase
{
    public void testContinuumReleaseManager()
        throws Exception
    {
        ContinuumReleaseManager crm = (ContinuumReleaseManager) lookup( ContinuumReleaseManager.ROLE, "default" );

        Release r = new Release();

        crm.release( r );
    }
}
