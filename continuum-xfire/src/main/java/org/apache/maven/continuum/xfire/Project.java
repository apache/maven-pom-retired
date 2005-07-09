package org.apache.maven.continuum.xfire;

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

public class Project
{
    private String name;
    private String version;
    // private List developers;
    // private List notifiers;
    private String scmUrl;
    private String url;
     private String id;
    private String type;
    
    private String executable;
    private String targets;
    private String goals;
    
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    public String getExecutable()
    {
        return executable;
    }
    public void setExecutable(String executable)
    {
        this.executable = executable;
    }
    public String getGoals()
    {
        return goals;
    }
    public void setGoals(String goals)
    {
        this.goals = goals;
    }
    public String getTargets()
    {
        return targets;
    }
    public void setTargets(String targets)
    {
        this.targets = targets;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    /*
    public List getDevelopers()
    {
        return developers;
    }
    public void setDevelopers(List developers)
    {
        this.developers = developers;
    }
    */
    public String getScmUrl()
    {
        return scmUrl;
    }
    public void setScmUrl(String scmUrl)
    {
        this.scmUrl = scmUrl;
    }
    public String getVersion()
    {
        return version;
    }
    public void setVersion(String version)
    {
        this.version = version;
    }
}
