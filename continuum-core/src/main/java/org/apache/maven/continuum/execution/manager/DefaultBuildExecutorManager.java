package org.apache.maven.continuum.execution.manager;

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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultBuildExecutorManager
    extends AbstractLogEnabled
    implements BuildExecutorManager, Initializable
{
    private Map executors;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        if ( executors == null )
        {
            executors = new HashMap();
        }

        if ( executors.size() == 0 )
        {
            getLogger().warn( "No build executors defined." );
        }
        else
        {
            getLogger().info( "Build executors:" );

            for ( Iterator it = executors.keySet().iterator(); it.hasNext(); )
            {
                getLogger().info( "  " + it.next().toString() );
            }
        }
    }

    // ----------------------------------------------------------------------
    // BuildExecutorManager Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutor getBuildExecutor( String builderType )
        throws ContinuumException
    {
        ContinuumBuildExecutor executor = (ContinuumBuildExecutor) executors.get( builderType );

        if ( executor == null )
        {
            throw new ContinuumException( "No such executor: '" + builderType + "'." );
        }

        return executor;
    }

    public boolean hasBuildExecutor( String executorId )
    {
        return executors.containsKey( executorId );
    }
}
