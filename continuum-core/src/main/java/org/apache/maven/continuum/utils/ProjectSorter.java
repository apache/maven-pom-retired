package org.apache.maven.continuum.utils;

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

import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.project.ContinuumProject;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sort projects by dependencies.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class ProjectSorter
{
    private ProjectSorter()
    {
        // no touchy...
    }

    /**
     * Sort a list of projects.
     * <ul>
     * <li>collect all the vertices for the projects that we want to build.</li>
     * <li>iterate through the deps of each project and if that dep is within
     * the set of projects we want to build then add an edge, otherwise throw
     * the edge away because that dependency is not within the set of projects
     * we are trying to build. we assume a closed set.</li>
     * <li>do a topo sort on the graph that remains.</li>
     * </ul>
     */
    public static List getSortedProjects( Collection projects )
        throws CycleDetectedException
    {
        DAG dag = new DAG();

        Map projectMap = new HashMap();

        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            String id = getProjectId( project );

            dag.addVertex( id );

            projectMap.put( id, project );
        }

        for ( Iterator i = projects.iterator(); i.hasNext(); )
        {
            ContinuumProject project = (ContinuumProject) i.next();

            String id = getProjectId( project );

            for ( Iterator j = project.getDependencies().iterator(); j.hasNext(); )
            {
                ProjectDependency dependency = (ProjectDependency) j.next();

                String dependencyId = getDependencyId( dependency );

                if ( dag.getVertex( dependencyId ) != null )
                {
                    dag.addEdge( id, dependencyId );
                }
            }
        }

        List sortedProjects = new ArrayList();

        for ( Iterator i = TopologicalSorter.sort( dag ).iterator(); i.hasNext(); )
        {
            String id = (String) i.next();

            sortedProjects.add( projectMap.get( id ) );
        }

        return sortedProjects;
    }

    private static String getProjectId( ContinuumProject project )
    {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    private static String getDependencyId( ProjectDependency project )
    {
        return project.getGroupId() + ":" + project.getArtifactId();
    }
}
