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

import com.opensymphony.xwork.Validateable;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;

import java.util.Iterator;

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
    implements Validateable, SecureAction
{

    private String projectName;

    private String projectVersion;

    private String projectScmUrl;

    private String projectScmUsername;

    private String projectScmPassword;

    private String projectScmTag;

    private String projectType;

    public void validate()
    {
        boolean projectNameAlreadyExist = false;
        Iterator iterator;
        Project project;

        clearErrorsAndMessages();
        try
        {
            iterator = getContinuum().getProjects().iterator();
            while ( iterator.hasNext() )
            {
                project = (Project) iterator.next();
                if ( project.getName().equalsIgnoreCase( projectName ) )
                {
                    projectNameAlreadyExist = true;
                    break;
                }
            }
            if ( projectNameAlreadyExist == true )
            {
                addActionError( "projectName.already.exist.error" );
            }
        }
        catch ( ContinuumException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String add()
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

    public String input()
    {
        return SUCCESS;
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


    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION );

        return bundle;
    }
}
