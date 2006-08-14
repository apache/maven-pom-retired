package org.apache.maven.continuum.core.action;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManagerException;
import org.codehaus.plexus.formica.util.MungedHttpsURL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CreateProjectsFromMetadata
    extends AbstractContinuumAction
{
    private ContinuumProjectBuilderManager projectBuilderManager;

    public static final String KEY_URL = "url";

    public static final String KEY_PROJECT_BUILDER_ID = "builderId";

    public static final String KEY_PROJECT_BUILDING_RESULT = "projectBuildingResult";

    public void execute( Map context )
        throws ContinuumException, ContinuumProjectBuilderManagerException, ContinuumProjectBuilderException
    {
        String projectBuilderId = getString( context, KEY_PROJECT_BUILDER_ID );

        String u = getString( context, KEY_URL );

        URL url;

        ContinuumProjectBuilder projectBuilder = projectBuilderManager.getProjectBuilder( projectBuilderId );

        ContinuumProjectBuildingResult result;

        try
        {
            if ( !u.startsWith( "http" ) )
            {
                url = new URL( u );

                result = projectBuilder.buildProjectsFromMetadata( url, null, null );
            }
            else
            {
                MungedHttpsURL mungedURL = new MungedHttpsURL( u );

                if ( mungedURL.isValid() )
                {
                    url = mungedURL.getURL();

                    result = projectBuilder.buildProjectsFromMetadata( url, mungedURL.getUsername(), mungedURL
                        .getPassword() );
                }
                else
                {
                    result = new ContinuumProjectBuildingResult();

                    result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
                }
            }

        }
        catch ( MalformedURLException e )
        {
            result = new ContinuumProjectBuildingResult();

            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }

        context.put( KEY_PROJECT_BUILDING_RESULT, result );
    }
}
