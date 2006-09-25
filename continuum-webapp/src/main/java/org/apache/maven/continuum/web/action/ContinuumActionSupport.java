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
 *
 */

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.initialization.ContinuumInitializationException;
import org.codehaus.plexus.xwork.action.PlexusActionSupport;

import com.opensymphony.xwork.Preparable;

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
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    public void prepare()
        throws Exception
    {
        getLogger().debug( "Checking if Continuum is initialized" );

        if ( !continuum.getConfiguration().isInitialized() )
        {
            throw new ContinuumInitializationException( "This is your first time running continuum, "
                + "when you access it through a web browser you will need to enter some "
                + "information before being able to use it. " + "You can ignore this exception." );
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
