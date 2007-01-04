package org.apache.maven.continuum.core.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.continuum.utils.WorkingDirectoryService;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.FileUtils;

import java.util.Map;
import java.io.File;

/**
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.action.Action"
 *   role-hint="clean-working-directory"
 */
public class CleanWorkingDirectoryAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement 
     */
    private WorkingDirectoryService workingDirectoryService;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    public void execute( Map context )
        throws Exception
    {
        Project project = store.getProject( getProjectId( context ) );

        File workingDirectory = workingDirectoryService.getWorkingDirectory( project );

        if ( workingDirectory.exists() )
        {
            FileUtils.cleanDirectory( workingDirectory );
        }
    }
}
