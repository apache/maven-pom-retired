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
import org.apache.maven.continuum.model.project.Project;

/**
 * @author Nick Gonzalez
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="addProject"
 */
public class AddProjectAction
    extends ContinuumActionSupport
{

    private String projectName;

    private String projectVersion;

    private String projectScmUrl;

    private String projectScmUsername;

    private String projectScmPassword;

    private String projectScmTag;

    private String projectType;

    public String execute()
        throws ContinuumException
    {
        Project project = new Project();

        project.setName( projectName );

        project.setVersion( projectVersion );

        project.setScmUrl( projectScmUrl );

        project.setScmUsername( projectScmUsername );

        project.setScmPassword( projectScmPassword );

        project.setScmTag( projectScmTag );

        getContinuum().addProject( project, projectType );

        return SUCCESS;
    }

    public String doDefault()
    {
        return INPUT;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getProjectScmPassword()
    {
        return projectScmPassword;
    }

    public void setProjectScmPassword( String projectScmPassword )
    {
        this.projectScmPassword = projectScmPassword;
    }

    public String getProjectScmTag()
    {
        return projectScmTag;
    }

    public void setProjectScmTag( String projectScmTag )
    {
        this.projectScmTag = projectScmTag;
    }

    public String getProjectScmUrl()
    {
        return projectScmUrl;
    }

    public void setProjectScmUrl( String projectScmUrl )
    {
        this.projectScmUrl = projectScmUrl;
    }

    public String getProjectScmUsername()
    {
        return projectScmUsername;
    }

    public void setProjectScmUsername( String projectScmUsername )
    {
        this.projectScmUsername = projectScmUsername;
    }

    public String getProjectType()
    {
        return projectType;
    }

    public void setProjectType( String projectType )
    {
        this.projectType = projectType;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    public void setProjectVersion( String projectVersion )
    {
        this.projectVersion = projectVersion;
    }
}
