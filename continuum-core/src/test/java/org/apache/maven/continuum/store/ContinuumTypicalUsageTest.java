package org.apache.maven.continuum.store;

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.initialization.ContinuumInitializer;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;

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

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class ContinuumTypicalUsageTest
    extends AbstractContinuumTest
{
    // !! as part of creating the build settings a build job needs to be scheduled
    // and we need to control the activation of the build settings.

    // I can specify the class of the job and have a test job
    // I can set a job listener here
    // figure out when the job is done and make sure the mechanism works
    // then we can figure out how to get projects into the right groups.
    // create a project group for all projects with X groupId

    // ----------------------------------------------------------------------
    // 1. Initialize Continuum
    //    -> create default build settings
    //    -> create default project group
    //    -> default build settings are added to the project group
    //
    // 2. Create a project
    //
    // 3. Add the project to the default project group
    //
    // 4.

    // ----------------------------------------------------------------------

    public void testContinuumTypicalUsage()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Simulate the initial running of the system which will create the
        // default project group and the default build settings.
        // ----------------------------------------------------------------------

        ContinuumInitializer initializer = (ContinuumInitializer) lookup( ContinuumInitializer.ROLE );

        initializer.initialize();

        ContinuumProjectGroup defaultProjectGroup = initializer.getDefaultProjectGroup();

        // ----------------------------------------------------------------------
        // At this point we can now accept new projects into the system
        // ----------------------------------------------------------------------

        ContinuumProject project = makeStubMavenTwoProject( "test1", "scm:url" );

        String projectId = addMavenTwoProject( getStore(), project );

        project = getStore().getProject( projectId );

        // ----------------------------------------------------------------------
        // Now that we have a project we want to add it to the default project group
        // ----------------------------------------------------------------------

        defaultProjectGroup.addProject( project );

        getStore().updateProjectGroup( defaultProjectGroup );

        assertEquals( DefaultContinuumInitializer.DEFAULT_PROJECT_GROUP_NAME,   project.getProjectGroup().getName() );

        assertEquals( DefaultContinuumInitializer.DEFAULT_PROJECT_GROUP_DESCRIPTION,   project.getProjectGroup().getDescription() );

        assertEquals( DefaultContinuumInitializer.DEFAULT_PROJECT_GROUP_ID,   project.getProjectGroup().getGroupId() );
    }
}
