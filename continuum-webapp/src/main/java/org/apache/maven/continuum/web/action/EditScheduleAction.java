package org.apache.maven.continuum.web.action;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Schedule;

/**
 * @author Nik Gonzalez
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="editSchedule"
 */
public class EditScheduleAction
    extends AbstractContinuumAction
{

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    private Schedule schedule;

    private int id;

    private boolean active;

    private String cronExpression;

    private int delay;

    private String description;

    private String name;

    public String execute()
        throws Exception
    {
        try
        {
            schedule = continuum.getSchedule( id );

        }
        catch ( ContinuumException e )
        {
            e.printStackTrace();
        }
        schedule.setActive( active );
        schedule.setCronExpression( cronExpression );
        schedule.setDelay( delay );
        schedule.setDescription( description );
        schedule.setName( name );

        try
        {
            continuum.updateSchedule( schedule );
        }
        catch ( ContinuumException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public String doEdit()
        throws Exception
    {
        try
        {
            schedule = continuum.getSchedule( id );
        }
        catch ( ContinuumException e )
        {
            e.printStackTrace();
        }

        active = schedule.isActive();
        cronExpression = schedule.getCronExpression();
        delay = schedule.getDelay();
        description = schedule.getDescription();
        name = schedule.getName();

        return INPUT;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public void setCronExpression( String cronExpression )
    {
        this.cronExpression = cronExpression;
    }

    public void setDelay( int delay )
    {
        this.delay = delay;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }

    public Schedule getSchedule()
    {
        return schedule;
    }

    public void setSchedule( Schedule schedule )
    {
        this.schedule = schedule;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public int getDelay()
    {
        return delay;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }

}
