package org.apache.maven.continuum.security.profile;

import org.codehaus.plexus.rbac.profile.AbstractRoleProfile;
import org.apache.maven.continuum.security.ContinuumRoleConstants;

import java.util.List;
import java.util.ArrayList;
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
 *   role-hint="continuum-group-administrator"
 */
public class ContinuumGroupAdministratorRoleProfile
    extends AbstractRoleProfile
{

    public String getRoleName()
    {
        return ContinuumRoleConstants.GROUP_ADMINISTRATOR_ROLE;
    }

    public List getOperations()
    {
        List operations = new ArrayList();
        operations.add( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION );
        operations.add( ContinuumRoleConstants.CONTINUUM_MANAGE_SCHEDULES );        

        return operations;
    }


    public boolean isAssignable()
    {
        return false;
    }
}
