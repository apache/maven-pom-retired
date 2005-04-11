package org.apache.maven.continuum;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.store.ContinuumStore;

import junit.framework.Assert;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: TestUtils.java,v 1.2 2005/04/04 15:24:03 trygvis Exp $
 */
public class TestUtils
{
    private static int buildTimeout = 30 * 1000;

    // ----------------------------------------------------------------------
    // Wait for build
    // ----------------------------------------------------------------------

    public final static ContinuumBuild waitForSuccessfulBuild( ContinuumStore continuumStore, String buildId )
        throws Exception
    {
        ContinuumBuild build = waitForBuild( continuumStore, buildId );

        Assert.assertEquals( ContinuumProjectState.OK, build.getState() );

        return build;
    }

    public final static ContinuumBuild waitForFailedBuild( ContinuumStore continuumStore, String buildId )
        throws Exception
    {
        ContinuumBuild build = waitForBuild( continuumStore, buildId );

        Assert.assertEquals( ContinuumProjectState.FAILED, build.getState() );

        return build;
    }

    public final static ContinuumBuild waitForBuild( ContinuumStore continuumStore, String buildId )
        throws Exception
    {
        int time = buildTimeout;

        int interval = 100;

        ContinuumBuild result;

        while ( time > 0 )
        {
            Thread.sleep( interval );

            time -= interval;

            result = continuumStore.getBuild( buildId );

            Assert.assertNotNull( result );

            if ( result.getState() == ContinuumProjectState.BUILD_SIGNALED )
            {
                continue;
            }

            if ( result.getState() != ContinuumProjectState.BUILDING )
            {
                return result;
            }
        }

        Assert.assertTrue( "Timeout while waiting for build. Build id: " + buildId, time > 0 );

        return null; // will never happen
    }

    public static void setBuildTimeout( int buildTimeout )
    {
        TestUtils.buildTimeout = buildTimeout;
    }
}
