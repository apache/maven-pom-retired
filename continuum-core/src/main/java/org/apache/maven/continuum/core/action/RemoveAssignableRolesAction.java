package org.apache.maven.continuum.core.action;

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
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.rbac.profile.RoleProfileException;
import org.codehaus.plexus.rbac.profile.RoleProfileManager;

import java.util.Map;

/**
 * AddAssignableRolesAction:
 *
 * @author: Emmanuel Venisse <evenisse@apache.org>
 * @version: $Id$
 * @plexus.component role="org.codehaus.plexus.action.Action" role-hint="remove-assignable-roles"
 */
public class RemoveAssignableRolesAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement role-hint="continuum"
     */
    private RoleProfileManager roleManager;

    public void execute( Map context )
        throws ContinuumException, ContinuumStoreException
    {
        int projectGroupId = getProjectGroupId( context );

        ProjectGroup projectGroup = store.getProjectGroup( projectGroupId );

        try
        {
            roleManager.deleteDynamicRole( "continuum-group-project-administrator", projectGroup.getName() );

            roleManager.deleteDynamicRole( "continuum-group-developer", projectGroup.getName() );

            roleManager.deleteDynamicRole( "continuum-group-user", projectGroup.getName() );
        }
        catch ( RoleProfileException rpe )
        {
            rpe.printStackTrace();
            throw new ContinuumException( "error removing dynamic role for project " + projectGroup.getName(), rpe );
        }
    }
}
