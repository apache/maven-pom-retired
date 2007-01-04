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

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="releaseCleanup"
 */
public class ReleaseCleanupAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    public String execute()
        throws Exception
    {
        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        releaseManager.getReleaseResults().remove( releaseId );

        ContinuumReleaseManagerListener listener;

        listener = (ContinuumReleaseManagerListener) releaseManager.getListeners().remove( releaseId );

        if ( listener != null )
        {
            String goal = listener.getGoalName();

            return goal + "Finished";
        }
        else
        {
            throw new Exception( "No listener to cleanup for id " + releaseId );
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

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }
}
