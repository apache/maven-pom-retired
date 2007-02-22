package org.apache.maven.continuum.web.action.admin;

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

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Preparable;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="configuration"
 */
public class ConfigurationAction
    extends ContinuumActionSupport
    implements Preparable
{

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    private String workingDirectory;

    private String buildOutputDirectory;

    private String deploymentRepositoryDirectory;

    private String baseUrl;

    public void prepare()
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        File workingDirectoryFile = configuration.getWorkingDirectory();
        if ( workingDirectoryFile != null )
        {
            workingDirectory = workingDirectoryFile.getAbsolutePath();
        }

        File buildOutputDirectoryFile = configuration.getBuildOutputDirectory();
        if ( buildOutputDirectoryFile != null )
        {
            buildOutputDirectory = buildOutputDirectoryFile.getAbsolutePath();
        }

        File deploymentRepositoryDirectoryFile = configuration.getDeploymentRepositoryDirectory();
        if ( deploymentRepositoryDirectoryFile != null )
        {
            deploymentRepositoryDirectory = deploymentRepositoryDirectoryFile.getAbsolutePath();
        }

        baseUrl = configuration.getUrl();

        if ( StringUtils.isEmpty( baseUrl ) )
        {
            HttpServletRequest request = ServletActionContext.getRequest();
            baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                request.getContextPath();
            getLogger().info( "baseUrl='" + baseUrl + "'" );
        }
    }

    public String save()
        throws ConfigurationStoringException, ContinuumStoreException
    {
        try
        {
            checkManageConfigurationAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        ConfigurationService configuration = getContinuum().getConfiguration();

        configuration.setWorkingDirectory( new File( workingDirectory ) );

        configuration.setBuildOutputDirectory( new File( buildOutputDirectory ) );

        if ( StringUtils.isNotEmpty( deploymentRepositoryDirectory ) )
        {
            configuration.setDeploymentRepositoryDirectory( new File( deploymentRepositoryDirectory ) );
        }

        configuration.setUrl( baseUrl );

        configuration.setInitialized( true );

        configuration.store();

        return SUCCESS;
    }

    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( String workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public String getDeploymentRepositoryDirectory()
    {
        return deploymentRepositoryDirectory;
    }

    public void setDeploymentRepositoryDirectory( String deploymentRepositoryDirectory )
    {
        this.deploymentRepositoryDirectory = deploymentRepositoryDirectory;
    }

    public String getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( String buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }
}
