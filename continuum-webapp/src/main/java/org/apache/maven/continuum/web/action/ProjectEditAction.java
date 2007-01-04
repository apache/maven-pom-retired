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
import org.apache.maven.continuum.model.project.Project;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="projectEdit"
 */
public class ProjectEditAction
    extends ContinuumActionSupport
{

    private Project project;

    private int projectId;

    private String name;

    private String version;

    private String scmUrl;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    public String save()
        throws ContinuumException
    {
        project = getProject( projectId );

        project.setName( name );

        project.setVersion( version );

        project.setScmUrl( scmUrl );

        project.setScmUsername( scmUsername );

        project.setScmPassword( scmPassword );

        project.setScmTag( scmTag );

        getContinuum().updateProject( project );

        return SUCCESS;
    }

    public String edit()
        throws ContinuumException
    {
        project = getProject( projectId );

        name = project.getName();

        version = project.getVersion();

        scmUrl = project.getScmUrl();

        scmUsername = project.getScmUsername();

        scmPassword = project.getScmPassword();

        scmTag = project.getScmTag();

        return SUCCESS;
    }

    private Project getProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProject( projectId );
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
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

    public Project getProject()
    {
        return project;
    }
}
