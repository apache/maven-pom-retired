package org.apache.maven.continuum.web.model;

import java.util.List;
import java.io.Serializable;

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
 * GroupSummaryModel:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 */
public class GroupSummaryModel
    implements Serializable
{
    /**
     * Field id
     */
    private int id;

    /**
     * Field groupId
     */
    private String groupId;


    /**
     * Field name
     */
    private String name;

    /**
     * Field description
     */
    private String description;

    /**
     * Field projects
     */
    private List projects;

    private int numSuccesses;

    private int numFailures;

    private int numErrors;

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }


    public List getProjects()
    {
        return projects;
    }

    public void setProjects( List projects )
    {
        this.projects = projects;
    }


    public int getNumSuccesses()
    {
        return numSuccesses;
    }

    public void setNumSuccesses( int numSuccesses )
    {
        this.numSuccesses = numSuccesses;
    }

    public int getNumFailures()
    {
        return numFailures;
    }

    public void setNumFailures( int numFailures )
    {
        this.numFailures = numFailures;
    }

    public int getNumErrors()
    {
        return numErrors;
    }

    public void setNumErrors( int numErrors )
    {
        this.numErrors = numErrors;
    }
}
