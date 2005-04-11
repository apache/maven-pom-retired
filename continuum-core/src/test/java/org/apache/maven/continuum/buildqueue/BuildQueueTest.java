package org.apache.maven.continuum.buildqueue;

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

import java.util.Properties;

import org.apache.maven.continuum.store.ContinuumStore;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: BuildQueueTest.java,v 1.1.1.1 2005/03/29 20:42:04 trygvis Exp $
 */
public class BuildQueueTest
    extends PlexusTestCase
{
    private BuildQueue buildQueue;

    private ContinuumStore store;

    public void setUp()
        throws Exception
    {
        super.setUp();

        buildQueue = (BuildQueue) lookup( BuildQueue.ROLE );

        store = (ContinuumStore) lookup( ContinuumStore.ROLE );
    }

    public void testTestTheQueueWithASingleProject()
        throws Exception
    {
        String name = "Project 1";

        String project = createProject( name );

        String build = enqueue( project );

        assertNextBuildIs( build );

        assertNextBuildIsNull();

        String buildX = enqueue( project );

        enqueue( project );
        enqueue( project );
        enqueue( project );
        enqueue( project );

        assertNextBuildIs( buildX );

        assertNextBuildIsNull();
    }

    public void testTheQueueWithMultipleProjects()
        throws Exception
    {
        String name1 = "Project 1";

        String name2 = "Project 2";

        String project1 = createProject( name1 );

        String project2 = createProject( name2 );

        String build1 = enqueue( project1 );

        String build2 = enqueue( project2 );

        assertNextBuildIs( build1 );

        assertNextBuildIs( build2 );

        assertNextBuildIsNull();

        String buildX1 = enqueue( project1 );

        String buildX2 = enqueue( project2 );

        enqueue( project1 );
        enqueue( project2 );
        enqueue( project1 );
        enqueue( project2 );
        enqueue( project1 );
        enqueue( project2 );
        enqueue( project1 );
        enqueue( project2 );

        assertNextBuildIs( buildX1 );

        assertNextBuildIs( buildX2 );

        assertNextBuildIsNull();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String createProject( String name )
        throws Exception
    {
        String scmUrl = "scm:local:src/test/projects/project-1";
        String nagEmailAddress = "foo@bar";
        String version = "1.0";
        String builderId = "test";
        String workingDirectory = getTestPath( "target/checkouts" );
        Properties properties = new Properties();

        return store.addProject( name, scmUrl, nagEmailAddress, version, builderId, workingDirectory, properties );
    }

    private String enqueue( String projectId )
        throws Exception
    {
        String buildId = store.createBuild( projectId );

        buildQueue.enqueue( projectId, buildId );

        return buildId;
    }

    private void assertNextBuildIs( String expectedBuildId )
        throws Exception
    {
        String actualBuildId = buildQueue.dequeue();

        assertNotNull( "Got a null build id returned.", actualBuildId );

        assertEquals( "Didn't get the expected build id.", expectedBuildId, actualBuildId );
    }

    private void assertNextBuildIsNull()
        throws Exception
    {
        String actualBuildId = buildQueue.dequeue();

        assertNull( "Got a non-null build id returned: " + actualBuildId, actualBuildId );
    }
}
