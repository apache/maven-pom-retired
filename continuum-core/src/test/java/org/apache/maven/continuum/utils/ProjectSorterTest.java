package org.apache.maven.continuum.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jmcconnell@apache.org">Jesse McConnell</a>
 * @version $Id:$
 */
public class ProjectSorterTest
    extends TestCase
{

    /**
     * test basic three project tree (really a line in this case)
     *
     * @throws Exception
     */
    public void testBasicNestedProjectStructure()
        throws Exception
    {
        List list = new ArrayList();

        Project top = getNewProject( "top" );
        list.add( top );

        Project c1 = getNewProject( "c1" );
        c1.setParent( generateProjectDependency( top ) );
        list.add( c1 );

        Project c2 = getNewProject( "c2" );
        c2.setParent( generateProjectDependency( top ) );
        c2.setDependencies( Collections.singletonList( generateProjectDependency( c1 ) ) );
        list.add( c2 );

        List sortedList = ProjectSorter.getSortedProjects( list );

        assertNotNull( sortedList );

        Project p1 = (Project)sortedList.get( 0 );
        assertEquals( top.getArtifactId(), p1.getArtifactId() );
        Project p2 = (Project)sortedList.get( 1 );
        assertEquals( c1.getArtifactId(), p2.getArtifactId() );
        Project p3 = (Project)sortedList.get( 2 );
        assertEquals( c2.getArtifactId(), p3.getArtifactId() );
    }

    /**
     * test one of the child projects not having the artifactId or groupId empty and working off the
     * name instead
     *
     * @throws Exception
     */
    public void testIncompleteNestedProjectStructure()
        throws Exception
    {
        List list = new ArrayList();

        Project top = getNewProject( "top" );
        list.add( top );

        Project c1 = getIncompleteProject( "c1" );
        c1.setParent( generateProjectDependency( top ) );
        list.add( c1 );

        Project c2 = getNewProject( "c2" );
        c2.setParent( generateProjectDependency( top ) );
        c2.setDependencies( Collections.singletonList( generateProjectDependency( c1 ) ) );
        list.add( c2 );

        List sortedList = ProjectSorter.getSortedProjects( list );

        assertNotNull( sortedList );

        Project p1 = (Project)sortedList.get( 0 );
        assertEquals( top.getArtifactId(), p1.getArtifactId() );
        Project p2 = (Project)sortedList.get( 1 );
        assertEquals( c1.getArtifactId(), p2.getArtifactId() );
        Project p3 = (Project)sortedList.get( 2 );
        assertEquals( c2.getArtifactId(), p3.getArtifactId() );

    }

    /**
     * project sorter can work with name replacing the artifactid and groupId
     *
     * @param projectId
     * @return
     */
    private Project getIncompleteProject( String projectId )
    {
        Project project = new Project();
        project.setName( "foo" + projectId );
        project.setVersion( "v" + projectId );

        return project;
    }

    private Project getNewProject( String projectId )
    {
        Project project = new Project();
        project.setArtifactId( "a" + projectId );
        project.setGroupId( "g" + projectId );
        project.setVersion( "v" + projectId );
        project.setName( "n" + projectId );

        return project;
    }

    private ProjectDependency generateProjectDependency( Project project )
    {
        ProjectDependency dep = new ProjectDependency();
        dep.setArtifactId( project.getArtifactId() );
        dep.setGroupId( project.getGroupId() );
        dep.setVersion( project.getVersion() );

        return dep;
    }

}