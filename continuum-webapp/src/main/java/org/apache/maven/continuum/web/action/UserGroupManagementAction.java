package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.UserGroup;

import java.util.List;
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

/**
 * UserManagementAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id:$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="userGroupManagement"
 */
public class UserGroupManagementAction
    extends ContinuumActionSupport
{
    private int userGroupId;

    private String name;

    private String description;

    private List userGroups;

    public String summary()
    {
        try
        {
            userGroups = continuum.getUserGroups();
        }
        catch ( ContinuumException e )
        {
            addActionError( e.getMessage() );
            return ERROR;
        }

        return SUCCESS;
    }

    public String input()
    {
        if ( userGroupId != 0 )
        {
            try
            {
                UserGroup userGroup = continuum.getUserGroup( userGroupId );

                name = userGroup.getName();
                description = userGroup.getDescription();

            }
            catch ( ContinuumException e )
            {
                addActionError( e.getMessage() );
                return ERROR;
            }
        }
       return SUCCESS;
    }

    public String save()
    {
        if ( userGroupId == 0 )
        {
            UserGroup userGroup = new UserGroup();

            userGroup.setName( name );
            userGroup.setDescription( description );

            continuum.addUserGroup( userGroup );
        }
        else
        {
            try
            {
                UserGroup userGroup = continuum.getUserGroup( userGroupId );

                userGroup.setName( name );
                userGroup.setDescription( description );

                continuum.updateUserGroup( userGroup );
            }
            catch ( ContinuumException e )
            {
                addActionError( e.getMessage() );
                return ERROR;
            }
        }

        return SUCCESS;
    }

    public String remove()
    {
        try
        {
            continuum.removeUserGroup( userGroupId );
        }
        catch ( ContinuumException e )
        {
            addActionError( e.getMessage() );
            return ERROR;
        }

        return SUCCESS;
    }


    public int getUserGroupId()
    {
        return userGroupId;
    }

    public void setUserGroupId( int userGroupId )
    {
        this.userGroupId = userGroupId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List getUserGroups()
    {
        return userGroups;
    }

    public void setUserGroups( List userGroups )
    {
        this.userGroups = userGroups;
    }
}
