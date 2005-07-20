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
import org.apache.maven.continuum.project.ContinuumBuildGroup;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumSchedule;

import java.util.Collection;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class NewModelTest
    extends AbstractContinuumTest
{
    public void testAddingBuildGroupToProjectAndUpdatingProject()
        throws Exception
    {
        String projectId = addMavenTwoProject( getStore(), "Project Scheduling", "scm:scheduling" );

        ContinuumProject project = getStore().getProject( projectId );

        project.addBuildGroup( createStubBuildGroup( "Plexus", "Description" ) );

        getStore().updateProject( project );

        assertEquals( 1, getStore().getBuildGroups().size() );

        project = getStore().getProject( projectId );

        assertEquals( 1, project.getBuildGroups().size() );
    }

    public void testProjectAdditionAndRemovalFromBuildGroup()
        throws Exception
    {
        // create buildGroup
        ContinuumBuildGroup buildGroup = createStubBuildGroup( "buildGroup1", "buildGroup1" );

        String buildGroupId = getStore().addBuildGroup( buildGroup );

        buildGroup = getStore().getBuildGroup( buildGroupId );

        String projectId = addMavenTwoProject( getStore(), "project1", "scm:scheduling" );

        ContinuumProject project = getStore().getProject( projectId );

        // add project
        buildGroup.addProject( project );

        // update buildGroup
        getStore().updateBuildGroup( buildGroup );

        // retrieve buildGroup
        buildGroup = getStore().getBuildGroup( buildGroupId );

        assertNotNull( buildGroup );

        // get projects out of the buildGroup
        Set projects = buildGroup.getProjects();

        assertEquals( 1, projects.size() );

        // get individual project
        project = (ContinuumProject) buildGroup.getProjects().iterator().next();

        // test values within project
        assertEquals( "project1", project.getName() );

        // ----------------------------------------------------------------------
        // Now lookup the project on its own and make sure the buildGroup is
        // present within the project.
        // ----------------------------------------------------------------------

        project = getStore().getProject( projectId );

        assertNotNull( project );

        buildGroup = (ContinuumBuildGroup) project.getBuildGroups().iterator().next();

        assertEquals( "buildGroup1", buildGroup.getName() );

        // ----------------------------------------------------------------------
        // Now delete the buildGroup from the getStore() and make sure that the project
        // still remains in the getStore().
        // ----------------------------------------------------------------------

        buildGroup = getStore().getBuildGroup( buildGroupId );

        getStore().removeBuildGroup( buildGroup.getId() );

        project = getStore().getProject( projectId );

        assertNotNull( project );
    }


    public void xtestBasic()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Create the missing group
        // ----------------------------------------------------------------------

        ContinuumProjectGroup plexusGroup = new ContinuumProjectGroup();

        plexusGroup.setGroupId( "org.codehaus.plexus" );

        plexusGroup.setName( "Plexus" );

        String plexusGroupId = getStore().addProjectGroup( plexusGroup );

        plexusGroup = getStore().getProjectGroup( plexusGroupId );

        // ----------------------------------------------------------------------
        // Add projects from URL metadata
        // ----------------------------------------------------------------------

        ContinuumProject componentA = makeStubMavenTwoProject( "component a", "a" );

        componentA.setProjectGroup( plexusGroup );

        String projectIdA = addMavenTwoProject( getStore(),
                                                componentA );

        ContinuumProject componentB = makeStubMavenTwoProject( "component b", "b" );

        componentB.setProjectGroup( plexusGroup );

        String projectIdB = addMavenTwoProject( getStore(),
                                                componentB );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        plexusGroup = getStore().getProjectGroup( plexusGroupId );

        assertNotNull( plexusGroup.getProjects() );

        assertEquals( 2, plexusGroup.getProjects().size() );

        ContinuumProject plexusComponentA = getStore().getProject( projectIdA );

        ContinuumProject plexusComponentB = getStore().getProject( projectIdB );

        assertNotNull( "componentA.projectGroup == null", plexusComponentA.getProjectGroup() );

        assertNotNull( "componentB.projectGroup == null", plexusComponentB.getProjectGroup() );

        assertEquals( "projectGroup.id != componentA.projectGroup.id", plexusGroup.getId(), plexusComponentA.getProjectGroup().getId() );

        assertEquals( "projectGroup.id != componentA.projectGroup.id", plexusGroup.getId(), plexusComponentB.getProjectGroup().getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuildGroup buildGroup = new ContinuumBuildGroup();

        buildGroup.setName( "Plexus Hey Yo" );

        buildGroup.setDescription( "Description" );

        //String buildGroupId = getStore().addBuildGroup( buildGroup );

        //buildGroup = getStore().getBuildGroup( buildGroupId );

        System.err.println( "buildGroup.id: " + buildGroup.getId() );

        // ----------------------------------------------------------------------
        // Add component A to the build group
        // ----------------------------------------------------------------------

//        componentA.getBuildGroups().add( buildGroup );

        componentA.addBuildGroup( buildGroup );

        getStore().updateProject( componentA );

        System.out.println( "componentA.getBuildGroups().size() = " + componentA.getBuildGroups().size() );

        buildGroup = (ContinuumBuildGroup) componentA.getBuildGroups().iterator().next();

        String buildGroupId = buildGroup.getId();

        assertNotNull( buildGroup );

        //String buildGroupId = getStore().getBuildGroup;


        // ----------------------------------------------------------------------
        // Assert that the project has a build group
        // ----------------------------------------------------------------------

        componentA = getStore().getProject( componentA.getId() );

        assertNotNull( componentA.getBuildGroups() );

        assertEquals( 1, componentA.getBuildGroups().size() );

        // ----------------------------------------------------------------------
        // Assert that the build group has a project
        // ----------------------------------------------------------------------

//        buildGroup = (ContinuumBuildGroup) componentA.getBuildGroups().iterator().next();
//
//        assertEquals( "buildGroupId", buildGroupId, buildGroup.getId() );

        buildGroup = getStore().getBuildGroup( buildGroupId );

        assertEquals( "buildGroupId", buildGroupId, buildGroup.getId() );

        assertEquals( "buildGroup.projects.size", 1, buildGroup.getProjects().size() );

        assertEquals( projectIdA, ( (ContinuumProject) buildGroup.getProjects().iterator().next() ).getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        getStore().removeProject( componentA.getId() );

        getStore().removeProject( componentB.getId() );

        Collection projects = getStore().getAllProjects();

        assertEquals( "projects.size", 0, projects.size() );

        buildGroup = getStore().getBuildGroup( buildGroupId );

        assertNotNull( buildGroup );

        assertEquals( 0, buildGroup.getProjects().size() );

        plexusGroup = getStore().getProjectGroup( plexusGroupId );

        assertNotNull( plexusGroup );

//        assertEquals( 0, plexusGroup.getProjects().size() );
    }

    public ContinuumBuildGroup createStubBuildGroup( String name, String description )
    {
        ContinuumBuildGroup buildGroup = new ContinuumBuildGroup();

        buildGroup.setName( name );

        buildGroup.setDescription( description );

        return buildGroup;
    }
}
