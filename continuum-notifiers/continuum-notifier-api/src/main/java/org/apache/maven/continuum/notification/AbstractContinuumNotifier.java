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
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
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
    public String getReportUrl( ContinuumProject project, ContinuumBuild build, ConfigurationService configurationService )
        throws ContinuumException
    {
        try
        {
            //TODO it's bad to load always the conf when we want read a value
            configurationService.load();

            StringBuffer buf = new StringBuffer( configurationService.getUrl() );

            if ( !buf.toString().endsWith( "/" ) )
            {
                buf.append( "/" );
            }

            buf.append( "target/ProjectBuild.vm/view/ProjectBuild/id/" ).append( project.getId() ).append( "/buildId/" ).append( build.getId() );

            return buf.toString();
        }
        catch ( ConfigurationLoadingException e )
        {
            throw new ContinuumException( "Can't obtain the base url from configuration.", e );
        }
    }
}
