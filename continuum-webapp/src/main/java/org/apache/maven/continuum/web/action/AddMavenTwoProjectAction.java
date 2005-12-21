package org.apache.maven.continuum.web.action;


import com.opensymphony.xwork.ActionSupport;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;


public class AddMavenTwoProjectAction 
    extends ActionSupport  
    {
    private Continuum continuum;

    private String m2PomUrl;

    private String m2PomFile;

    private String m2Pom = null;

    public String execute() throws IOException, MalformedURLException, ContinuumException
    {
			System.out.println("inside");

			if ( !StringUtils.isEmpty( m2PomUrl ) )
			{
			    m2Pom = m2PomUrl;
			}
			else
			{

				URL url = new URL( "file:/"+m2PomFile);
				//URL m2PomUrl = getM2PomFileUrl();
				String content = IOUtil.toString( url.openStream() ); 
					
			    if ( !StringUtils.isEmpty( content ) )
			    {
			        m2Pom = url.toString();
			    }
			}

			if ( !StringUtils.isEmpty( m2Pom ) )
			{
				if (continuum == null ) System.out.println("shet!!!");
				ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( m2Pom );
			    
			    if(result.getWarnings().size() > 0) {
			    	addActionMessage(result.getWarnings().toArray().toString());
			    }
			}

			System.out.println("m2Pom="+m2Pom);
			Collection coll =  continuum.getProjects();
			Iterator iter = coll.iterator();
			
			while (iter.hasNext()) {
				
				Project proj = (Project)iter.next();
				System.out.println("project "+proj.getId()+": "+proj.getName());
			}
        return SUCCESS;
    }

    public String doDefault()
    {
        return INPUT;
    }

	public void setM2Pom(String pom) {
		m2Pom = pom;
	}

	public String getM2PomFile() {
		return m2PomFile;
	}

	public void setM2PomFile(String pomFile){
			m2PomFile = pomFile;
	}

	public String getM2PomUrl() {
		return m2PomUrl;
	}

	public void setM2PomUrl(String pomUrl) {
		m2PomUrl = pomUrl;
	}
}
