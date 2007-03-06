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
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.execution.maven.m2.SettingsConfigurationException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.app.company.CompanyPomHandler;
import org.apache.maven.shared.app.configuration.CompanyPom;
import org.apache.maven.shared.app.configuration.Configuration;
import org.apache.maven.shared.app.configuration.MavenAppConfiguration;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.rbac.Resource;

import java.io.IOException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: ConfigurationAction.java 480950 2006-11-30 14:58:35Z evenisse $
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="editPom"
 */
public class EditPomAction
    extends ContinuumActionSupport
    implements ModelDriven, SecureAction
{
    /**
     * @plexus.requirement
     */
    private MavenAppConfiguration appConfiguration;

    /**
     * The configuration.
     */
    private Configuration configuration;

    /**
     * @plexus.requirement
     */
    private CompanyPomHandler companyPomHandler;

    private Model companyModel;

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper helper;

    public String execute()
        throws IOException, ArtifactInstallationException, SettingsConfigurationException
    {
        // TODO: hack for passed in String[]
        String[] logo = (String[]) companyModel.getProperties().get( "organization.logo" );
        if ( logo != null )
        {
            companyModel.getProperties().put( "organization.logo", logo[0] );
        }

        companyPomHandler.save( companyModel, helper.getLocalRepository() );

        return SUCCESS;
    }

    public String input()
    {
        return INPUT;
    }

    public Object getModel()
    {
        return companyModel;
    }

    public void prepare()
        throws ProjectBuildingException, ArtifactMetadataRetrievalException, SettingsConfigurationException
    {
        configuration = appConfiguration.getConfiguration();

        CompanyPom companyPom = configuration.getCompanyPom();
        companyModel = companyPomHandler.getCompanyPomModel( companyPom, helper.getLocalRepository() );

        if ( companyModel == null )
        {
            companyModel = new Model();
            companyModel.setModelVersion( "4.0.0" );
            companyModel.setPackaging( "pom" );

            if ( companyPom != null )
            {
                companyModel.setGroupId( companyPom.getGroupId() );
                companyModel.setArtifactId( companyPom.getArtifactId() );
            }
        }
    }

    public Model getCompanyModel()
    {
        return companyModel;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION, Resource.GLOBAL );

        return bundle;
     }
}
