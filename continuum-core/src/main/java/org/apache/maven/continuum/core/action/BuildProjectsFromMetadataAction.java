package org.apache.maven.continuum.core.action;

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

import java.net.URL;
import java.util.Map;

import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.store.ContinuumStoreException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class BuildProjectsFromMetadataAction
    extends AbstractContinuumAction
{
    public static final String KEY_URL = "url";

    public static final String KEY_PROJECT_BUILDER_ID = "builderId";

    public static final String KEY_PROJECT_BUILDING_RESULT = "projectBuildingResult";

    /**
     * @plexus.requirement
     */
    private ContinuumProjectBuilderManager projectBuilderManager;

    protected void doExecute( Map context )
        throws Exception
    {
        String projectBuilderId = getString( context, KEY_PROJECT_BUILDER_ID );

        URL url = new URL( getString( context, KEY_URL ) );

        ContinuumProjectBuilder projectBuilder = projectBuilderManager.getProjectBuilder( projectBuilderId );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( url );

        context.put( KEY_PROJECT_BUILDING_RESULT, result );
    }

    protected void handleException( Throwable throwable )
        throws ContinuumStoreException
    {
    }
}
