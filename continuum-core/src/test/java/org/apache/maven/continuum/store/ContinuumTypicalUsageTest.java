package org.apache.maven.continuum.store;

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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.initialization.DefaultContinuumInitializer;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.MavenTwoProject;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
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
        Continuum continuum = (Continuum) lookup( Continuum.ROLE );

        ContinuumProjectGroup defaultProjectGroup = AbstractContinuumTest.getDefaultProjectGroup( getStore() );
            //continuum.getDefaultProjectGroup();

        // ----------------------------------------------------------------------
        // At this point we can now accept new projects into the system
        // ----------------------------------------------------------------------

        MavenTwoProject project = makeStubMavenTwoProject( "test1" );

        project = addMavenTwoProject( getStore(), project );

        // ----------------------------------------------------------------------
        // Now that we have a project we want to add it to the default project group
        // ----------------------------------------------------------------------

        defaultProjectGroup.addProject( project );

        getStore().updateProjectGroup( defaultProjectGroup );

        assertEquals( defaultProjectGroup.getName(), project.getProjectGroup().getName() );

        assertEquals( defaultProjectGroup.getDescription(), project.getProjectGroup().getDescription() );

        assertEquals( defaultProjectGroup.getGroupId(), project.getProjectGroup().getGroupId() );
    }
}
