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
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumBuildGroup;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class NewModelTest
    extends AbstractContinuumTest
{
    public void testBasic()
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

        assertNotNull( plexusComponentA.getProjectGroup() );

        assertNotNull( plexusComponentB.getProjectGroup() );

        assertEquals( plexusGroup.getId(), plexusComponentA.getProjectGroup().getId() );

        assertEquals( plexusGroup.getId(), plexusComponentB.getProjectGroup().getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ContinuumBuildGroup buildGroup = new ContinuumBuildGroup();

        buildGroup.setName( "Plexus Hey Yo");

        String buildGroupId = getStore().addBuildGroup( buildGroup );

        buildGroup = getStore().getBuildGroup( buildGroupId );

        componentA.getBuildGroups().add( buildGroup );

        getStore().updateProject( componentA );

        componentA = getStore().getProject( componentA.getId() );

        assertNotNull( componentA.getBuildGroups() );

        assertEquals( 1, componentA.getBuildGroups().size() );

        assertEquals( buildGroupId, ((ContinuumBuildGroup) componentA.getBuildGroups().iterator().next()).getId() );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        getStore().removeProject( componentA.getId() );

        getStore().removeProject( componentB.getId() );

        buildGroup = getStore().getBuildGroup( buildGroupId );

        assertNotNull( buildGroup );

        assertEquals( 0, buildGroup.getProjects().size() );

        plexusGroup = getStore().getProjectGroup( plexusGroupId );

        assertNotNull( plexusGroup );

//        assertEquals( 0, plexusGroup.getProjects().size() );
    }
}
