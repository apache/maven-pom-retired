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

    private static final int DEFINED_BY_POM_GROUP_ID = -1;

    private String pomUrl;

    private File pomFile;

    private String pom = null;
    
    private String scmUsername;
    
    private String scmPassword;

    private Collection projectGroups;

    private String projectGroupName;

    private int selectedProjectGroup = DEFINED_BY_POM_GROUP_ID;

    private boolean disableGroupSelection;

    public String execute()
        throws ContinuumException
    {
        boolean checkProtocol = true;

        if ( !StringUtils.isEmpty( pomUrl ) )
        {
            try
            {
                URL url = new URL( pomUrl );
                if ( pomUrl.startsWith( "http" ) && !StringUtils.isEmpty(scmUsername) )
                {
                    StringBuffer urlBuffer = new StringBuffer();
                    urlBuffer.append( url.getProtocol() ).append( "://" );
                    urlBuffer.append(scmUsername).append( ':' ).append(scmPassword).append( '@' ).append( url.getHost() );
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
                    checkProtocol = false;
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

        ContinuumProjectBuildingResult result = doExecute( pom, selectedProjectGroup, checkProtocol );

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
     * @param checkProtocol check if the protocol is allowed, use false if the pom is uploaded
     * @return result of adding the pom to continuum
     */
    protected abstract ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup, boolean checkProtocol )
        throws ContinuumException;

    public String doDefault()
    {
        Collection allProjectGroups = getContinuum().getAllProjectGroups();
        projectGroups = new ArrayList();

        ProjectGroup defaultGroup = new ProjectGroup();
        defaultGroup.setId( DEFINED_BY_POM_GROUP_ID );
        defaultGroup.setName( "Defined by POM" );
        projectGroups.add( defaultGroup );

        for ( Iterator i = allProjectGroups.iterator(); i.hasNext(); )
        {
            ProjectGroup pg = (ProjectGroup) i.next();
            //TODO: must implement same functionality using plexus-security
            //if ( pg.getPermissions().isWrite() )
            //{
                projectGroups.add( pg );
            //}
        }

        initializeProjectGroupName();

        return INPUT;
    }

    private void initializeProjectGroupName()
    {
        if ( disableGroupSelection == true && selectedProjectGroup != DEFINED_BY_POM_GROUP_ID )
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

    public void setScmPassword( String scmPassword)
    {
        this.scmPassword = scmPassword;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername)
    {
        this.scmUsername = scmUsername;
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
        initializeProjectGroupName();

        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );

        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION );
        }
        else
        {
            bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_TO_GROUP_OPERATION, projectGroupName );
        }

        return bundle;
    }
}
