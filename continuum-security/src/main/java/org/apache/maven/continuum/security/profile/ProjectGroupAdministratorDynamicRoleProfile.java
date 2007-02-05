package org.apache.maven.continuum.security.profile;

import org.codehaus.plexus.rbac.profile.AbstractDynamicRoleProfile;
import org.codehaus.plexus.rbac.profile.RoleProfileException;
import org.codehaus.plexus.security.rbac.Role;
import org.codehaus.plexus.security.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.security.rbac.RbacManagerException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
/*
 * Copyright 2006 The Apache Software Foundation.
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
 * ProjectGroupDeveloperDynamicRoleProfile:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.rbac.profile.RoleProfile"
 *   role-hint="continuum-group-project-administrator"
 */
public class ProjectGroupAdministratorDynamicRoleProfile
    extends AbstractDynamicRoleProfile
{


    public String getRoleName( String string )
    {
        return ContinuumRoleConstants.CONTINUUM_PROJECT_ADMINISTRATOR_ROLE_PREFIX + ContinuumRoleConstants.DELIMITER + string;
    }

    public List getOperations()
    {
        List operations = new ArrayList();
        operations.add( ContinuumRoleConstants.USER_MANAGEMENT_ROLE_GRANT_OPERATION );
        return operations;
    }

    public List getGlobalOperations()
    {
        List operations = new ArrayList();
        operations.add( ContinuumRoleConstants.CONTINUUM_MANAGE_USERS );
        operations.add( ContinuumRoleConstants.USER_MANAGEMENT_USER_ROLE_OPERATION );
        return operations;
    }


    public List getDynamicChildRoles( String string )
    {
        return Collections.singletonList( ContinuumRoleConstants.CONTINUUM_PROJECT_DEVELOPER_ROLE_PREFIX + ContinuumRoleConstants.DELIMITER + string );
    }


    public boolean isAssignable()
    {
        return true;
    }


    public Role getRole( String resource )
         throws RoleProfileException
    {
         try
         {
             if ( rbacManager.roleExists( getRoleName( resource ) ) )
             {
                 return rbacManager.getRole( getRoleName( resource ) );
             }
             else
             {
                 // first time assign the role to the group administrator since they need the access
                 Role newRole = generateRole( resource );

                 Role groupAdmin = rbacManager.getRole( ContinuumRoleConstants.GROUP_ADMINISTRATOR_ROLE );
                 groupAdmin.addChildRoleName( newRole.getName() );
                 rbacManager.saveRole( groupAdmin );

                 return newRole;
             }
         }
         catch ( RbacObjectNotFoundException ne )
         {
             throw new RoleProfileException( "unable to get role", ne );
         }
         catch ( RbacManagerException e )
         {
             throw new RoleProfileException( "system error with rbac manager", e );
         }
      }
}
