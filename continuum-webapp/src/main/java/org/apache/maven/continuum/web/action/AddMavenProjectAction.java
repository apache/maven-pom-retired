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
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Action to add a Maven project to Continuum, either Maven 1 or Maven 2.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public abstract class AddMavenProjectAction
    extends ContinuumActionSupport
    implements SecureAction
{

    private static final long serialVersionUID = -3965565189557706469L;

    private String pomUrl;

    private File pomFile;

    private String pom = null;
    
    private String username;
    
    private String password;

    private Collection projectGroups;

    private String projectGroupName;

    private int selectedProjectGroup = -1;

    private boolean disableGroupSelection;

    public String execute()
        throws ContinuumException
    {
        if ( selectedProjectGroup == -1 )
        {
            addActionError( "add.project.nogroup.error" );
            return doDefault();
        }
        
        if ( !StringUtils.isEmpty( pomUrl ) )
        {
            try
            {
                URL url = new URL( pomUrl );
                if ( pomUrl.startsWith( "http" ) && !StringUtils.isEmpty( username ) )
                {
                    StringBuffer urlBuffer = new StringBuffer();
                    urlBuffer.append( url.getProtocol() ).append( "://" );
                    urlBuffer.append( username ).append( ':' ).append( password ).append( '@' ).append( url.getHost() );
                    if ( url.getPort() != -1 )
                    {
                        urlBuffer.append( url.getPort() );
                    }
                    urlBuffer.append( url.getPath() );
                    
                    pom = urlBuffer.toString();
                }
                else
                {
                    pom = pomUrl;
                }
            }
            catch ( MalformedURLException e )
            {
                addActionError( "add.project.unknown.error" );
                return doDefault();
            }
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
                return doDefault();
            }
        }

        ContinuumProjectBuildingResult result = doExecute( pom, selectedProjectGroup );

        if ( result.hasErrors() )
        {
            Iterator it = result.getErrors().iterator();

            while ( it.hasNext() )
            {
                addActionError( (String) it.next() );
            }

            return doDefault();
        }

        return SUCCESS;
    }

    /**
     * Subclasses must implement this method calling the appropiate operation on the continuum service.
     * 
     * @param pomUrl url of the pom specified by the user
     * @param selectedProjectGroup project group id selected by the user
     * @return result of adding the pom to continuum
     */
    protected abstract ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup )
        throws ContinuumException;

    public String doDefault()
    {
        Collection allProjectGroups = getContinuum().getAllProjectGroups();
        projectGroups = new ArrayList();
        for ( Iterator i = allProjectGroups.iterator(); i.hasNext(); )
        {
            ProjectGroup pg = (ProjectGroup) i.next();
            //TODO: must implement same functionality using plexus-security
            //if ( pg.getPermissions().isWrite() )
            //{
                projectGroups.add( pg );
            //}
        }
        
        if ( session.get( "lastViewedProjectGroup" ) != null )
        {
            selectedProjectGroup = ( (Integer) session.get( "lastViewedProjectGroup" ) ).intValue();
        }
        else
        {
            selectedProjectGroup = -1;
        }
        if ( disableGroupSelection == true && selectedProjectGroup != -1 )
        {
            try
            {
                projectGroupName = getContinuum().getProjectGroup( selectedProjectGroup ).getName();
            }
            catch ( ContinuumException e )
            {
                e.printStackTrace();
            }
        }

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

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public Collection getProjectGroups()
    {
        return projectGroups;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public int getSelectedProjectGroup()
    {
        return selectedProjectGroup;
    }

    public void setSelectedProjectGroup( int selectedProjectGroup )
    {
        this.selectedProjectGroup = selectedProjectGroup;
    }

    public boolean isDisableGroupSelection()
    {
        return this.disableGroupSelection;
    }

    public void setDisableGroupSelection( boolean disableGroupSelection )
    {
        this.disableGroupSelection = disableGroupSelection;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
        {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_TO_GROUP_OPERATION, projectGroupName );

        return bundle;
    }

}
