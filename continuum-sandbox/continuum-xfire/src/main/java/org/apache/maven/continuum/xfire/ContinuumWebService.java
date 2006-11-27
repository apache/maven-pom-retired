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
import java.util.Collection;

import org.codehaus.xfire.fault.XFireFault;

import org.codehaus.xfire.annotations.commons.*;

/**
 * @@WebService(name = "Continuum", targetNamespace = "http://continuum.maven.apache.org/")
 */
public interface ContinuumWebService
{
    String ROLE = ContinuumWebService.class.getName();

    /**
     * @@WebMethod()
     * @@.projectId WebParam("ProjectId")
     * @@.return WebResult("Project")
     */ 
    Project getProject(String projectId) throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.projectInfo WebParam("Project")
     */ 
    void updateProject(Project projectInfo) throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.project WebParam("MetadataUrl")
     * @@.return WebResult("Projects")
     */ 
    Collection addMavenTwoProject(String scmUrl) throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.project WebParam("MetadataUrl")
     * @@.return WebResult("Projects")
     */ 
    Collection addMavenOneProject(String scmUrl) throws XFireFault;

    /**
     * @@WebMethod()
     * @@.project WebParam("Project")
     * @@.return WebResult("ProjectId")
     */ 
    String addProject(Project project) throws XFireFault;

    /**
     * @@WebMethod()
     * @@.projectId WebParam("ProjectId")
     * @@.return WebResult("Builds")
     */ 
    Collection getBuilds(String projectId) throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.return WebResult("Projects")
     */ 
    Collection getProjects() throws XFireFault;
    
    /**
     * @@WebMethod()
     * @throws XFireFault 
     * @@.projectId WebParam("ProjectId")
     * @@.return WebResult("Build")
     */ 
    Build getLatestBuild( String projectId ) throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.projectId WebParam("ProjectId")
     */
    void removeProject( String projectId )
        throws XFireFault;

    /**
     * @@WebMethod()
     * @@.projectId WebParam("ProjectId")
     * @@.force WebResult("Force")
     */ 
    void buildProject( String projectId, boolean force )
        throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.id WebParam("ProjectId")
     */
    void checkoutProject( String id )
        throws XFireFault;
    
    /**
     * @@WebMethod()
     * @@.projectId WebParam("ProjectId")
     * @@.return WebResult(name="ScmResult", targetNamespace="http://continuum.maven.apache.org")
     */
    ScmResult getScmResult( String projectId )
        throws XFireFault;

}
