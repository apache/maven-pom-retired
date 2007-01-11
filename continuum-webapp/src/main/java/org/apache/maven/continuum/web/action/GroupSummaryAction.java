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

import com.opensymphony.xwork.ActionContext;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.model.GroupSummary;
import org.apache.maven.continuum.web.model.ProjectSummary;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.security.authorization.AuthorizationException;
import org.codehaus.plexus.security.system.SecuritySession;
import org.codehaus.plexus.security.system.SecuritySystem;
import org.codehaus.plexus.security.system.SecuritySystemConstants;
import org.codehaus.plexus.xwork.PlexusLifecycleListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="groupSummary"
 */
public class GroupSummaryAction
    extends ContinuumActionSupport
{
    private String infoMessage;

    private List groups;

    public String execute()
        throws ContinuumException
    {
        groups = new ArrayList();

        Collection projectGroups = getContinuum().getAllProjectGroupsWithProjects();

        for ( Iterator j = projectGroups.iterator(); j.hasNext(); )
        {
            ProjectGroup projectGroup = (ProjectGroup) j.next();

            if ( isAuthorized( projectGroup ) )
            {
                getLogger().debug( "GroupSummaryAction: building group " + projectGroup.getName() );

                GroupSummary groupModel = new GroupSummary();
                groupModel.setId( projectGroup.getId() );
                groupModel.setGroupId( projectGroup.getGroupId() );
                groupModel.setName( projectGroup.getName() );
                groupModel.setDescription( projectGroup.getDescription() );

                //TODO: Create a summary jpox request so code will be more simple and performance will be better
                Collection projects = projectGroup.getProjects();

                groupModel.setNumProjects( projects.size() );

                Map buildResults = getContinuum().getLatestBuildResults();

                Map buildResultsInSuccess = getContinuum().getBuildResultsInSuccess();

                List projectModels = new ArrayList();
                int numSuccesses = 0;
                int numFailures = 0;
                int numErrors = 0;

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    Project project = (Project) i.next();

                    if ( groupModel.getProjectType() == null )
                    {
                        groupModel.setProjectType( project.getExecutorId() );
                    }

                    ProjectSummary model = new ProjectSummary();

                    getLogger().debug( "GroupSummaryAction: building project model " + project.getName() );

                    model.setId( project.getId() );

                    model.setName( project.getName() );

                    model.setVersion( project.getVersion() );

                    model.setProjectGroupId( project.getProjectGroup().getId() );

                    model.setProjectGroupName( project.getProjectGroup().getName() );

                    if ( getContinuum().isInBuildingQueue( project.getId() ) )
                    {
                        model.setInBuildingQueue( true );
                    }
                    else if ( getContinuum().isInCheckoutQueue( project.getId() ) )
                    {
                        model.setInCheckoutQueue( true );
                    }
                    else
                    {
                        model.setInBuildingQueue( false );
                        model.setInCheckoutQueue( false );
                    }

                    model.setState( project.getState() );

                    if ( project.getState() == 2 )
                    {
                        numSuccesses++;
                    }
                    else if ( project.getState() == 3 )
                    {
                        numFailures++;
                    }
                    else if ( project.getState() == 4 )
                    {
                        numErrors++;
                    }

                    model.setBuildNumber( project.getBuildNumber() );

                    if ( buildResultsInSuccess != null )
                    {
                        BuildResult buildInSuccess =
                            (BuildResult) buildResultsInSuccess.get( new Integer( project.getId() ) );

                        if ( buildInSuccess != null )
                        {
                            model.setBuildInSuccessId( buildInSuccess.getId() );
                        }
                    }

                    if ( buildResults != null )
                    {
                        BuildResult latestBuild = (BuildResult) buildResults.get( new Integer( project.getId() ) );

                        if ( latestBuild != null )
                        {
                            model.setLatestBuildId( latestBuild.getId() );
                        }
                    }
                    getLogger().debug( "GroupSummaryAction: adding model to group " + model.getName() );
                    projectModels.add( model );
                }

                //todo wire in the next scheduled build for the project group and a meaningful status message
                //groupModel.setNextScheduledBuild( "unknown" );
                //groupModel.setStatusMessage( "none" );

                groupModel.setNumSuccesses( numSuccesses );
                groupModel.setNumFailures( numFailures );
                groupModel.setNumErrors( numErrors );
                groupModel.setProjects( projectModels );
                getLogger().debug( "GroupSummaryAction: adding group to groups list " + groupModel.getName() );
                groups.add( groupModel );
            }
        }

        return SUCCESS;
    }

    public List getGroups()
    {
        return groups;
    }


    public String getInfoMessage()
    {
        return infoMessage;
    }

    public void setInfoMessage( String infoMessage )
    {
        this.infoMessage = infoMessage;
    }

    private boolean isAuthorized( ProjectGroup projectGroup )
    {
        // do the authz bit
        ActionContext context = ActionContext.getContext();

        PlexusContainer container = (PlexusContainer) context.getApplication().get( PlexusLifecycleListener.KEY );
        SecuritySession securitySession =
            (SecuritySession) context.getSession().get( SecuritySystemConstants.SECURITY_SESSION_KEY );

        try
        {
            SecuritySystem securitySystem = (SecuritySystem) container.lookup( SecuritySystem.ROLE );

            if ( !securitySystem.isAuthorized( securitySession, ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION,
                                               projectGroup.getName() ) )
            {
                return false;
            }
        }
        catch ( ComponentLookupException cle )
        {
            return false;
        }
        catch ( AuthorizationException ae )
        {
            return false;
        }

        return true;
    }
}
