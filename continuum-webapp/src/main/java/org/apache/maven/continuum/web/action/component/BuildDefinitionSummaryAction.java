package org.apache.maven.continuum.web.action.component;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.BuildDefinitionSummary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * BuildDefinitionSummaryAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="buildDefinitionSummary"
 */
public class BuildDefinitionSummaryAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private String projectGroupName;

    private int projectId;

    private ProjectGroup projectGroup;

    private List projectBuildDefinitionSummaries = new ArrayList();

    private List groupBuildDefinitionSummaries = new ArrayList();

    private List allBuildDefinitionSummaries = new ArrayList();

    public String summarizeForProject()
    {
        try
        {
            projectGroup = getContinuum().getProjectGroupByProjectId( projectId );
            projectGroupId = projectGroup.getId();
            projectGroupName = projectGroup.getName();
            groupBuildDefinitionSummaries = gatherGroupBuildDefinitionSummaries( projectGroupId );
            projectBuildDefinitionSummaries = gatherProjectBuildDefinitionSummaries( projectId );

            allBuildDefinitionSummaries.addAll( groupBuildDefinitionSummaries );
            allBuildDefinitionSummaries.addAll( projectBuildDefinitionSummaries );
        }
        catch ( ContinuumException e )
        {
            getLogger().info( "unable to build summary" );
            return ERROR;
        }

        return SUCCESS;
    }

    public String summarizeForGroup()
    {
        try
        {
            groupBuildDefinitionSummaries = gatherGroupBuildDefinitionSummaries( projectGroupId );

            projectGroup = getContinuum().getProjectGroup( projectGroupId );

            for ( Iterator i = projectGroup.getProjects().iterator(); i.hasNext(); )
            {
                Project project = (Project) i.next();
                projectBuildDefinitionSummaries.addAll( gatherProjectBuildDefinitionSummaries( project.getId() ) );

            }

            allBuildDefinitionSummaries.addAll( groupBuildDefinitionSummaries );
            allBuildDefinitionSummaries.addAll( projectBuildDefinitionSummaries );
        }
        catch ( ContinuumException e )
        {
            getLogger().info( "unable to build summary" );
            return ERROR;
        }

        return SUCCESS;
    }

    private List gatherProjectBuildDefinitionSummaries( int projectId )
        throws ContinuumException
    {
        List summaryList = new ArrayList();

        Project project = getContinuum().getProjectWithAllDetails( projectId );
        for ( Iterator i = project.getBuildDefinitions().iterator(); i.hasNext(); )
        {
            BuildDefinitionSummary bds = generateBuildDefinitionSummary( (BuildDefinition) i.next() );
            bds.setFrom( "PROJECT" );
            bds.setProjectId( project.getId() );
            bds.setProjectName( project.getName() );

            summaryList.add( bds );
        }

        return summaryList;
    }


    private List gatherGroupBuildDefinitionSummaries( int projectGroupId )
        throws ContinuumException
    {
        List summaryList = new ArrayList();

        projectGroup = getContinuum().getProjectGroup( projectGroupId );

        for ( Iterator i = projectGroup.getBuildDefinitions().iterator(); i.hasNext(); )
        {
            BuildDefinitionSummary bds = generateBuildDefinitionSummary( (BuildDefinition) i.next() );
            bds.setFrom( "GROUP" );
            bds.setProjectGroupId( projectGroup.getId() );

            summaryList.add( bds );
        }

        return summaryList;
    }

    private BuildDefinitionSummary generateBuildDefinitionSummary( BuildDefinition bd )
    {
        BuildDefinitionSummary bds = new BuildDefinitionSummary();

        bds.setGoals( bd.getGoals() );
        bds.setId( bd.getId() );
        bds.setArguments( bd.getArguments() );
        bds.setBuildFile( bd.getBuildFile() );
        bds.setScheduleId( bd.getSchedule().getId() );
        bds.setScheduleName( bd.getSchedule().getName() );
        bds.setIsDefault( bd.isDefaultForProject() );
        bds.setIsBuildFresh( bd.isBuildFresh() );

        return bds;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public List getProjectBuildDefinitionSummaries()
    {
        return projectBuildDefinitionSummaries;
    }

    public void setProjectBuildDefinitionSummaries( List projectBuildDefinitionSummaries )
    {
        this.projectBuildDefinitionSummaries = projectBuildDefinitionSummaries;
    }

    public List getGroupBuildDefinitionSummaries()
    {
        return groupBuildDefinitionSummaries;
    }

    public void setGroupBuildDefinitionSummaries( List groupBuildDefinitionSummaries )
    {
        this.groupBuildDefinitionSummaries = groupBuildDefinitionSummaries;
    }

    public List getAllBuildDefinitionSummaries()
    {
        return allBuildDefinitionSummaries;
    }

    public void setAllBuildDefinitionSummaries( List allBuildDefinitionSummaries )
    {
        this.allBuildDefinitionSummaries = allBuildDefinitionSummaries;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }
}
