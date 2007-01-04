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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
    // TODO: remove this part once uploading of an m2 project with modules is supported ( CONTINUUM-1098 )
    public static final String ERROR_UPLOADING_M2_PROJECT_WITH_MODULES = "add.m2.project.upload.modules.error";
    public static final String ERROR_READING_POM_EXCEPTION_MESSAGE = "Error reading POM";
    public static final String FILE_SCHEME = "file:/";
    
    protected ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup, boolean checkProtocol )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = null;
        
        // TODO: remove this part once uploading of an m2 project with modules is supported ( CONTINUUM-1098 )
        if ( checkProtocol == false )
        {
            MavenXpp3Reader m2pomReader = new MavenXpp3Reader();

            try
            {
                String filePath = pomUrl;
                if ( filePath.startsWith( FILE_SCHEME ) )
                {
                    filePath = filePath.substring( FILE_SCHEME.length() );
                }

                Model model = m2pomReader.read( new FileReader( filePath ) );

                List modules = model.getModules();

                if ( modules != null && modules.size() != 0 )
                {
                    result = new ContinuumProjectBuildingResult();
                    result.addError( ERROR_UPLOADING_M2_PROJECT_WITH_MODULES );
                }
            }
            catch ( FileNotFoundException e )
            {
                throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
            }
            catch ( IOException e )
            {
                throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
           }
            catch ( XmlPullParserException e )
            {
                throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
            }
        }

        if ( result == null )
        {
            result = getContinuum().addMavenTwoProject( pomUrl, selectedProjectGroup, checkProtocol );
        }
        
        return result;
    }

    public String doDefault()
    {
        return super.doDefault();
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
