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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;

/**
 * Add a Maven 2 project to Continuum.
 * 
 * @author Nick Gonzalez
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="addMavenTwoProject"
 */
public class AddMavenTwoProjectAction
    extends AddMavenProjectAction
{

    protected ContinuumProjectBuildingResult doExecute( String pomUrl )
        throws ContinuumException
    {
        return getContinuum().addMavenTwoProject( pomUrl );
    }

    public String doDefault()
    {
        return INPUT;
    }

    /**
     * @deprecated Use {@link #getPomFile()} instead
     */
    public File getM2PomFile()
    {
        return getPomFile();
    }

    /**
     * @deprecated Use {@link #setPomFile(File)} instead
     */
    public void setM2PomFile( File pomFile )
    {
        setPomFile( pomFile );
    }

    /**
     * @deprecated Use {@link #getPomUrl()} instead
     */
    public String getM2PomUrl()
    {
        return getPomUrl();
    }

    /**
     * @deprecated Use {@link #setPomUrl(String)} instead
     */
    public void setM2PomUrl( String pomUrl )
    {
        setPomUrl( pomUrl );
    }
}
