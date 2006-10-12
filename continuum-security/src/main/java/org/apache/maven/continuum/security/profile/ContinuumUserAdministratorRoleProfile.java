package org.apache.maven.continuum.security.profile;

import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.rbac.profile.AbstractRoleProfile;

import java.util.ArrayList;
import java.util.List;
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
 * ContinuumSystemAdministratorRoleProfile:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.rbac.profile.RoleProfile"
 *   role-hint="continuum-user-administrator"
 */
public class ContinuumUserAdministratorRoleProfile
    extends AbstractRoleProfile
{

    public String getRoleName()
    {
        return ContinuumRoleConstants.USER_ADMINISTRATOR_ROLE;
    }

    public List getOperations()
    {
        List operations = new ArrayList();
        operations.add( ContinuumRoleConstants.CONTINUUM_MANAGE_USERS );

        return operations;
    }


    public boolean isAssignable()
    {
        return false;
    }
}
