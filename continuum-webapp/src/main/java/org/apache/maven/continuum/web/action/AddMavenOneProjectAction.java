package org.apache.maven.continuum.web.action;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import com.opensymphony.xwork.ActionSupport;

/**
 * @author Nick Gonzalez
 * @version $Id$
 */
public class AddMavenOneProjectAction
    extends ActionSupport
{
    private Continuum continuum;

    private String m1PomUrl;

    private String m1PomFile;

    private String m1Pom = null;

    public String execute()
        throws IOException, MalformedURLException, ContinuumException
    {
		if ( !StringUtils.isEmpty( m1PomUrl ) )
		{
		    m1Pom = m1PomUrl;
		}
		else
		{

			URL url = new URL( "file:/" + m1PomFile );

			String content = IOUtil.toString( url.openStream() ); 

		    if ( !StringUtils.isEmpty( content ) )
		    {
		        m1Pom = url.toString();
		    }
		}

		if ( !StringUtils.isEmpty( m1Pom ) )
		{
			ContinuumProjectBuildingResult result = continuum.addMavenOneProject( m1Pom );

		    if( result.getWarnings().size() > 0 )
		    {
		    	addActionMessage( result.getWarnings().toArray().toString() );
		    }
		}

        return SUCCESS;
    }

    public String doDefault()
    {
        return INPUT;
    }

	public String getM1Pom()
	{
		return m1Pom;
	}

	public void setM1Pom(String pom)
	{
		m1Pom = pom;
	}

	public String getM1PomFile()
	{
		return m1PomFile;
	}

	public void setM1PomFile(String pomFile)
	{
		m1PomFile = pomFile;
	}

	public String getM1PomUrl()
	{
		return m1PomUrl;
	}

	public void setM1PomUrl(String pomUrl)
	{
		m1PomUrl = pomUrl;
	}
}
