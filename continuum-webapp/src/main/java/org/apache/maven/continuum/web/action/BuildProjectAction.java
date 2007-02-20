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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="buildProject"
 */
public class BuildProjectAction
    extends ContinuumActionSupport
    implements SecureAction
{
    private int projectId;

    private int buildDefinitionId;

    private int projectGroupId;

    private boolean fromGroupPage = false;

    private boolean fromProjectPage = false;

    private String projectGroupName = "";

    public String execute()
        throws ContinuumException
    {            

        if ( projectId > 0 )
        {
            if ( buildDefinitionId > 0 )
            {
                getContinuum().buildProjectWithBuildDefinition( projectId, buildDefinitionId );
            }
            else
            {
                getContinuum().buildProject( projectId );
            }
        }
        else
        {
            if ( buildDefinitionId > 0 )
            {
                getContinuum().buildProjectsWithBuildDefinition( buildDefinitionId );
            }
            else
            {
                getContinuum().buildProjects();
            }
        }

        if ( projectId > 0 )
        {
            if ( fromGroupPage == true )
            {
                return "to_group_page";
            }
            if ( fromProjectPage == true )
            {
                return "to_project_page";
            }
        }


        return SUCCESS;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setBuildDefinitionId( int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public int getBuildDefinition()
    {
        return buildDefinitionId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public boolean isFromGroupPage()
    {
        return fromGroupPage;
    }

    public void setFromGroupPage( boolean fromGroupPage )
    {
        this.fromGroupPage = fromGroupPage;
    }

    public boolean isFromProjectPage()
    {
        return fromProjectPage;
    }

    public void setFromProjectPage( boolean fromProjectPage )
    {
        this.fromProjectPage = fromProjectPage;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if( projectGroupName == null || "".equals( projectGroupName ) )
        {
            if ( projectGroupId != 0 )
            {
                projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
            }
            else
            {
                projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
            }
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
            bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_BUILD_PROJECT_IN_GROUP_OPERATION,
                    getProjectGroupName() );
        }
        catch ( ContinuumException e )
        {
            throw new SecureActionException( e.getMessage() );
        }

        return bundle;
    }
}
