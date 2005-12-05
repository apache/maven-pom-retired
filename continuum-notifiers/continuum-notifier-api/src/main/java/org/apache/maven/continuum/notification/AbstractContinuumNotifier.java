package org.apache.maven.continuum.notification;

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
import org.apache.maven.continuum.configuration.ConfigurationLoadingException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.notification.notifier.AbstractNotifier;

public abstract class AbstractContinuumNotifier
    extends AbstractNotifier
{
    /**
     * Returns url of the last build
     *
     * @param project The project
     * @param build The build
     */
    public String getReportUrl( Project project, BuildResult build, ConfigurationService configurationService )
        throws ContinuumException
    {
        try
        {
            //TODO it's bad to load always the conf when we want read a value
            configurationService.load();

            StringBuffer buf = new StringBuffer( configurationService.getUrl() );

            if ( project != null && build != null )
            {
                if ( !buf.toString().endsWith( "/" ) )
                {
                    buf.append( "/" );
                }

                buf.append( "target/ProjectBuild.vm/view/ProjectBuild/id/" ).append( project.getId() ).append(
                    "/buildId/" ).append( build.getId() );
            }

            return buf.toString();
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new ContinuumException( "Can't obtain the base url from configuration.", e );
        }
    }

    public boolean shouldNotify( BuildResult build, BuildResult previousBuild )
    {
        if ( build == null )
        {
            return false;
        }

        // Always send if the project failed
        if ( build.getState() == ContinuumProjectState.FAILED || build.getState() == ContinuumProjectState.ERROR )
        {
            return true;
        }

        // Send if this is the first build
        if ( previousBuild == null )
        {
            return true;
        }

        // Send if the state has changed
        getLogger().info(
            "Current build state: " + build.getState() + ", previous build state: " + previousBuild.getState() );

        if ( build.getState() != previousBuild.getState() )
        {
            return true;
        }

        getLogger().info( "Same state, not sending message." );

        return false;
    }
}
