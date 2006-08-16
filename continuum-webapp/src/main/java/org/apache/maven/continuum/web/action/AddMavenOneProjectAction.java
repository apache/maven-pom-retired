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
 * Add a Maven 1 project to continuum.
 * 
 * @author Nick Gonzalez
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="addMavenOneProject"
 */
public class AddMavenOneProjectAction
    extends AddMavenProjectAction
{

    protected ContinuumProjectBuildingResult doExecute( String pomUrl )
        throws ContinuumException
    {
        return continuum.addMavenTwoProject( pomUrl );
    }

    /**
     * @deprecated Use {@link #getPom()} instead
     */
    public String getM1Pom()
    {
        return getPom();
    }

    /**
     * @deprecated Use {@link #setPom(String)} instead
     */
    public void setM1Pom( String pom )
    {
        setPom( pom );
    }

    /**
     * @deprecated Use {@link #getPomFile()} instead
     */
    public File getM1PomFile()
    {
        return getPomFile();
    }

    /**
     * @deprecated Use {@link #setPomFile(File)} instead
     */
    public void setM1PomFile( File pomFile )
    {
        setPomFile( pomFile );
    }

    /**
     * @deprecated Use {@link #getPomUrl()} instead
     */
    public String getM1PomUrl()
    {
        return getPomUrl();
    }

    /**
     * @deprecated Use {@link #setPomUrl(String)} instead
     */
    public void setM1PomUrl( String pomUrl )
    {
        setPomUrl( pomUrl );
    }
}
