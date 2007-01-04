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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.execution.maven.m2.SettingsConfigurationException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilder;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuilderException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManager;
import org.apache.maven.continuum.project.builder.manager.ContinuumProjectBuilderManagerException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.formica.util.MungedHttpsURL;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Resolve the project url being passed in and gather authentication information
 * if the url is so configured, then create the projects
 *
 * Supports:
 *
 * - standard maven-scm url
 * - MungedUrl https://username:password@host
 * - maven settings based, server = host and scm info set to username and password
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.action.Action"
 *   role-hint="create-projects-from-metadata"
 */
public class CreateProjectsFromMetadataAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumProjectBuilderManager projectBuilderManager;
    
    /**
     * @plexus.requirement
     */
    private MavenSettingsBuilder mavenSettingsBuilder;

    private Settings settings;

    public static final String KEY_URL = "url";

    public static final String KEY_PROJECT_BUILDER_ID = "builderId";

    public static final String KEY_PROJECT_BUILDING_RESULT = "projectBuildingResult";

    public void execute( Map context )
        throws ContinuumException, ContinuumProjectBuilderManagerException, ContinuumProjectBuilderException
    {
        String projectBuilderId = getString( context, KEY_PROJECT_BUILDER_ID );

        String curl = getString( context, KEY_URL );

        URL url;

        ContinuumProjectBuilder projectBuilder = projectBuilderManager.getProjectBuilder( projectBuilderId );

        ContinuumProjectBuildingResult result;

        try
        {
            if ( !curl.startsWith( "http" ) )
            {
                url = new URL( curl );

                result = projectBuilder.buildProjectsFromMetadata( url, null, null );
            }
            else
            {
                url = new URL ( curl );
                String username = null;
                String password = null;

                try
                {
                    settings = getSettings();

                    getLogger().info( "checking for settings auth setup" );
                    if ( settings != null && settings.getServer( url.getHost() ) != null )
                    {
                        getLogger().info( "found setting based auth setup, using" );
                        Server server = settings.getServer( url.getHost() );

                        username = server.getUsername();
                        password = server.getPassword();
                    }
                }
                catch ( SettingsConfigurationException se )
                {
                    getLogger().warn( "problem with settings file, disabling scm resolution of username and password" );
                }

                MungedHttpsURL mungedURL;
                
                if ( username == null )
                {
                    mungedURL = new MungedHttpsURL( curl );
                    username = mungedURL.getUsername();
                    password = mungedURL.getPassword();
                }
                else
                {
                   mungedURL = new MungedHttpsURL( curl, username, password );
                }

                mungedURL.setLogger( getLogger() );

                if ( mungedURL.isValid() )
                {
                    url = mungedURL.getURL();

                    result = projectBuilder.buildProjectsFromMetadata( url, username, password );
                }
                else
                {
                    result = new ContinuumProjectBuildingResult();
                    getLogger().info( "Malformed URL (MungedHttpsURL is not valid): " + hidePasswordInUrl( curl ) );
                    result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
                }
            }

        }
        catch ( MalformedURLException e )
        {
            getLogger().info( "Malformed URL: " + hidePasswordInUrl( curl ), e );
            result = new ContinuumProjectBuildingResult();
            result.addError( ContinuumProjectBuildingResult.ERROR_MALFORMED_URL );
        }


        context.put( KEY_PROJECT_BUILDING_RESULT, result );
    }

    private String hidePasswordInUrl( String url )
    {
        int indexAt = url.indexOf( "@" );

        if ( indexAt < 0 )
        {
            return url;
        }

        String s = url.substring( 0, indexAt );

        int pos = s.lastIndexOf( ":" );

        return s.substring( 0, pos + 1 ) + "*****" + url.substring( indexAt );
    }

    private Settings getSettings()
        throws SettingsConfigurationException
    {
        try
        {
            return mavenSettingsBuilder.buildSettings();
        }
        catch ( IOException e )
        {
            throw new SettingsConfigurationException( "Error reading settings file", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new SettingsConfigurationException( e.getMessage(), e.getDetail(), e.getLineNumber(),
                                                      e.getColumnNumber() );
        }
    }


    public ContinuumProjectBuilderManager getProjectBuilderManager()
    {
        return projectBuilderManager;
    }

    public void setProjectBuilderManager( ContinuumProjectBuilderManager projectBuilderManager )
    {
        this.projectBuilderManager = projectBuilderManager;
    }

    public MavenSettingsBuilder getMavenSettingsBuilder()
    {
        return mavenSettingsBuilder;
    }

    public void setMavenSettingsBuilder( MavenSettingsBuilder mavenSettingsBuilder )
    {
        this.mavenSettingsBuilder = mavenSettingsBuilder;
    }
}
