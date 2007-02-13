package org.apache.maven.continuum.web.action;

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
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.ContinuumActionException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * BuildDefinitionAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="buildDefinition"
 */
public class BuildDefinitionAction
    extends ContinuumConfirmAction
{

    private int buildDefinitionId;

    private int projectId;

    private int projectGroupId;

    private int scheduleId;

    private boolean defaultBuildDefinition;

    private boolean confirmed = false;

    private String executor;

    private String goals;

    private String arguments;

    private String buildFile;

    private boolean buildFresh;

    private Map schedules;

    private Map profiles;

    private boolean groupBuildDefinition = false;

    public void prepare()
        throws Exception
    {
        super.prepare();

        if ( schedules == null )
        {
            schedules = new HashMap();

            Collection allSchedules = getContinuum().getSchedules();

            for ( Iterator i = allSchedules.iterator(); i.hasNext(); )
            {
                Schedule schedule = (Schedule) i.next();

                schedules.put( new Integer( schedule.getId() ), schedule.getName() );
            }
        }

        // todo: missing from continuum, investigate
        if ( profiles == null )
        {
            profiles = new HashMap();
        }


    }

    /**
     * if there is a build definition id set, then retrieve it..either way set us to up to work with build definition
     *
     * @return action result
     */
    public String input()
        throws ContinuumException
    {
        if ( executor == null )
        {
            if ( projectId != 0 )
            {
                executor = getContinuum().getProject( projectId ).getExecutorId();
            }
            else
            {
                List projects = getContinuum().getProjectGroupWithProjects( projectGroupId ).getProjects();
                
                if( projects.size() > 0 )
                {
                    Project project = (Project) projects.get( 0 );
                    executor = project.getExecutorId();
                }
            }
        }

        if ( buildDefinitionId != 0 )
        {
            BuildDefinition buildDefinition = getContinuum().getBuildDefinition( buildDefinitionId );
            goals = buildDefinition.getGoals();
            arguments = buildDefinition.getArguments();
            buildFile = buildDefinition.getBuildFile();
            buildFresh = buildDefinition.isBuildFresh();
            scheduleId = buildDefinition.getSchedule().getId();
            defaultBuildDefinition = buildDefinition.isDefaultForProject();
        }
        else
        {
            if ( "maven2".equals(executor) )
            {
                buildFile = "pom.xml";
            }
            else if ( "maven-1".equals(executor) )
            {
                buildFile = "project.xml";
            }
            else if ( "ant".equals(executor) )
            {
                buildFile = "build.xml";
            }
        }

        return SUCCESS;
    }

    public String saveBuildDefinition()
        throws ContinuumException
    {
        if ( projectId != 0  && !groupBuildDefinition )
        {
            return saveToProject();
        }
        else
        {
            return saveToGroup();
        }
    }

    public String saveToProject()
        throws ContinuumException
    {

        try
        {
            if ( buildDefinitionId == 0 )
            {
                getContinuum().addBuildDefinitionToProject( projectId, getBuildDefinitionFromInput() );
            }
            else
            {
                getContinuum().updateBuildDefinitionForProject( projectId, getBuildDefinitionFromInput() );
            }
        }
        catch ( ContinuumActionException cae )
        {
            addActionError( cae.getMessage() );
            return INPUT;
        }

        return SUCCESS;
    }

    public String saveToGroup()
        throws ContinuumException
    {
        try
        {
            BuildDefinition newBuildDef = getBuildDefinitionFromInput();
            
            if ( getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId ).size() == 0 )
            {
                newBuildDef.setDefaultForProject( true );
            }
            
            if ( buildDefinitionId == 0 )
            {
                getContinuum().addBuildDefinitionToProjectGroup( projectGroupId, newBuildDef );
            }
            else
            {
                getContinuum().updateBuildDefinitionForProjectGroup( projectGroupId, newBuildDef );
            }
        }
        catch ( ContinuumActionException cae )
        {
            addActionError( cae.getMessage() );
            return INPUT;
        }

        if ( projectId != 0 )
        {
            return SUCCESS;
        }
        else
        {
            return "success_group";
        }
    }

    public String removeFromProject()
        throws ContinuumException
    {
        if ( confirmed )
        {
            getContinuum().removeBuildDefinitionFromProject( projectId, buildDefinitionId );

            return SUCCESS;
        }
        else
        {            
            return CONFIRM;
        }
    }

    public String removeFromProjectGroup()
        throws ContinuumException
    {
        if ( confirmed )
        {
            getContinuum().removeBuildDefinitionFromProjectGroup( projectGroupId, buildDefinitionId );

            return SUCCESS;
        }
        else
        {
            return CONFIRM;
        }
    }

    private BuildDefinition getBuildDefinitionFromInput()
        throws ContinuumActionException
    {

        Schedule schedule;

        try
        {
            schedule = getContinuum().getSchedule( scheduleId );
        }
        catch ( ContinuumException e )
        {
            addActionError( "unable to get schedule" );
            throw new ContinuumActionException( "unable to get schedule" );
        }

        BuildDefinition buildDefinition = new BuildDefinition();

        if ( buildDefinitionId != 0 )
        {
            buildDefinition.setId( buildDefinitionId );
        }
        buildDefinition.setGoals( goals );
        buildDefinition.setArguments( arguments );
        buildDefinition.setBuildFile( buildFile );
        buildDefinition.setBuildFresh( buildFresh );
        buildDefinition.setDefaultForProject( defaultBuildDefinition );
        buildDefinition.setSchedule( schedule );

        return buildDefinition;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
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

    public int getScheduleId()
    {
        return scheduleId;
    }

    public void setScheduleId( int scheduleId )
    {
        this.scheduleId = scheduleId;
    }

    public boolean isDefaultBuildDefinition()
    {
        return defaultBuildDefinition;
    }

    public void setDefaultBuildDefinition( boolean defaultBuildDefinition )
    {
        this.defaultBuildDefinition = defaultBuildDefinition;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor( String executor )
    {
        this.executor = executor;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments( String arguments )
    {
        this.arguments = arguments;
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile( String buildFile )
    {
        this.buildFile = buildFile;
    }

    public boolean isBuildFresh()
    {
        return buildFresh;
    }

    public void setBuildFresh( boolean buildFresh )
    {
        this.buildFresh = buildFresh;
    }

    public Map getSchedules()
    {
        return schedules;
    }

    public void setSchedules( Map schedules )
    {
        this.schedules = schedules;
    }

    public Map getProfiles()
    {
        return profiles;
    }

    public void setProfiles( Map profiles )
    {
        this.profiles = profiles;
    }

    public boolean isGroupBuildDefinition()
    {
        return groupBuildDefinition;
    }

    public void setGroupBuildDefinition( boolean groupBuildDefinition )
    {
        this.groupBuildDefinition = groupBuildDefinition;
    }
}
