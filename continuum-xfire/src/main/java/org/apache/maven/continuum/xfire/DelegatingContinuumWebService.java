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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.AntProject;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenOneProject;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.project.ShellProject;
import org.apache.maven.continuum.scm.ScmFile;
import org.codehaus.xfire.fault.XFireFault;

public class DelegatingContinuumWebService
    implements ContinuumWebService
{
    Continuum continuum; 

    public void checkoutProject(String id)
        throws XFireFault
    {
        try
        {
            continuum.checkoutProject(id);
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e.getCause());
        }
    }

    public String addProject(Project projectInfo)
        throws XFireFault
    {
        try
        {
            if (projectInfo.getType().equals("maven-one"))
            {
                MavenOneProject project = new MavenOneProject();
                project.setGoals(projectInfo.getGoals());
                
                convertToLocal(projectInfo, project);

                return continuum.addMavenOneProject(project);
            }
            else if (projectInfo.getType().equals("maven-two"))
            {
                MavenTwoProject project = new MavenTwoProject();
                project.setGoals(projectInfo.getGoals());
                
                convertToLocal(projectInfo, project);
                
                return continuum.addMavenTwoProject(project);
            }
            else if (projectInfo.getType().equals("shell"))
            {
                ShellProject project = new ShellProject();
                project.setExecutable(projectInfo.getExecutable());
                
                convertToLocal(projectInfo, project);
                
                return continuum.addShellProject(project);
            }
            else if (projectInfo.getType().equals("ant"))
            {
                AntProject project = new AntProject();
                project.setExecutable(projectInfo.getExecutable());
                project.setTargets(projectInfo.getTargets());
                
                convertToLocal(projectInfo, project);
                
                return continuum.addAntProject(project);
            }
            else
            {
                throw new XFireFault("Invalid project type: " + projectInfo.getType() + 
                                     ". Must be maven-one, maven-two, shell, or ant.", 
                                     XFireFault.SENDER);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new XFireFault(e);
        }
    }

    private void convertToLocal(Project info, ContinuumProject project)
        throws XFireFault
    {
        project.setName(info.getName());
        project.setVersion(info.getVersion());
        project.setScmUrl(info.getScmUrl());
        project.setUrl(info.getUrl());
    }

    public void buildProject(String projectId, boolean force)
        throws XFireFault
    {
        try
        {
            continuum.buildProject(projectId, force);
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }

    public Collection getBuilds(String projectId) throws XFireFault
    {
        try
        {
            Collection localBuilds = continuum.getBuildsForProject(projectId);
            ArrayList builds = new ArrayList();
            
            for (Iterator itr = localBuilds.iterator(); itr.hasNext();)
            {
                builds.add(convertToRemote((ContinuumBuild) itr.next()));
            }
            
            return builds;
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }


    public CheckOutScmResult getCheckOutScmResult(String projectId)
        throws XFireFault
    {
        try
        {
            org.apache.maven.continuum.scm.CheckOutScmResult localCSR = 
                continuum.getCheckOutScmResultForProject(projectId);
            
            if (localCSR == null) return null;
            
            return convertToRemote(localCSR);
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }

    private CheckOutScmResult convertToRemote(org.apache.maven.continuum.scm.CheckOutScmResult localUSR)
    {
        CheckOutScmResult result = new CheckOutScmResult();
        result.setCommandOutput(localUSR.getCommandOutput());
        result.setProviderMessage(localUSR.getProviderMessage());
        result.setSuccess(localUSR.isSuccess());
        
        ArrayList files = new ArrayList();
        for (Iterator itr = localUSR.getCheckedOutFiles().iterator(); itr.hasNext();)
        {
            ScmFile file = (ScmFile) itr.next();
            files.add(file.getPath());
        }
        result.setCheckedOutFiles(files);
        
        return result;
    }

    public Build getLatestBuild(String projectId) throws XFireFault
    {
        try
        {
            ContinuumBuild build = continuum.getLatestBuildForProject(projectId);
            
            return convertToRemote(build);
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }


    private Build convertToRemote(ContinuumBuild build)
    {
        Build remBuild = new Build();
        remBuild.setEndTime(new Date(build.getEndTime()));
        remBuild.setStartTime(new Date(build.getStartTime()));
        remBuild.setError(build.getError());
        remBuild.setExitCode(build.getExitCode());
        remBuild.setForced(build.isForced());
        remBuild.setId(build.getId());
        remBuild.setStandardError(build.getStandardError());
        remBuild.setStandardOutput(build.getStandardOutput());
        remBuild.setState(build.getState());
        remBuild.setUpdateScmResult(convertToRemote(build.getUpdateScmResult()));
        return remBuild;
    }

    private UpdateScmResult convertToRemote(org.apache.maven.continuum.scm.UpdateScmResult localUSR)
    {
        UpdateScmResult result = new UpdateScmResult();
        result.setCommandOutput(localUSR.getCommandOutput());
        result.setProviderMessage(localUSR.getProviderMessage());
        result.setSuccess(localUSR.isSuccess());
        
        ArrayList files = new ArrayList();
        for (Iterator itr = localUSR.getUpdatedFiles().iterator(); itr.hasNext();)
        {
            ScmFile file = (ScmFile) itr.next();
            files.add(file.getPath());
        }
        result.setUpdatedFiles(files);
        
        return result;
    }

    public Project getProject(String projectId)
        throws XFireFault
    {
        try
        {
            ContinuumProject project = continuum.getProject(projectId);
            return createProjectInfo(project);
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }

    private Project createProjectInfo(ContinuumProject project)
        throws XFireFault
    {
        Project projectInfo = new Project();
        projectInfo.setId(project.getId());
        
        convertToProjectInfo(project, projectInfo);
        
        return projectInfo;
    }
    
    private void convertToProjectInfo(ContinuumProject project, Project projectInfo)
        throws XFireFault
    {
        if (project instanceof MavenOneProject)
        {
            projectInfo.setGoals(((MavenOneProject)project).getGoals());
            projectInfo.setType("maven-one");
        }
        else if (project instanceof MavenTwoProject)
        {
            projectInfo.setGoals(((MavenTwoProject)project).getGoals());
            projectInfo.setType("maven-two");
        }
        else if (project instanceof ShellProject)
        {
            projectInfo.setExecutable(((ShellProject)project).getExecutable());
            projectInfo.setType("shell");
        }
        else if (project instanceof AntProject)
        {
            projectInfo.setExecutable(((AntProject)project).getExecutable());
            projectInfo.setTargets(((AntProject)project).getTargets());
            projectInfo.setType("ant");
        }
        else
        {
            throw new XFireFault("Invalid project type for id " + project.getId(), 
                                 XFireFault.SENDER);
        }

        projectInfo.setName(project.getName());
        projectInfo.setVersion(project.getVersion());
        projectInfo.setScmUrl(project.getScmUrl());
        projectInfo.setUrl(project.getUrl());
    }

    public Collection getProjects() throws XFireFault
    {
        try
        {
            Collection projects = continuum.getProjects();
            
            List infos = new ArrayList();
            for (Iterator itr = projects.iterator(); itr.hasNext();)
            {
                infos.add(createProjectInfo((ContinuumProject) itr.next()));
            }
            return infos;
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }


    public void removeProject(String projectId)
        throws XFireFault
    {
        try
        {
            continuum.removeProject(projectId);
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }


    public void updateProject(Project projectInfo)
        throws XFireFault
    {
        try
        {
            ContinuumProject project = continuum.getProject(projectInfo.getId());
            
            convertToLocal(projectInfo, project);
            
            if (project instanceof MavenOneProject)
            {
                continuum.updateMavenOneProject((MavenOneProject)project);
            }
            else if (project instanceof MavenTwoProject)
            {
                continuum.updateMavenTwoProject((MavenTwoProject)project);
            }
            else if (project instanceof ShellProject)
            {
                continuum.updateShellProject((ShellProject)project);
            }
            else if (project instanceof AntProject)
            {
                continuum.updateAntProject((AntProject)project);
            }
        }
        catch (ContinuumException e)
        {
            throw new XFireFault(e);
        }
    }
}
