package org.apache.maven.continuum.initialization;

import org.apache.maven.continuum.project.ContinuumProjectGroup;
import org.apache.maven.continuum.project.ContinuumBuildSettings;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.build.settings.BuildSettingsConstants;

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

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class DefaultContinuumInitializer
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

    // ----------------------------------------------------------------------
    //  Requirements
    // ----------------------------------------------------------------------

    /** @plexus.requirement */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize()
        throws ContinuumInitializationException
    {
        ContinuumBuildSettings defaultBuildSettings = createDefaultBuildSettings();

        try
        {
            store.addBuildSettings( defaultBuildSettings );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Error storing default Continuum build settings.", e );
        }

        ContinuumProjectGroup defaultProjectGroup = createDefaultProjectGroup();

        defaultProjectGroup.addBuildSetting( defaultBuildSettings );

        try
        {
            store.addProjectGroup( defaultProjectGroup );
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Error storing default Continuum project group.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ContinuumProjectGroup createDefaultProjectGroup()
        throws ContinuumInitializationException
    {
        ContinuumProjectGroup projectGroup = new ContinuumProjectGroup();

        projectGroup.setName( DEFAULT_PROJECT_GROUP_NAME );

        projectGroup.setGroupId( DEFAULT_PROJECT_GROUP_ID );

        projectGroup.setDescription( DEFAULT_PROJECT_GROUP_DESCRIPTION );

        return projectGroup;
    }

    public ContinuumBuildSettings createDefaultBuildSettings()
        throws ContinuumInitializationException
    {
        ContinuumBuildSettings buildSettings = new ContinuumBuildSettings();

        buildSettings.setName( DEFAULT_BUILD_SETTINGS_NAME );

        buildSettings.setNotificationScheme( BuildSettingsConstants.NOTIFICATION_STRATEGY_ON_FAILURE );

        buildSettings.setLabelingScheme( BuildSettingsConstants.LABELLING_STRATEGY_NEVER );

        buildSettings.setScmMode( BuildSettingsConstants.SCM_MODE_UPDATE );

        // Setting the jdk version to null means fall back to the default JAVA_HOME.
        buildSettings.setJdkVersion( null );

        return buildSettings;
    }
}
