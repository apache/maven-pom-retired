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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;

import com.opensymphony.webwork.ServletActionContext;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="buildResult"
 */
public class BuildResultAction
    extends ContinuumActionSupport
    implements SecureAction
{
    private Project project;

    private BuildResult buildResult;

    private int buildId;

    private int projectId;

    private List changeSet;

    private boolean hasSurefireResults;

    private String buildOutput;

    private String state;

    private String projectGroupName = "";

    public String execute()
        throws ContinuumException, ConfigurationException, IOException
    {

        //todo get this working for other types of test case rendering other then just surefire
        // check if there are surefire results to display
        project = getContinuum().getProject( getProjectId() );
        hasSurefireResults = FileUtils.fileExists( project.getWorkingDirectory() + "/target/surefire-reports" );

        buildResult = getContinuum().getBuildResult( getBuildId() );

        changeSet = getContinuum().getChangesSinceLastSuccess( getProjectId(), getBuildId() );

        File buildOutputFile = getContinuum().getConfiguration().getBuildOutputFile( getBuildId(), getProjectId() );

        if ( buildOutputFile.exists() )
        {
            buildOutput = FileUtils.fileRead( buildOutputFile );
        }

        state = StateGenerator.generate( buildResult.getState(), ServletActionContext.getRequest().getContextPath() );


        return SUCCESS;
    }


    public int getBuildId()
    {
        return buildId;
    }

    public void setBuildId( int buildId )
    {
        this.buildId = buildId;
    }

    public Project getProject()
    {
        return project;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public List getChangesSinceLastSuccess()
    {
        return changeSet;
    }

    public boolean isHasSurefireResults()
    {
        return hasSurefireResults;
    }

    public void setHasSurefireResults( boolean hasSurefireResults )
    {
        this.hasSurefireResults = hasSurefireResults;
    }

    public String getBuildOutput()
    {
        return buildOutput;
    }

    public String getState()
    {
        return state;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if( projectGroupName == null || "".equals( projectGroupName ) )
        {               
            projectGroupName = getContinuum().getProjectGroupByProjectId( getProjectId() ).getName();
        }

        return projectGroupName;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        
        try
        {
            bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION,
                getProjectGroupName() );
        }
        catch ( ContinuumException e )
        {

        }

        return bundle;
    }

}
