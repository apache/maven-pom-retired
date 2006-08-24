package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.UserGroup;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
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
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="userManagement"
 */
public class UserManagementAction
    extends ContinuumActionSupport
{
    private int userId;

    private String username;

    private String fullName;

    private String password;

    private String passwordTwo;

    private String email;

    private int userGroupId;

    private List users;

    private Map userGroups;


    /**
     * initialize the userGroups map since it is used on a number of pages
     *
     * @throws Exception
     */
    public void prepare()
        throws Exception
    {
        super.prepare();

        if ( userGroups == null )
        {
            userGroups = new HashMap();

            for ( Iterator i = continuum.getUserGroups().iterator(); i.hasNext();)
            {
                UserGroup group = (UserGroup)i.next();
                userGroups.put( new Integer( group.getId() ), group.getName() );
            }
        }
    }

    public String summary()
    {
        try
        {
            users = continuum.getUsers();
        }
        catch ( ContinuumException e )
        {
            getLogger().info(e.getMessage());
            addActionError( e.getMessage() );
            return ERROR;
        }

        return SUCCESS;
    }

    public String input()
    {
        if ( userId != 0 )
        {
            try
            {
                ContinuumUser user = continuum.getUser( userId );

                username = user.getUsername();
                fullName = user.getFullName();
                email = user.getEmail();
                userGroupId = user.getGroup().getId();

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
        if ( userId == 0 )
        {
            try
            {
                ContinuumUser newUser = new ContinuumUser();

                newUser.setUsername( username );
                newUser.setFullName( fullName );
                newUser.setEmail( email );
                newUser.setPassword( password );
                newUser.setGroup( continuum.getUserGroup( userGroupId ) );
                continuum.addUser( newUser );
            }
            catch ( ContinuumException e )
            {
                addActionError( e.getMessage() );
                return ERROR;
            }
        }
        else
        {
            try
            {
                ContinuumUser editUser = continuum.getUser( userId );

                editUser.setUsername( username );
                editUser.setFullName( fullName );
                editUser.setEmail( email );
                editUser.setPassword( password );
                editUser.setGroup( continuum.getUserGroup( userGroupId ) );
                continuum.updateUser( editUser );

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
            continuum.removeUser( userId );
        }
        catch ( ContinuumException e )
        {
            addActionError( e.getMessage() );
            return ERROR;
        }

        return SUCCESS;
    }


    public int getUserId()
    {
        return userId;
    }

    public void setUserId( int userId )
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getPasswordTwo()
    {
        return passwordTwo;
    }

    public void setPasswordTwo( String passwordTwo )
    {
        this.passwordTwo = passwordTwo;
    }

    public List getUsers()
    {
        return users;
    }

    public void setUsers( List users )
    {
        this.users = users;
    }

    public int getUserGroupId()
    {
        return userGroupId;
    }

    public void setUserGroupId( int userGroupId )
    {
        this.userGroupId = userGroupId;
    }

    public Map getUserGroups()
    {
        return userGroups;
    }

    public void setUserGroups( Map userGroups )
    {
        this.userGroups = userGroups;
    }
}
