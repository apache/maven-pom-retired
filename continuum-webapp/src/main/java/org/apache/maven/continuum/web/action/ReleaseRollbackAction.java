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

import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.release.DefaultReleaseManagerListener;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="releaseRollback"
 */
public class ReleaseRollbackAction
    extends ContinuumActionSupport
    implements SecureAction
{
    private int projectId;

    private String releaseId;

    private String projectGroupName = "";

    public String execute()
        throws Exception
    {
        /*try
        {
            if ( isAuthorizedBuildProjectGroup( getProjectGroupName() ) )
            { */
                ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

                ContinuumReleaseManagerListener listener = new DefaultReleaseManagerListener();

                Project project = getContinuum().getProject( projectId );

                releaseManager.rollback( releaseId, project.getWorkingDirectory(), listener );

                //recurse until rollback is finished
                while( listener.getState() != ContinuumReleaseManagerListener.FINISHED )
                {
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch( InterruptedException e )
                    {
                        //do nothing
                    }
                }

                releaseManager.getPreparedReleases().remove( releaseId );
        /*    }
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException authnE )
        {
            return REQUIRES_AUTHENTICATION;
        } */

        return SUCCESS;
    }

    public String warn()
        throws Exception
    {
        /*try
        {
            if ( isAuthorizedBuildProjectGroup( getProjectGroupName() ) )
            {
                return SUCCESS;
            }
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException authnE )
        {
            return REQUIRES_AUTHENTICATION;
        } */
        
        return SUCCESS;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( projectGroupName == null || "".equals( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );

        try
        {
            bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_BUILD_PROJECT_IN_GROUP_OPERATION,
                getProjectGroupName() );
        }
        catch ( ContinuumException ce )
        {

        }

        return bundle;
    }

}
