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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.xwork.action.PlexusActionSupport;
import org.codehaus.plexus.security.system.SecuritySession;
import org.codehaus.plexus.security.system.SecuritySystem;
import org.codehaus.plexus.security.system.SecuritySystemConstants;
import org.codehaus.plexus.security.authorization.AuthorizationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.opensymphony.xwork.Preparable;
import com.opensymphony.xwork.ActionContext;

/**
 * ContinuumActionSupport
 *
 * @author Jesse McConnell <jesse@codehaus.org>
 * @version $Id$
 */
public class ContinuumActionSupport
    extends PlexusActionSupport
    implements Preparable
{       
    private SecuritySession securitySession;

    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    protected static final String REQUIRES_AUTHENTICATION = "requires-authentication";

    protected static final String REQUIRES_AUTHORIZATION = "requires-authorization";

    protected static final String ERROR_MSG_AUTHORIZATION_REQUIRED = "You are not authorized to access this page. " +
        "Please contact your administrator to be granted the appropriate permissions.";

    protected static final String ERROR_MSG_PROCESSING_AUTHORIZATION = "An error occurred while performing authorization.";

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    public void prepare()
        throws Exception
    {
        if( securitySession == null )
        {
            securitySession = (SecuritySession) getContext().getSession().get( SecuritySystemConstants.SECURITY_SESSION_KEY );
        }
    }

    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }

    /**
     * Check if the current user is authorized to view the specified project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedViewProjectGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to add a project group
     *
     * @param resource the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedAddProjectGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to delete the specified project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedRemoveProjectGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )                           
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to build the specified project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedBuildProjectGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_BUILD_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_BUILD_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to modify the specified project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedModifyProjectGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to add a project to a specific project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedAddProjectToGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_TO_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_TO_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to delete a project from a specified group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedRemoveProjectFromGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_FROM_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_FROM_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to modify a project in the specified group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedModifyProjectInGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_IN_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_IN_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to build a project in the specified group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedBuildProjectInGroup( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_BUILD_PROJECT_IN_GROUP_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_BUILD_PROJECT_IN_GROUP_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to add a build definition for the specified
     * project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedAddGroupBuildDefinition( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_ADD_GROUP_BUILD_DEFINTION_OPERATION, resource ) )
                {
                   throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_ADD_GROUP_BUILD_DEFINTION_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to delete a build definition in the specified
     * project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedRemoveGroupBuildDefinition( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_BUILD_DEFINITION_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_BUILD_DEFINITION_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to modify a build definition in the specified
     * project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedModifyGroupBuildDefinition( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_BUILD_DEFINITION_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_BUILD_DEFINITION_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to add a group build definition to a specific
     * project
     *
     * @param resource the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedAddProjectBuildDefinition( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_BUILD_DEFINTION_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_BUILD_DEFINTION_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to modify a build definition of a specific project
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedModifyProjectBuildDefinition( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_BUILD_DEFINITION_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_BUILD_DEFINITION_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to delete a build definition of a specific
     * project
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedRemoveProjectBuildDefinition( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_BUILD_DEFINITION_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_BUILD_DEFINITION_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to add a notifier to the specified
     * project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedAddProjectGroupNotifier( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_ADD_GROUP_NOTIFIER_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_ADD_GROUP_NOTIFIER_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to delete a notifier in the specified
     * project group
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedRemoveProjectGroupNotifier( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_NOTIFIER_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_NOTIFIER_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to modify a notifier in the specified
     * project group
     *
     * @param resource  the operartion resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedModifyProjectGroupNotifier( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_NOTIFIER_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_NOTIFIER_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to add a notifier to a specific project
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedAddProjectNotifier( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_NOTIFIER_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_NOTIFIER_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to delete a notifier in a specific project
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedRemoveProjectNotifier( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_NOTIFIER_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_NOTIFIER_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to modify a notifier in a specific project
     *
     * @param resource  the operation resource
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedModifyProjectNotifier( String resource )
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( resource != null && !"".equals( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                        ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_NOTIFIER_OPERATION, resource ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
            else
            {
                 if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                         ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_NOTIFIER_OPERATION ) )
                {
                    throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
                }
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to manage the application's configuration
     *
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedManageConfiguration()
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                    ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION ) )
            {
                throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Check if the current user is authorized to manage the project build schedules
     *
     * @return
     * @throws AuthenticationRequiredException
     * @throws AuthorizationRequiredException
     */
    protected boolean isAuthorizedManageSchedules()
        throws AuthenticationRequiredException, AuthorizationRequiredException
    {
        if( !isAuthenticated() )
        {
            throw new AuthenticationRequiredException( "Authentication required." );
        }

        try
        {
            if ( !getSecuritySystem().isAuthorized( getSecuritySession(),
                                                    ContinuumRoleConstants.CONTINUUM_MANAGE_SCHEDULES ) )
            {
               throw new AuthorizationRequiredException( ERROR_MSG_AUTHORIZATION_REQUIRED );
            }
        }
        catch ( ComponentLookupException cle )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }
        catch ( AuthorizationException ae )
        {
            throw new AuthorizationRequiredException( ERROR_MSG_PROCESSING_AUTHORIZATION );
        }

        return true;
    }

    /**
     * Get the security session
     *
     * @return current SecuritySession
     */
    private SecuritySession getSecuritySession()
    {

        return securitySession;
    }

    /**
     * Get the action context
     *
     * @return action context
     */
    private ActionContext getContext()
    {

        return ActionContext.getContext();
    }

    /**
     * Get the security system
     *
     * @return
     * @throws ComponentLookupException
     */
    private SecuritySystem getSecuritySystem()
        throws ComponentLookupException
    {

        return securitySystem;
    }

    protected boolean requiresAuthentication()
    {
        return true;
    }

    /**
     * Check if the current user is already authenticated
     * 
     * @return
     */
    public boolean isAuthenticated()
    {
        if ( requiresAuthentication() )
        {
            if ( getSecuritySession() == null || !getSecuritySession().isAuthenticated() )
            {
                return false;
            }
        }

        return true;
    }
}
