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

import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.settings.Settings;

import java.io.File;

/**
 * The Continuum Release Manager is responsible for performing releases based on a release descriptor
 * that has been received by the Maven Release Plugin.
 *
 * @author Jason van Zyl
 */
//TODO:JW You can probably test this in isolation and then we can add methods to the main Continuum API for
//        releasing. The Core Continuum component would then have a dependency on this component and just delegate
//        to this component for release management.
public interface ContinuumReleaseManager
{
    String ROLE = ContinuumReleaseManager.class.getName();

    /**
     * Prepare a project for release which also updates the release descriptor
     *
     * @param descriptor
     * @throws ContinuumReleaseException
     */
    void prepare( ReleaseDescriptor descriptor, Settings settings )
        throws ContinuumReleaseException;

    /**
     * Perform a release based on a release descriptor received by the Maven Release Plugin.
     *
     * @param descriptor
     * @throws ContinuumReleaseException
     */
    void perform( ReleaseDescriptor descriptor, Settings settings, File buildDirectory,
                  String goals, boolean useReleaseProfile )
        throws ContinuumReleaseException;
}
