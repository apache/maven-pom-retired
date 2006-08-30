package org.apache.maven.continuum.web.action;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Schedule;

import java.util.Collection;

/**
 * @author Nik Gonzalez
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="schedule"
 */
public class ScheduleAction
    extends ContinuumActionSupport
{
    private int id;

    private boolean active = true;

    private String cronExpression;

    private int delay;

    private String description;

    private String name;

    private Collection schedules;

    private Schedule schedule;

    private boolean confirmed;

    public String summary()
        throws ContinuumException
    {
        schedules = getContinuum().getSchedules();

        return SUCCESS;
    }

    public String input()
    {
        if ( id != 0 )
        {
            try
            {
                schedule = getContinuum().getSchedule( id );
                active = schedule.isActive();
                cronExpression= schedule.getCronExpression();
                description = schedule.getDescription();
                name = schedule.getName();
                delay = schedule.getDelay();
            }
            catch ( ContinuumException e )
            {
                addActionError( "unable to retrieve schedule for editting" );
                return ERROR;
            }
        }
        return SUCCESS;
    }

    public String save()
    {
        if ( id == 0 )
        {
            try
            {
                Schedule schedule = new Schedule();
                schedule.setActive( active );
                schedule.setCronExpression( cronExpression );
                schedule.setDelay( delay );
                schedule.setDescription( description );
                schedule.setName( name );

                getContinuum().addSchedule( schedule );
            }
            catch ( ContinuumException e )
            {
                addActionError( "unable to add schedule" );
                return ERROR;
            }
            return SUCCESS;
        }
        else
        {

            try
            {
                schedule = getContinuum().getSchedule( id );

                schedule.setActive( active );
                schedule.setCronExpression( cronExpression );
                schedule.setDelay( delay );
                schedule.setDescription( description );
                schedule.setName( name );

                getContinuum().updateSchedule( schedule );

            }
            catch ( ContinuumException e )
            {
                addActionError( "unable to edit schedule" );
                return ERROR;
            }

            return SUCCESS;
        }
    }

    public String confirm()
        throws ContinuumException
    {
        schedule = getContinuum().getSchedule( id );

        return SUCCESS;
    }

    public String remove()
        throws ContinuumException
    {
        if ( confirmed )
        {
            getContinuum().removeSchedule( id );
        }
        else
        {
            return CONFIRM;
        }

        return SUCCESS;
    }

    public Collection getSchedules()
    {
        return schedules;
    }

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression( String cronExpression )
    {
        this.cronExpression = cronExpression;
    }

    public int getDelay()
    {
        return delay;
    }

    public void setDelay( int delay )
    {
        this.delay = delay;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Schedule getSchedule()
    {
        return schedule;
    }

    public void setSchedule( Schedule schedule )
    {
        this.schedule = schedule;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }
}
