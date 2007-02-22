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

import com.opensymphony.xwork.ModelDriven;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.execution.maven.m2.SettingsConfigurationException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.app.company.CompanyPomHandler;
import org.apache.maven.shared.app.configuration.Configuration;
import org.apache.maven.shared.app.configuration.MavenAppConfiguration;
import org.codehaus.plexus.registry.RegistryException;

import java.io.IOException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: ConfigurationAction.java 480950 2006-11-30 14:58:35Z evenisse $
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="configureAppearance"
 */
public class ConfigureAppearanceAction
    extends ContinuumActionSupport
    implements ModelDriven
{
    /**
     * @plexus.requirement
     */
    private MavenAppConfiguration appConfiguration;

    /**
     * The configuration.
     */
    private Configuration configuration;

    private Model companyModel;

    /**
     * @plexus.requirement
     */
    private CompanyPomHandler companyPomHandler;

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper helper;

    public String execute()
        throws IOException, RegistryException
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

        appConfiguration.save( configuration );

        return SUCCESS;
    }

    public Object getModel()
    {
        return configuration;
    }

    public void prepare()
        throws ProjectBuildingException, ArtifactMetadataRetrievalException, SettingsConfigurationException
    {
        configuration = appConfiguration.getConfiguration();

        companyModel =
            companyPomHandler.getCompanyPomModel( configuration.getCompanyPom(), helper.getLocalRepository() );
    }

    public Model getCompanyModel()
    {
        return companyModel;
    }
}
