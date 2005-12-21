package org.apache.maven.continuum.web.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import com.opensymphony.xwork.ActionSupport;

public class AddMavenOneProjectAction extends ActionSupport {
    private Continuum continuum;

    private String m1PomUrl;

    private String m1PomFile;

    private String m1Pom = null;

    public String execute() throws IOException, MalformedURLException, ContinuumException
    {
			System.out.println("inside");

			if ( !StringUtils.isEmpty( m1PomUrl ) )
			{
			    m1Pom = m1PomUrl;
			}
			else
			{

				URL url = new URL( "file:/"+m1PomFile);
				//URL m2PomUrl = getM2PomFileUrl();
				String content = IOUtil.toString( url.openStream() ); 
					
			    if ( !StringUtils.isEmpty( content ) )
			    {
			        m1Pom = url.toString();
			    }
			}

			if ( !StringUtils.isEmpty( m1Pom ) )
			{
				if (continuum == null ) System.out.println("shet!!!");
				ContinuumProjectBuildingResult result = continuum.addMavenOneProject( m1Pom );
			    
			    if(result.getWarnings().size() > 0) {
			    	addActionMessage(result.getWarnings().toArray().toString());
			    }
			}

			System.out.println("m2Pom="+m1Pom);
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

	public String getM1Pom() {
		return m1Pom;
	}

	public void setM1Pom(String pom) {
		m1Pom = pom;
	}

	public String getM1PomFile() {
		return m1PomFile;
	}

	public void setM1PomFile(String pomFile) {
		m1PomFile = pomFile;
	}

	public String getM1PomUrl() {
		return m1PomUrl;
	}

	public void setM1PomUrl(String pomUrl) {
		m1PomUrl = pomUrl;
	}


}
