package org.apache.maven.continuum.web.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.util.UrlHelper;
import com.opensymphony.xwork.ActionContext;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.util.WorkingCopyContentGenerator;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.security.authorization.AuthorizationException;
import org.codehaus.plexus.security.system.SecuritySession;
import org.codehaus.plexus.security.system.SecuritySystem;
import org.codehaus.plexus.security.system.SecuritySystemConstants;
import org.codehaus.plexus.xwork.PlexusLifecycleListener;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="workingCopy"
 */
public class WorkingCopyAction
    extends ContinuumActionSupport
{
    /**
     * @plexus.requirement
     */
    private WorkingCopyContentGenerator generator;

    private Project project;

    private int projectId;

    private String userDirectory;

    private String currentFile;

    private String currentFileContent;

    private List files;

    private String output;

    private String projectName;

    private File downloadFile;

    private String mimeType = "application/octet-stream";

    private static String FILE_SEPARATOR = System.getProperty( "file.separator" );

    public String execute()
        throws ContinuumException
    {
        files = getContinuum().getFiles( projectId, userDirectory );

        project = getContinuum().getProject( projectId );

        projectName = project.getName();

        if ( isAuthorized( project ) )
        {
            HashMap params = new HashMap();

            params.put( "projectId", new Integer( projectId ) );

            params.put( "projectName", projectName );

            String baseUrl = UrlHelper.buildUrl( "/workingCopy.action", ServletActionContext.getRequest(),
                                                 ServletActionContext.getResponse(), params );

            output = generator.generate( files, baseUrl, getContinuum().getWorkingDirectory( projectId ) );

            if ( currentFile != null && currentFile != "" )
            {
                String dir;

                //TODO: maybe create a plexus component for this so that additional mimetypes can be easily added
                MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
                mimeTypesMap.addMimeTypes( "application/java-archive jar war ear" );
                mimeTypesMap.addMimeTypes( "application/java-class class" );
                mimeTypesMap.addMimeTypes( "image/png png" );

                if ( FILE_SEPARATOR.equals( userDirectory ) )
                {
                    dir = userDirectory;
                }
                else
                {
                    dir = FILE_SEPARATOR + userDirectory + FILE_SEPARATOR;
                }

                downloadFile = new File( getContinuum().getWorkingDirectory( projectId ) + dir + currentFile );
                mimeType = mimeTypesMap.getContentType( downloadFile );

                if ( ( mimeType.indexOf( "image" ) >= 0 ) || ( mimeType.indexOf( "java-archive" ) >= 0 ) ||
                    ( mimeType.indexOf( "java-class" ) >= 0 ) || ( downloadFile.length() > 100000 ) )
                {
                    return "stream";
                }

                currentFileContent = getContinuum().getFileContent( projectId, userDirectory, currentFile );
            }
            else
            {
                currentFileContent = "";
            }
        }

        return SUCCESS;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getUserDirectory()
    {
        return userDirectory;
    }

    public void setUserDirectory( String userDirectory )
    {
        this.userDirectory = userDirectory;
    }

    public void setFile( String currentFile )
    {
        this.currentFile = currentFile;
    }

    public List getFiles()
    {
        return files;
    }

    public String getOutput()
    {
        return output;
    }

    public String getFileContent()
    {
        return currentFileContent;
    }


    public InputStream getInputStream()
        throws ContinuumException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( downloadFile );
        }
        catch ( FileNotFoundException fne )
        {
            throw new ContinuumException( "Error accessing file.", fne );
        }

        return fis;
    }

    public String getFileLength()
    {
        return Long.toString( downloadFile.length() );
    }

    public String getDownloadFilename()
    {
        return downloadFile.getName();
    }

    public String getMimeType()
    {
        return this.mimeType;
    }

    public Project getProject()
    {
        return project;
    }

    private boolean isAuthorized( Project project )
    {
        // do the authz bit
        ActionContext context = ActionContext.getContext();

        PlexusContainer container = (PlexusContainer) context.getApplication().get( PlexusLifecycleListener.KEY );
        SecuritySession securitySession =
            (SecuritySession) context.getSession().get( SecuritySystemConstants.SECURITY_SESSION_KEY );

        try
        {
            SecuritySystem securitySystem = (SecuritySystem) container.lookup( SecuritySystem.ROLE );

            if ( !securitySystem.isAuthorized( securitySession, ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION,
                                               project.getProjectGroup().getName() ) )
            {
                return false;
            }
        }
        catch ( ComponentLookupException cle )
        {
            return false;
        }
        catch ( AuthorizationException ae )
        {
            return false;
        }

        return true;
    }
}
