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
import org.apache.maven.continuum.web.action.ContinuumActionSupport;

/**
 * @author Nik Gonzalez
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="addSchedule"
 */
public class AddScheduleAction
    extends ContinuumActionSupport
{

    private boolean active = true;

    private String cronExpression;

    private int delay = 0;

    private String description;

    private String name;

    public String execute()
        throws Exception
    {
        try
        {
            Schedule schedule = new Schedule();
            schedule.setActive( active );
            schedule.setCronExpression( cronExpression );
            schedule.setDelay( delay );
            schedule.setDescription( description );
            schedule.setName( name );

            continuum.addSchedule( schedule );
        }
        catch ( ContinuumException e )
        {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public String doDefault()
    {
        return INPUT;
    }

    public boolean getActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
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

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
