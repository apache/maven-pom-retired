package org.apache.maven.continuum.release.tasks;

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

import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.io.File;

/**
 * @author Edwin Punzalan
 */
public class PerformReleaseProjectTask
    extends AbstractReleaseProjectTask
{
    private File buildDirectory;

    private String goals;

    private boolean useReleaseProfile = true;

    public PerformReleaseProjectTask( String releaseId, ReleaseDescriptor descriptor, File buildDirectory,
                                      String goals, boolean useReleaseProfile, ReleaseManagerListener listener )
    {
        super( releaseId, descriptor, listener );
        setBuildDirectory( buildDirectory );
        setGoals( goals );
        setUseReleaseProfile( useReleaseProfile );
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public boolean isUseReleaseProfile()
    {
        return useReleaseProfile;
    }

    public void setUseReleaseProfile( boolean useReleaseProfile )
    {
        this.useReleaseProfile = useReleaseProfile;
    }

    public File getBuildDirectory()
    {
        return buildDirectory;
    }

    public void setBuildDirectory( File buildDirectory )
    {
        this.buildDirectory = buildDirectory;
    }
}
