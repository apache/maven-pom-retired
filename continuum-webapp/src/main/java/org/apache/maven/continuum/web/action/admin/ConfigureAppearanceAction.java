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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.continuum.configuration.CompanyPom;
import org.apache.maven.continuum.configuration.Configuration;
import org.apache.maven.continuum.configuration.ConfigurationChangeException;
import org.apache.maven.continuum.configuration.ConfigurationStore;
import org.apache.maven.continuum.configuration.ConfigurationStoreException;
import org.apache.maven.continuum.configuration.InvalidConfigurationException;
import org.apache.maven.continuum.execution.maven.m2.MavenBuilderHelper;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.security.rbac.Resource;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: ConfigurationAction.java 480950 2006-11-30 14:58:35Z evenisse $
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="configureAppearance"
 */
public class ConfigureAppearanceAction
    extends ContinuumActionSupport
    implements ModelDriven, SecureAction
{
    /**
     * @plexus.requirement
     */
    private ConfigurationStore configurationStore;

    /**
     * The configuration.
     */
    private Configuration configuration;

    /**
     * The company POM.
     */
    private Model companyModel;

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper helper;

    /**
     * @plexus.requirement
     */
    private ArtifactMetadataSource artifactMetadataSource;

    public String execute()
        throws IOException, ConfigurationStoreException, InvalidConfigurationException, ConfigurationChangeException
    {
        configurationStore.storeConfiguration( configuration );

        return SUCCESS;
    }

    public Object getModel()
    {
        return configuration;
    }

    public void prepare()
        throws ConfigurationStoreException, ProjectBuildingException, ArtifactMetadataRetrievalException
    {
        configuration = configurationStore.getConfigurationFromStore();

        CompanyPom companyPom = configuration.getCompanyPom();
        if ( companyPom != null )
        {
            if ( StringUtils.isNotEmpty( companyPom.getGroupId() ) &&
                StringUtils.isNotEmpty( companyPom.getArtifactId() ) )
            {
                Artifact artifact = artifactFactory.createProjectArtifact( companyPom.getGroupId(),
                                                                           companyPom.getArtifactId(),
                                                                           Artifact.RELEASE_VERSION );

                ArtifactRepository localRepository = helper.getLocalRepository();

                List repositories =
                    projectBuilder.buildStandaloneSuperProject( localRepository ).getRemoteArtifactRepositories();
                List versions =
                    artifactMetadataSource.retrieveAvailableVersions( artifact, localRepository, repositories );

                if ( !versions.isEmpty() )
                {
                    Collections.sort( versions );

                    DefaultArtifactVersion artifactVersion =
                        (DefaultArtifactVersion) versions.get( versions.size() - 1 );
                    artifact = artifactFactory.createProjectArtifact( companyPom.getGroupId(),
                                                                      companyPom.getArtifactId(),
                                                                      artifactVersion.toString() );

                    MavenProject project =
                        projectBuilder.buildFromRepository( artifact, repositories, localRepository );

                    companyModel = project.getModel();
                }
                else
                {
                    addActionMessage( "Company POM '" + companyPom.getGroupId() + ":" + companyPom.getArtifactId() +
                        "' doesn't exist. Click on the 'Edit Company POM' link to create it." );
                }
            }
        }
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION, Resource.GLOBAL );

        return bundle;
    }

    public Model getCompanyModel()
    {
        return companyModel;
    }
}
