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
 */

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultWorkingDirectoryService
    extends AbstractLogEnabled
    implements WorkingDirectoryService
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    // ----------------------------------------------------------------------
    // WorkingDirectoryService Implementation
    // ----------------------------------------------------------------------

    public File getWorkingDirectory( Project project )
    {
//        TODO: Enable, this is what we really want
//        ContinuumProjectGroup projectGroup = project.getProjectGroup();
//
//        return new File( projectGroup.getWorkingDirectory(),
//                         project.getPath() );

        return new File( configurationService.getWorkingDirectory(), Integer.toString( project.getId() ) );
    }
}
