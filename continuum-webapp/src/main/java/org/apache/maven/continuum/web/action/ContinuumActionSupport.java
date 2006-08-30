package org.apache.maven.continuum.web.action;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import com.opensymphony.xwork.Preparable;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.initialization.ContinuumInitializationException;
import org.codehaus.plexus.xwork.action.PlexusActionSupport;

/**
 * ContinuumActionSupport
 *
 * @author Jesse McConnell <jesse@codehaus.org>
 * @version $Id$
 */
public class ContinuumActionSupport
    extends PlexusActionSupport
    implements Preparable
{
    public static final String CONFIRM = "confirm";

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    public void prepare()
        throws Exception
    {
        

        getLogger().info("checking the continuum configuration");

        if ( !continuum.getConfiguration().isInitialized() )
        {
            throw new ContinuumInitializationException( "continuum not initialized" );
        }

    }

    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }
}
