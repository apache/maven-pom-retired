package org.apache.maven.continuum.project;

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

import java.io.Serializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ContinuumProjectState.java,v 1.1.1.1 2005/03/29 20:42:02 trygvis Exp $
 */
public class ContinuumProjectState
    implements Serializable
{
    /**
     * This state indicates that the project is new and has never been build.
     */
//    public final static ContinuumProjectState NEW = new ContinuumProjectState( "new" );
    public final static int NEW = 1;

    /**
     * This state indicates that the project has been successfully build.
     */
//    public final static ContinuumProjectState OK = new ContinuumProjectState( "ok" );
    public final static int OK = 2;

    /**
     * This state indicates that the project didn't build successfully.
     */
//    public final static ContinuumProjectState FAILED = new ContinuumProjectState( "failed" );
    public final static int FAILED = 3;

    /**
     * This stats indicates that there was a error while building the project.
     * <p/>
     * A error while building the project might indicate that it couldn't
     * download the sources or other things that continuum doesn't have any
     * control over.
     */
//    public final static ContinuumProjectState ERROR = new ContinuumProjectState( "error" );
    public final static int ERROR = 4;

    /**
     * This state indicates that this project has been placed on the build queue.
     * <p/>
     * Continuum can be configured with a delay from the first build signal to
     * the actual build starts to make.
     */
//    public final static ContinuumProjectState BUILD_SIGNALED = new ContinuumProjectState( "signaled" );
//    public final static int BUILD_SIGNALED = 5;

    /**
     * This state indicates that a project is currently beeing build.
     */
//    public final static ContinuumProjectState BUILDING = new ContinuumProjectState( "building" );
    public final static int BUILDING = 6;

    public final static int CHECKING_OUT = 7;

    public final static int UPDATING = 8;

    private String name;

    protected ContinuumProjectState( String name )
    {
        this.name = name;
    }

    public String getI18nKey()
    {
        return "org.apache.maven.continuum.project.state." + name;
    }

    // ----------------------------------------------------------------------
    // Object Overrides
    // ----------------------------------------------------------------------

    public boolean equals( Object object )
    {
        if ( !( object instanceof ContinuumProjectState ) )
        {
            return false;
        }

        ContinuumProjectState other = (ContinuumProjectState) object;

        return name.equals( other.name );
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    public String toString()
    {
        return name;
    }
}
