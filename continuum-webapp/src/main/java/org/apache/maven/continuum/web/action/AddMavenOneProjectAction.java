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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Nick Gonzalez
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="addMavenOneProject"
 */
public class AddMavenOneProjectAction
    extends ContinuumActionSupport
{

    private String m1PomUrl;

    private File m1PomFile;

    private String m1Pom = null;

    public String execute()
        throws ContinuumException
    {
        if ( !StringUtils.isEmpty( m1PomUrl ) )
        {
            m1Pom = m1PomUrl;
        }
        else
        {
            if ( m1PomFile != null )
            {
                try
                {
                    m1Pom = m1PomFile.toURL().toString();
                }
                catch ( MalformedURLException e )
                {
                    // if local file can't be converted to url it's an internal error
                    throw new RuntimeException( e );
                }
            }
            else
            {
                return INPUT;
            }
        }

        ContinuumProjectBuildingResult result = null;

        result = continuum.addMavenOneProject( m1Pom );

        if ( result.hasErrors() )
        {
            Iterator it = result.getErrors().iterator();

            while ( it.hasNext() )
            {
                addActionError( (String) it.next() );
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

    public void setM1Pom( String pom )
    {
        m1Pom = pom;
    }

    public File getM1PomFile()
    {
        return m1PomFile;
    }

    public void setM1PomFile( File pomFile )
    {
        m1PomFile = pomFile;
    }

    public String getM1PomUrl()
    {
        return m1PomUrl;
    }

    public void setM1PomUrl( String pomUrl )
    {
        m1PomUrl = pomUrl;
    }
}
