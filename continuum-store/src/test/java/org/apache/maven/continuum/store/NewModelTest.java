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
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.MavenTwoProject;

import java.util.Collection;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class NewModelTest
    extends AbstractContinuumTest
{
    public void testProjectAdditionAndRemovalFromProjectGroup()
        throws Exception
    {
        // create projectGroup
        ContinuumProjectGroup projectGroup = createStubProjectGroup( "projectGroup1", "projectGroup1" );

        projectGroup = getStore().addProjectGroup( projectGroup );

        ContinuumProject project = addMavenTwoProject( getStore(), "project2" );

        // add project
        projectGroup.addProject( project );

        // update projectGroup
        projectGroup = getStore().updateProjectGroup( projectGroup );

        assertNotNull( projectGroup );

        // get projects out of the projectGroup
        Set projects = projectGroup.getProjects();

        assertEquals( 1, projects.size() );

        // get individual project
        project = (ContinuumProject) projectGroup.getProjects().iterator().next();

        // test values within project
        assertEquals( "project2", project.getName() );

        // ----------------------------------------------------------------------
        // Now lookup the project on its own and make sure the projectGroup is
        // present within the project.
        // ----------------------------------------------------------------------

        project = getStore().getProject( project.getId() );

        assertNotNull( project );

        projectGroup = project.getProjectGroup();

        assertEquals( "projectGroup1", projectGroup.getName() );

        // ----------------------------------------------------------------------
        // Now delete the projectGroup from the getStore() and make sure that the project
        // still remains in the getStore().
        // ----------------------------------------------------------------------

        projectGroup = getStore().getProjectGroup( projectGroup.getId() );

        getStore().removeProjectGroup( projectGroup.getId() );

        project = getStore().getProject( project.getId() );

        assertNotNull( project );
    }

    public void DISABLEDtestBuildSettingsAdditionAndRemovalFromProjectGroup()
        throws Exception
    {
        // create buildSettings
        ContinuumBuildSettings buildSettings = createStubBuildSettings( "buildSettings1", "1.3" );

        buildSettings = getStore().addBuildSettings( buildSettings );

        // create projectGroup
        ContinuumProjectGroup projectGroup = createStubProjectGroup( "projectGroup1", "projectGroup1" );

        projectGroup = getStore().addProjectGroup( projectGroup );

        // add build group
        buildSettings.addProjectGroup( projectGroup );

        // update buildSettings
        buildSettings = getStore().updateBuildSettings( buildSettings );

        assertNotNull( buildSettings );

        // get projectGroups out of the buildSettings
        Set projectGroups = buildSettings.getProjectGroups();

        assertEquals( 1, projectGroups.size() );

        // get individual build group
        projectGroup = (ContinuumProjectGroup) buildSettings.getProjectGroups().iterator().next();

        // test values within the build group
        assertEquals( "projectGroup1", projectGroup.getName() );

        // ----------------------------------------------------------------------
        // Now lookup the build group on its own and make sure the build settings are
        // present within the build build gropu.
        // ----------------------------------------------------------------------

        projectGroup = getStore().getProjectGroup( projectGroup.getId() );

        assertNotNull( projectGroup );

        buildSettings = (ContinuumBuildSettings) projectGroup.getBuildSettings().iterator().next();

        assertEquals( "buildSettings1", buildSettings.getName() );

        // ----------------------------------------------------------------------
        // Now delete the buildSettings from the getStore() and make sure that the build group
        // still remains in the getStore().
        // ----------------------------------------------------------------------

        buildSettings = getStore().getBuildSettings( buildSettings.getId() );

        getStore().removeBuildSettings( buildSettings.getId() );

        projectGroup = getStore().getProjectGroup( projectGroup.getId() );

        assertNotNull( projectGroup );
    }

    // ----------------------------------------------------------------------
    // We're not exactly sure why this beasty doesn't work.
    // ----------------------------------------------------------------------

    public void DISABLEDtestBasic()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Create the missing group
        // ----------------------------------------------------------------------

        ContinuumProjectGroup plexusGroup = new ContinuumProjectGroup();

        plexusGroup.setGroupId( "org.codehaus.plexus" );

        plexusGroup.setName( "Plexus" );

        plexusGroup = getStore().addProjectGroup( plexusGroup );

        // ----------------------------------------------------------------------
        // Add projects from URL metadata
        // ----------------------------------------------------------------------

        MavenTwoProject componentA = makeStubMavenTwoProject( "component a" );

        componentA.setProjectGroup( plexusGroup );

        componentA = addMavenTwoProject( getStore(), componentA );

        MavenTwoProject componentB = makeStubMavenTwoProject( "component b" );

        componentB.setProjectGroup( plexusGroup );

        componentB = addMavenTwoProject( getStore(), componentB );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        assertNotNull( plexusGroup.getProjects() );

        assertEquals( 2, plexusGroup.getProjects().size() );

        componentA = (MavenTwoProject) getStore().getProject( componentA.getId() );

        componentB = (MavenTwoProject) getStore().getProject( componentB.getId() );

        assertNotNull( "componentA.projectGroup == null", componentA.getProjectGroup() );

        assertNotNull( "componentB.projectGroup == null", componentB.getProjectGroup() );

        assertEquals( "projectGroup.id != componentA.projectGroup.id", plexusGroup.getId(), componentA.getProjectGroup().getId() );

        assertEquals( "projectGroup.id != componentA.projectGroup.id", plexusGroup.getId(), componentB.getProjectGroup().getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        getStore().removeProject( componentA.getId() );

        getStore().removeProject( componentB.getId() );

        Collection projects = getStore().getAllProjects();

        assertEquals( "projects.size", 0, projects.size() );

        plexusGroup = getStore().getProjectGroup( plexusGroup.getId() );

        assertNotNull( plexusGroup );

//        assertEquals( 0, plexusGroup.getProjects().size() );
    }
}
