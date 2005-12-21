package org.apache.maven.continuum.web.action;

import java.util.Map;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionSupport;

public class AddProjectAction extends ActionSupport{
    
	private Continuum continuum;
    
    private Project project;
    
    private String projectName;
    
    private String projectVersion;
    
    private String projectScmUrl;
    
    private String projectScmUsername;
    
    private String projectScmPassword;
    
    private String projectScmTag;

    private String projectType;
	
    /*public void addParam(String name, Object value) {
    	System.out.println(name+" = "+value);
    	getParams().put(name,value);
	}*/

	public String execute() throws Exception {
        project = new Project();
        project.setName(projectName);
        project.setVersion(projectVersion);
        project.setScmUrl(projectScmUrl);
        project.setScmUsername(projectScmUsername);
        project.setScmPassword(projectScmPassword);
        project.setScmTag(projectScmTag);
        
        continuum.addProject(project, projectType);
        return SUCCESS;
	}

	public String doDefault()
    {
		setProjectType(ServletActionContext.getRequest().getParameter("projectType"));		
        return INPUT;
    }

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectScmPassword() {
		return projectScmPassword;
	}

	public void setProjectScmPassword(String projectScmPassword) {
		this.projectScmPassword = projectScmPassword;
	}

	public String getProjectScmTag() {
		return projectScmTag;
	}

	public void setProjectScmTag(String projectScmTag) {
		this.projectScmTag = projectScmTag;
	}

	public String getProjectScmUrl() {
		return projectScmUrl;
	}

	public void setProjectScmUrl(String projectScmUrl) {
		this.projectScmUrl = projectScmUrl;
	}

	public String getProjectScmUsername() {
		return projectScmUsername;
	}

	public void setProjectScmUsername(String projectScmUsername) {
		this.projectScmUsername = projectScmUsername;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getProjectVersion() {
		return projectVersion;
	}

	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}

}
