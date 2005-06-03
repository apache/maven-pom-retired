/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import java.net.URL;
import java.util.Map;

import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;

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

    public  void execute( Map context )
        throws Exception
    {
        String projectBuilderId = getString( context, KEY_PROJECT_BUILDER_ID );

        URL url = new URL( getString( context, KEY_URL ) );

        ContinuumProjectBuilder projectBuilder = projectBuilderManager.getProjectBuilder( projectBuilderId );

        ContinuumProjectBuildingResult result = projectBuilder.buildProjectsFromMetadata( url );

        context.put( KEY_PROJECT_BUILDING_RESULT, result );
    }
}
