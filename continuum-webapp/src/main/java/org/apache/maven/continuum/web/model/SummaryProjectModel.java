package org.apache.maven.continuum.web.model;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SummaryProjectModel
{
    private int id = -1;
    private String name;
    private String version;
    private String projectGroupName;
    private int latestBuildId = -1;
    private int buildInSuccessId = -1;
    private int buildNumber = -1;
    private int state = -1;
    private boolean inQueue = false;

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public int getLatestBuildId()
    {
        return latestBuildId;
    }

    public void setLatestBuildId( int latestBuildId )
    {
        this.latestBuildId = latestBuildId;
    }

    public int getBuildInSuccessId()
    {
        return buildInSuccessId;
    }

    public void setBuildInSuccessId( int buildInSuccessId )
    {
        this.buildInSuccessId = buildInSuccessId;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber( int buildNumber )
    {
        this.buildNumber = buildNumber;
    }

    public int getState()
    {
        return state;
    }

    public void setState( int state )
    {
        this.state = state;
    }

    public boolean isInQueue()
    {
        return inQueue;
    }

    public void setInQueue( boolean inQueue )
    {
        this.inQueue = inQueue;
    }
}
