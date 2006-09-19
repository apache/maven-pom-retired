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
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="schedule"
 */
public class ScheduleAction
    extends ContinuumConfirmAction
{
    private int id;

    private boolean active = true;

    private int delay;

    private String description;

    private String name;

    private Collection schedules;

    private Schedule schedule;

    private boolean confirmed;

    private int maxJobExecutionTime;

    private String second = "0";

    private String minute = "0";

    private String hour = "*";

    private String dayOfMonth = "*";

    private String month = "*";

    private String dayOfWeek = "?";

    private String year;

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

                String[] cronEx = schedule.getCronExpression().split( " " );
                second = cronEx[0];
                minute = cronEx[1];
                hour = cronEx[2];
                dayOfMonth = cronEx[3];
                month = cronEx[4];
                dayOfWeek = cronEx[5];
                year = cronEx[6];

                description = schedule.getDescription();
                name = schedule.getName();
                delay = schedule.getDelay();
                maxJobExecutionTime = schedule.getMaxJobExecutionTime();
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
                getContinuum().addSchedule( setFields( new Schedule() ) );
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
                getContinuum().updateSchedule( setFields( getContinuum().getSchedule( id ) ) );
            }
            catch ( ContinuumException e )
            {
                addActionError( "unable to edit schedule" );
                return ERROR;
            }

            return SUCCESS;
        }
    }

    private Schedule setFields( Schedule schedule )
    {
        schedule.setActive( active );
        schedule.setCronExpression( getCronExpression() );
        schedule.setDelay( delay );
        schedule.setDescription( description );
        schedule.setName( name );
        schedule.setMaxJobExecutionTime( maxJobExecutionTime );

        return schedule;
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
            setConfirmationInfo( "Schedule Removal", "removeSchedule", name, "id", "" + id );
                        
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

    public int getMaxJobExecutionTime()
    {
        return maxJobExecutionTime;
    }

    public void setMaxJobExecutionTime( int maxJobExecutionTime )
    {
        this.maxJobExecutionTime = maxJobExecutionTime;
    }

    public String getSecond()
    {
        return second;
    }

    public void setSecond( String second )
    {
        this.second = second;
    }

    public String getMinute()
    {
        return minute;
    }

    public void setMinute( String minute )
    {
        this.minute = minute;
    }

    public String getHour()
    {
        return hour;
    }

    public void setHour( String hour )
    {
        this.hour = hour;
    }

    public String getDayOfMonth()
    {
        return dayOfMonth;
    }

    public void setDayOfMonth( String dayOfMonth )
    {
        this.dayOfMonth = dayOfMonth;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear( String year )
    {
        this.year = year;
    }

    public String getMonth()
    {
        return month;
    }

    public void setMonth( String month )
    {
        this.month = month;
    }

    public String getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek( String dayOfWeek )
    {
        this.dayOfWeek = dayOfWeek;
    }

    private String getCronExpression()
    {
        return ( second + " " + minute + " " + hour + " " + dayOfMonth + " " +
                    month + " " + dayOfWeek + " " + year ).trim();
    }
}
