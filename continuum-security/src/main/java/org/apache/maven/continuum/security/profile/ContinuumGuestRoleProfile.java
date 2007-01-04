package org.apache.maven.continuum.security.profile;

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

import org.codehaus.plexus.rbac.profile.AbstractRoleProfile;
import org.apache.maven.continuum.security.ContinuumRoleConstants;

import java.util.List;
import java.util.ArrayList;

/**
 * ContinuumSystemAdministratorRoleProfile:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.rbac.profile.RoleProfile"
 *   role-hint="continuum-guest"
 */
public class ContinuumGuestRoleProfile
    extends AbstractRoleProfile
{

    public String getRoleName()
    {
        return ContinuumRoleConstants.GUEST_ROLE;
    }

    public List getOperations()
    {
        List operations = new ArrayList();

        operations.add(  ContinuumRoleConstants.CONTINUUM_ACTIVE_GUEST_OPERATION );

        return operations;
    }


    public boolean isAssignable()
    {
        return false;
    }
}
