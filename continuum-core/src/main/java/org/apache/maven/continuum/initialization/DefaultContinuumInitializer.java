package org.apache.maven.continuum.initialization;

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

import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 * @todo use this, reintroduce default project group
 */
public class DefaultContinuumInitializer
    extends AbstractLogEnabled
    implements ContinuumInitializer
{
    // ----------------------------------------------------------------------
    // Default values for the default project group
    // ----------------------------------------------------------------------

    public static final String DEFAULT_PROJECT_GROUP_NAME = "DEFAULT_PROJECT_GROUP";

    public static final String DEFAULT_PROJECT_GROUP_ID = "DEFAULT";

    public static final String DEFAULT_PROJECT_GROUP_DESCRIPTION = "Default Project Group";

    // ----------------------------------------------------------------------
    // Default values for the default schedule
    // ----------------------------------------------------------------------

    //TODO: move this to an other place
    public static final String DEFAULT_SCHEDULE_NAME = "DEFAULT_SCHEDULE";

    public static final String DEFAULT_SCHEDULE_DESCRIPTION = "Run hourly";

    // Cron expression for execution every hour.
    public static final String DEFAULT_SCHEDULE_CRON_EXPRESSION = "0 0 * * * ?";

    private Schedule defaultSchedule;

    // ----------------------------------------------------------------------
    //  Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize()
        throws ContinuumInitializationException
    {
        getLogger().info( "Continuum initializer running ..." );

        try
        {
            Schedule s = store.getScheduleByName( DEFAULT_SCHEDULE_NAME );

            if ( s != null )
            {
                defaultSchedule = s;
            }
            else
            {
                defaultSchedule = createDefaultSchedule();

                defaultSchedule = store.addSchedule( defaultSchedule );
            }
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Can't initialize default schedule.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Schedule createDefaultSchedule()
        throws ContinuumInitializationException
    {
        Schedule schedule = new Schedule();

        schedule.setName( DEFAULT_SCHEDULE_NAME );

        schedule.setDescription( DEFAULT_SCHEDULE_DESCRIPTION );

        schedule.setCronExpression( DEFAULT_SCHEDULE_CRON_EXPRESSION );

        schedule.setActive( true );

        return schedule;
    }
}
