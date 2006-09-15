package org.apache.maven.continuum.release;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugins.release.ReleaseManagerListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Edwin Punzalan
 */
public class DefaultReleaseManagerListener
    implements ReleaseManagerListener, ContinuumReleaseManagerListener
{
    private String goalName;

    private List completedPhases;

    private String inProgress;

    private List phases;

    private String error;

    private int state;

    public void goalStart( String name, List phases )
    {
        state = LISTENING;
        goalName = name;
        this.phases = phases;
        completedPhases = Collections.synchronizedList( new ArrayList() );
        inProgress = null;
    }

    public void phaseStart( String name )
    {
        inProgress = name;
    }

    public void phaseEnd()
    {
        completedPhases.add( inProgress );

        inProgress = null;
    }

    public void phaseSkip( String name )
    {
        completedPhases.add( name );
    }

    public void goalEnd()
    {
        state = FINISHED;
    }

    public void error( String message )
    {
        error = message;
        goalEnd();
    }

    public List getCompletedPhases()
    {
        return completedPhases;
    }

    public String getInProgress()
    {
        return inProgress;
    }

    public List getPhases()
    {
        return phases;
    }

    public String getGoalName()
    {
        return goalName;
    }

    public String getError()
    {
        return error;
    }

    public int getState()
    {
        return state;
    }
}
