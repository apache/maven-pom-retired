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

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="releaseRollback"
 */
public class ReleaseRollbackAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String releaseId;

    public String execute()
        throws Exception
    {
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

        return SUCCESS;
    }

    public String warn()
        throws Exception
    {
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
}
