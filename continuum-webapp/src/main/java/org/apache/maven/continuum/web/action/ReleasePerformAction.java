package org.apache.maven.continuum.web.action;

/*
 * Copyright 2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;

import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

import java.io.File;

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="releasePerform"
 */
public class ReleasePerformAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private String goals;

    private boolean useReleaseProfile;

    private ContinuumReleaseManagerListener listener;

    private ReleaseResult result;

    public String inputFromScm()
        throws Exception
    {
        populateFromProject();

        releaseId = "";

        return SUCCESS;
    }

    public String input()
        throws Exception
    {
        return SUCCESS;
    }

    public String execute()
        throws Exception
    {
        listener = new DefaultReleaseManagerListener();

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        //todo should be configurable
        File performDirectory = new File( getContinuum().getConfiguration().getWorkingDirectory(),
                                          "releases-" + System.currentTimeMillis() );
        performDirectory.mkdirs();

        releaseManager.perform( releaseId, performDirectory, goals, useReleaseProfile, listener );

        return SUCCESS;
    }

    public String executeFromScm()
        throws Exception
    {
        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        ReleaseDescriptor descriptor = new ReleaseDescriptor();
        descriptor.setScmSourceUrl( scmUrl );
        descriptor.setScmUsername( scmUsername );
        descriptor.setScmReleaseLabel( scmTag );
        descriptor.setScmTagBase( scmTagBase );

        String releaseId;
        do
        {
            releaseId = String.valueOf( System.currentTimeMillis() );
        }while ( releaseManager.getPreparedReleases().containsKey( releaseId ) );

        releaseManager.getPreparedReleases().put( releaseId, descriptor );

        return execute();
    }

    private void populateFromProject()
        throws Exception
    {
        Project project = getContinuum().getProjectWithAllDetails( projectId );

        scmUrl = project.getScmUrl();
        scmUsername = project.getScmUsername();
        scmPassword = project.getScmPassword();

        if ( scmUrl.startsWith( "scm:svn:" ) )
        {
            scmTagBase = new SvnScmProviderRepository( scmUrl, scmUsername, scmPassword ).getTagBase();
        }
        else
        {
            scmTagBase = "";
        }

        releaseId = "";
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public void setScmUrl( String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public String getScmTagBase()
    {
        return scmTagBase;
    }

    public void setScmTagBase( String scmTagBase )
    {
        this.scmTagBase = scmTagBase;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public boolean isUseReleaseProfile()
    {
        return useReleaseProfile;
    }

    public void setUseReleaseProfile( boolean useReleaseProfile )
    {
        this.useReleaseProfile = useReleaseProfile;
    }

    public ContinuumReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( ContinuumReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }
}
