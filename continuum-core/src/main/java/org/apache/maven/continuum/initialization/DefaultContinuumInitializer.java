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

import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
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
    // Default values for the default build settings
    // ----------------------------------------------------------------------

    public static final String DEFAULT_BUILD_SETTINGS_NAME = "DEFAULT_BUILD_SETTINGS";

    // Cron expression for execution every hour.
    //public static final String DEFAULT_BUILD_SETTINGS_CRON_EXPRESSION = "0 0 * * * ?";
    public static final String DEFAULT_BUILD_SETTINGS_CRON_EXPRESSION = "0 * * * * ?";

    // ----------------------------------------------------------------------
    // Default project group and build settings
    // ----------------------------------------------------------------------

    private ContinuumBuildSettings defaultBuildSettings;

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

        defaultBuildSettings = createDefaultBuildSettings();

        try
        {
            defaultBuildSettings = store.addBuildSettings( defaultBuildSettings );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Error storing default Continuum build settings.", e );
        }
    }

    public ContinuumBuildSettings getDefaultBuildSettings()
    {
        return defaultBuildSettings;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ContinuumBuildSettings createDefaultBuildSettings()
        throws ContinuumInitializationException
    {
        ContinuumBuildSettings buildSettings = new ContinuumBuildSettings();

        buildSettings.setName( DEFAULT_BUILD_SETTINGS_NAME );

        buildSettings.setCronExpression( DEFAULT_BUILD_SETTINGS_CRON_EXPRESSION );

        return buildSettings;
    }
}
