/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManagerException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.formica.util.MungedHttpsURL;

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
        ContinuumProjectBuildingResult result = null;

        try
        {
            if ( !u.startsWith( "https" ) )
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
                    throw new ContinuumException( "'" + u + "' is not a valid secureURL." );
                }
            }

        }
        catch ( MalformedURLException e )
        {
            throw new ContinuumException( "'" + u + "' is not a valid URL.", e );
        }

        context.put( KEY_PROJECT_BUILDING_RESULT, result );
    }
}
