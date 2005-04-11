package org.apache.maven.continuum.utils;

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

import org.codehaus.plexus.configuration.PlexusConfigurationException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PlexusUtils.java,v 1.1.1.1 2005/03/29 20:42:03 trygvis Exp $
 */
public class PlexusUtils
{
    private PlexusUtils()
    {
    }

    public static void assertConfiguration( Object configuration, String name )
        throws PlexusConfigurationException
    {
        if ( configuration == null )
        {
            throw new PlexusConfigurationException( "Missing configuration element: '" + name + "'." );
        }

        if ( configuration instanceof String )
        {
            String str = (String) configuration;

            if ( str.trim().length() == 0 )
            {
                throw new PlexusConfigurationException( "Misconfigured element '" + name + "': Element cannot be empty." );
            }
        }
    }

    public static void assertRequirement( Object requirement, String role )
        throws PlexusConfigurationException
    {
        if ( requirement == null )
        {
            throw new PlexusConfigurationException( "Missing requirement '" + role + "'." );
        }
    }
}
