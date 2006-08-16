package org.apache.maven.continuum.web.action;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
 * Action to add a Maven project to Continuum, either Maven 1 or Maven 2.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class AddMavenProjectAction
    extends ContinuumActionSupport
{

    private static final long serialVersionUID = -3965565189557706469L;

    private String pomUrl;

    private File pomFile;

    private String pom = null;

    public String execute()
        throws ContinuumException
    {
        if ( !StringUtils.isEmpty( pomUrl ) )
        {
            pom = pomUrl;
        }
        else
        {
            if ( pomFile != null )
            {
                try
                {
                    pom = pomFile.toURL().toString();
                }
                catch ( MalformedURLException e )
                {
                    // if local file can't be converted to url it's an internal error
                    throw new RuntimeException( e );
                }
            }
            else
            {
                // no url or file was filled
                addActionError( "add.project.field.required.error" );
                return INPUT;
            }
        }

        ContinuumProjectBuildingResult result = doExecute( pom );

        if ( result.hasErrors() )
        {
            Iterator it = result.getErrors().iterator();

            while ( it.hasNext() )
            {
                addActionError( (String) it.next() );
            }

            return INPUT;
        }

        return SUCCESS;
    }

    /**
     * Subclasses must implement this method calling the appropiate operation on the continuum service.
     * 
     * @param pomUrl url of the pom specified by the user
     * @return result of adding the pom to continuum
     */
    protected abstract ContinuumProjectBuildingResult doExecute( String pomUrl )
        throws ContinuumException;

    public String doDefault()
    {
        return INPUT;
    }

    public String getPom()
    {
        return pom;
    }

    public void setPom( String pom )
    {
        this.pom = pom;
    }

    public File getPomFile()
    {
        return pomFile;
    }

    public void setPomFile( File pomFile )
    {
        this.pomFile = pomFile;
    }

    public String getPomUrl()
    {
        return pomUrl;
    }

    public void setPomUrl( String pomUrl )
    {
        this.pomUrl = pomUrl;
    }
}
