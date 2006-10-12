package org.apache.maven.continuum.web.checks.security;

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

import org.codehaus.plexus.security.system.check.EnvironmentCheck;
import org.codehaus.plexus.rbac.profile.RoleProfileManager;
import org.codehaus.plexus.rbac.profile.RoleProfileException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.List;

/**
 * RoleProfileEnvironmentCheck:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.security.system.check.EnvironmentCheck"
 *   role-hint="continuum-role-profile-check"
 */
public class RoleProfileEnvironmentCheck
    extends AbstractLogEnabled
    implements EnvironmentCheck
{
    /**
     * @plexus.requirement role-hint="continuum"
     */
    private RoleProfileManager continuumRoleManager;

    public void validateEnvironment( List list )
    {
        try
        {
            if ( !continuumRoleManager.isInitialized() )
            {
                continuumRoleManager.initialize();
            }
        }
        catch ( RoleProfileException rpe )
        {
            rpe.printStackTrace();
            list.add( "error inititalizing the continuum role manager" );
        }
    }
}
