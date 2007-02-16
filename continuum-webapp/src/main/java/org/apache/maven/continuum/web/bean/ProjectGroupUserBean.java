package org.apache.maven.continuum.web.bean;

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

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.security.user.User;
import org.codehaus.plexus.security.rbac.Role;

import java.util.Collection;
import java.util.Iterator;

public class ProjectGroupUserBean
{
    public static final String ROLE_ADMINISTRATOR = "Group Project Administrator";
    
    public static final String ROLE_DEVELOPER = "Project Developer";
    
    public static final String ROLE_USER = "Project User";
    
    private User user;
    
    private ProjectGroup projectGroup;
    
    private Collection roles;

    /*
     * these booleans should be set on the addition of roles to this bean, see setRoles and addRole 
     */
    boolean isAdministrator = false;

    boolean isDeveloper = false;

    boolean isUser = false;
    
    public boolean isAdministrator()
    {
       return isAdministrator;
    }

    public boolean isDeveloper()
    {
       return isDeveloper;
    }

    public boolean isUser()
    {
       return isUser;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public void addRole( String role )
    {
        roles.add( role );

        if ( role.indexOf( ROLE_ADMINISTRATOR ) != -1 )
        {
            isAdministrator = true;
        }

        if ( role.indexOf( ROLE_DEVELOPER ) != -1 )
        {
            isDeveloper = true;
        }

        if ( role.indexOf( ROLE_USER ) != -1 )
        {
            isUser = true;
        }        
    }
    
    public Collection getRoles()
    {
        return roles;
    }

    public void setRoles( Collection roles )
    {
        this.roles = roles;

        for ( Iterator i = roles.iterator(); i.hasNext(); )
        {
            Role role = (Role)i.next();

            if ( role.getName().indexOf( ROLE_ADMINISTRATOR ) != -1 )
            {
                isAdministrator = true;
            }

            if ( role.getName().indexOf( ROLE_DEVELOPER ) != -1 )
            {
                isDeveloper = true;
            }

            if ( role.getName().indexOf( ROLE_USER ) != -1 )
            {
                isUser = true;
            }
        }
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }
    
    public String getUsername()
    {
        return user.getUsername();
    }
    
    public String getUserFullName()
    {
        return user.getFullName();
    }
    
    public String toString()
    {
        return user.getUsername() + ": " + roles + ": "+(isAdministrator()?"A":"-") + (isDeveloper()?"D":"-") + (isUser()?"U":"-"); 
    }

}
