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
        ContinuumProjectGroup projectGroup = AbstractContinuumTest.getDefaultProjectGroup( getStore() );

        int projectGroupProjectCount = projectGroup.getProjects().size();

        int projectCount = getStore().getAllProjects().size();

        int projectGroupCount = getStore().getProjectGroups().size();

        // ----------------------------------------------------------------------
        // At this point we can now accept new projects into the system
        // ----------------------------------------------------------------------

        MavenTwoProject project1 = makeStubMavenTwoProject( "Typical Project 1" );

        project1.setProjectGroup( projectGroup );

        project1 = addMavenTwoProject( getStore(), project1 );

        assertNotNull( project1.getProjectGroup() );

        assertEquals( projectGroup.getName(), project1.getProjectGroup().getName() );

        assertEquals( projectGroup.getDescription(), project1.getProjectGroup().getDescription() );

        assertEquals( projectGroup.getGroupId(), project1.getProjectGroup().getGroupId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MavenTwoProject project2 = makeStubMavenTwoProject( "Typical Project 2" );

        project2.setProjectGroup( projectGroup );

        project2 = addMavenTwoProject( getStore(), project2 );

        assertNotNull( project2.getProjectGroup() );

        assertEquals( projectGroup.getName(), project2.getProjectGroup().getName() );

        assertEquals( projectGroup.getDescription(), project2.getProjectGroup().getDescription() );

        assertEquals( projectGroup.getGroupId(), project2.getProjectGroup().getGroupId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        projectGroup = getDefaultProjectGroup( getStore() );

        assertEquals( projectGroupProjectCount + 2, projectGroup.getProjects().size() );

        assertEquals( projectCount + 2, getStore().getAllProjects().size() );

        assertEquals( projectGroupCount, getStore().getProjectGroups().size() );
    }
}
