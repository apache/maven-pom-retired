package org.apache.maven.continuum.utils;

/*
 * Copyright 2005 The Apache Software Foundation.
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

import org.codehaus.plexus.PlexusContainer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PlexusContainerManager
{
    private PlexusContainer container;

    private static PlexusContainerManager instance;

    private PlexusContainerManager()
    {
    }
    public static synchronized PlexusContainerManager getInstance()
    {
        if ( instance == null )
        {
            instance = new PlexusContainerManager();
        }

        return instance;
    }

    public PlexusContainer getContainer()
    {
        return container;
    }

    public void setContainer( PlexusContainer container )
    {
        this.container = container;
    }
}