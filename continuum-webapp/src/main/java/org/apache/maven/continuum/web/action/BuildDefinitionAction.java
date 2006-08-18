package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.ContinuumActionException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/*
 * Copyright 2005 The Apache Software Foundation.
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

/**
 * BuildDefinitionAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $ID:$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="buildDefinition"
 */
public class BuildDefinitionAction
    extends ContinuumActionSupport
{
    public static final String CONFIRM = "confirm";

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

    private Map schedules;

    private Map profiles;

    public void prepare()
        throws Exception
    {
        super.prepare();

        if ( schedules == null )
        {
            schedules = new HashMap();

            Collection allSchedules = continuum.getSchedules();

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
     * @return
     */
    public String input()
    {
        try
        {
        if ( executor == null )
        {
            if ( projectId != 0 )
            {
                executor = continuum.getProject( projectId ).getExecutorId();
            }
            else
            {
                Project project = (Project)continuum.getProjectGroup( projectGroupId ).getProjects().get( 0 );
                executor = project.getExecutorId();

            }

        }
        }
        catch ( ContinuumException e )
        {
            addActionError( "error determining executor type" );
            return ERROR;
        }

        if ( buildDefinitionId != 0 )
        {
            try
            {
                BuildDefinition buildDefinition = continuum.getBuildDefinition( buildDefinitionId );
                goals = buildDefinition.getGoals();
                arguments = buildDefinition.getArguments();
                buildFile = buildDefinition.getBuildFile();
                defaultBuildDefinition = buildDefinition.isDefaultForProject();

            }
            catch ( ContinuumException ce )
            {
                addActionError( "error getting build id" );
                return ERROR;
            }
        }

        return INPUT;
    }

    public String saveToProject()
    {

        try
        {
            if ( buildDefinitionId == 0 )
            {
                continuum.addBuildDefinitionToProject( projectId, getBuildDefinitionFromInput() );
            }
            else
            {
                continuum.updateBuildDefinitionForProject( projectId, getBuildDefinitionFromInput() );
            }
        }
        catch ( ContinuumActionException cae )
        {
            addActionError( cae.getMessage() );
            return INPUT;
        }
        catch ( ContinuumException ce )
        {
            getLogger().info("error saving project build definition", ce);
            addActionError( "error saving project build definition" );
            return ERROR;
        }

        return SUCCESS;
    }

    public String saveToGroup()
    {
        try
        {
            if ( buildDefinitionId == 0 )
            {
                continuum.addBuildDefinitionToProjectGroup( projectGroupId, getBuildDefinitionFromInput() );
            }
            else
            {
                continuum.updateBuildDefinitionForProjectGroup( projectGroupId, getBuildDefinitionFromInput() );
            }
        }
        catch ( ContinuumActionException cae )
        {
            addActionError( cae.getMessage() );
            return INPUT;
        }
        catch ( ContinuumException ce )
        {
            getLogger().info("error saving group build definition", ce);
            addActionError( "error saving group build definition" );
            return ERROR;
        }

        return SUCCESS;
    }

    public String removeFromProject()
    {
        if ( confirmed )
        {
            try
            {
                continuum.removeBuildDefinitionFromProject( projectId, buildDefinitionId );

                return SUCCESS;
            }
            catch ( ContinuumException ce )
            {
                getLogger().info("error removing build definition from project", ce);
                addActionError( "error removing build definition from project" );
                return ERROR;
            }
        }
        else
        {
            return CONFIRM;
        }
    }

    public String removeFromProjectGroup()
    {
        if ( confirmed )
        {
            try
            {
                continuum.removeBuildDefinitionFromProject( projectGroupId, buildDefinitionId );

                return SUCCESS;
            }
            catch ( ContinuumException ce )
            {
                getLogger().info("error removing build definition from project group", ce);
                addActionError( "error removing build definition from project group" );
                return ERROR;
            }
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
            schedule = continuum.getSchedule( scheduleId );
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
}
