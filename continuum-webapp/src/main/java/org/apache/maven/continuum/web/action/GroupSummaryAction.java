package org.apache.maven.continuum.web.action;

import org.codehaus.plexus.xwork.action.PlexusActionSupport;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.web.model.SummaryProjectModel;
import org.apache.maven.continuum.web.model.GroupSummaryModel;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
/*
 * Copyright 2005 The Codehaus.
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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="groupSummary"
 */
public class GroupSummaryAction
    extends PlexusActionSupport
{
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    

    private List groups;

    public String execute()
        throws Exception
    {
        try
        {
            groups = new ArrayList();

            Collection projectGroups = continuum.getAllProjectGroupsWithProjects();

            for ( Iterator j = projectGroups.iterator(); j.hasNext(); )
            {
                ProjectGroup projectGroup = (ProjectGroup) j.next();

                getLogger().info("GroupSummaryAction: building group " + projectGroup.getName() );

                GroupSummaryModel groupModel = new GroupSummaryModel();
                groupModel.setId( projectGroup.getId() );
                groupModel.setGroupId( projectGroup.getGroupId() );
                groupModel.setName( projectGroup.getName() );
                groupModel.setDescription( projectGroup.getDescription() );

                //TODO: Create a summary jpox request so code will be more simple and performance will be better
                Collection projects = projectGroup.getProjects();

                Map buildResults = continuum.getLatestBuildResults();

                Map buildResultsInSuccess = continuum.getBuildResultsInSuccess();

                List projectModels = new ArrayList();
                int numSuccesses = 0;
                int numFailures = 0;
                int numErrors = 0;

                for ( Iterator i = projects.iterator(); i.hasNext(); )
                {
                    Project project = (Project) i.next();

                    SummaryProjectModel model = new SummaryProjectModel();

                    getLogger().info("GroupSummaryAction: building project model " + project.getName() );

                    model.setId( project.getId() );

                    model.setName( project.getName() );

                    model.setVersion( project.getVersion() );

                    model.setProjectGroupName( project.getProjectGroup().getName() );

                    if ( continuum.isInBuildingQueue( project.getId() ) ||
                        continuum.isInCheckoutQueue( project.getId() ) )
                    {
                        model.setInQueue( true );
                    }
                    else
                    {
                        model.setInQueue( false );
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
                    getLogger().info( "GroupSummaryAction: adding model to group " + model.getName() );
                    projectModels.add( model );
                }

                groupModel.setNumSuccesses( numSuccesses );
                groupModel.setNumFailures( numFailures );
                groupModel.setNumErrors( numErrors );
                groupModel.setProjects( projectModels );
                getLogger().info( "GroupSummaryAction: adding group to groups list " + groupModel.getName() );
                groups.add( groupModel );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    public List getGroups()
    {
        return groups;
    }

}
