package org.apache.maven.continuum.web.action;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.model.ProjectSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Used to render the list of projects in the project group page.
 * 
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action"
 * role-hint="summary"
 */
public class SummaryAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private String projectGroupName;

    private int nbSuccesses;

    private int nbFailures;

    private int nbErrors;

    private List summary;
    
    private List projects;

    public String execute()
        throws ContinuumException
    {
        Collection projectsInGroup;

        //TODO: Create a summary jpox request so code will be more simple and performance will be better
        projectsInGroup = getContinuum().getProjectsInGroup( projectGroupId );

        Map buildResults = getContinuum().getLatestBuildResults();

        Map buildResultsInSuccess = getContinuum().getBuildResultsInSuccess();

        summary = new ArrayList();

        for ( Iterator i = projectsInGroup.iterator(); i.hasNext(); )
        {
            Project project = (Project) i.next();

            ProjectSummary model = new ProjectSummary();

            model.setId( project.getId() );

            model.setName( project.getName() );

            model.setVersion( project.getVersion() );

            model.setProjectGroupName( project.getProjectGroup().getName() );

            if ( getContinuum().isInBuildingQueue( project.getId() ) || getContinuum().isInCheckoutQueue( project.getId() ) )
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
                nbSuccesses++;
            }
            else if ( project.getState() == 3 )
            {
                nbFailures++;
            }
            else if ( project.getState() == 4 )
            {
                nbErrors++;
            }

            model.setBuildNumber( project.getBuildNumber() );

            if ( buildResultsInSuccess != null )
            {
                BuildResult buildInSuccess = (BuildResult) buildResultsInSuccess.get( new Integer( project.getId() ) );

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

            summary.add( model );
        }

        return SUCCESS;
    }

    public int getNbSuccesses()
    {
        return nbSuccesses;
    }

    public int getNbFailures()
    {
        return nbFailures;
    }

    public int getNbErrors()
    {
        return nbErrors;
    }

    public List getProjects()
    {
        return summary;
    }

    public void setProjects( List projects )
    {
        this.projects = projects;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }


    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }
}
