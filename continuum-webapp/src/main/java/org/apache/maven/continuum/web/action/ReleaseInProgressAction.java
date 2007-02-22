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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.release.ContinuumReleaseManagerListener;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.shared.release.ReleaseResult;

/**
 * @author Edwin Punzalan
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="releaseInProgress"
 */
public class ReleaseInProgressAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    private String releaseGoal;

    private ContinuumReleaseManagerListener listener;

    private ReleaseResult result;

    private String projectGroupName = "";

    public String execute()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        String status = "";

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );

        if ( listener != null )
        {
            if ( listener.getState() == ContinuumReleaseManagerListener.LISTENING )
            {
                status = "inProgress";
            }
            else if ( listener.getState() == ContinuumReleaseManagerListener.FINISHED )
            {
                status = SUCCESS;
            }
            else
            {
                status = "initialized";
            }
        }
        else
        {
            throw new Exception( "There is no on-going or finished release operation with id " + releaseId );
        }

        return status;
    }

    public String viewResult()
        throws Exception
    {
        try
        {
            checkBuildProjectInGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().get( releaseId );

        if ( listener != null )
        {
            if ( listener.getState() == ContinuumReleaseManagerListener.FINISHED )
            {
                result = (ReleaseResult) releaseManager.getReleaseResults().get( releaseId );

                return SUCCESS;
            }
            else
            {
                throw new Exception( "The release operation with id " + releaseId + "has not finished yet." );
            }
        }
        else
        {
            throw new Exception( "There is no finished release operation with id " + releaseId );
        }
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public ContinuumReleaseManagerListener getListener()
    {
        return listener;
    }

    public void setListener( ContinuumReleaseManagerListener listener )
    {
        this.listener = listener;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getReleaseGoal()
    {
        return releaseGoal;
    }

    public void setReleaseGoal( String releaseGoal )
    {
        this.releaseGoal = releaseGoal;
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
}
