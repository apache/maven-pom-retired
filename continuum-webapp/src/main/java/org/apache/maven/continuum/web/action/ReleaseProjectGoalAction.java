package org.apache.maven.continuum.web.action;

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

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;

/**
 * @author Edwin Punzalan
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="releaseProjectGoal"
 */
public class ReleaseProjectGoalAction
    extends ContinuumActionSupport
{
    private int projectId;

    private int projectGroupId;

    private String projectName;

    private String preparedReleaseName;

    private String preparedReleaseId;

    private String projectGroupName = "";

    public String execute()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        Project project = getContinuum().getProjectWithAllDetails( projectId );

        String releaseId = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        Map preparedReleases = releaseManager.getPreparedReleases();
        if ( preparedReleases.containsKey( releaseId ) )
        {
            ReleaseDescriptor descriptor = (ReleaseDescriptor) preparedReleases.get( releaseId );

            preparedReleaseName = descriptor.getReleaseVersions().get( releaseId ).toString();

            preparedReleaseId = releaseId;
        }

        projectName = project.getName();

        return SUCCESS;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getPreparedReleaseName()
    {
        return preparedReleaseName;
    }

    public void setPreparedReleaseName( String preparedReleaseName )
    {
        this.preparedReleaseName = preparedReleaseName;
    }

    public String getPreparedReleaseId()
    {
        return preparedReleaseId;
    }

    public void setPreparedReleaseId( String preparedReleaseId )
    {
        this.preparedReleaseId = preparedReleaseId;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }
}
