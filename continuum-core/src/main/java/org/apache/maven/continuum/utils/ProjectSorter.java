/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.utils;

import org.apache.maven.continuum.project.ContinuumDependency;
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
                ContinuumDependency dependency = (ContinuumDependency) j.next();

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

    private static String getDependencyId( ContinuumDependency project )
    {
        return project.getGroupId() + ":" + project.getArtifactId();
    }
}
