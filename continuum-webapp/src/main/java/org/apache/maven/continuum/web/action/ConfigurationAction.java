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

import com.opensymphony.xwork.Preparable;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.codehaus.plexus.xwork.action.PlexusActionSupport;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="configuration"
 */
public class ConfigurationAction
    extends PlexusActionSupport
    implements Preparable
{
    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    private boolean guestAccountEnabled;

    private String workingDirectory;

    private String buildOutputDirectory;

    private String baseUrl;

    private String companyLogo;

    private String companyName;

    private String companyUrl;

    public void prepare()
    {
        guestAccountEnabled = continuum.getConfiguration().isGuestAccountEnabled();

        workingDirectory = continuum.getConfiguration().getWorkingDirectory().getAbsolutePath();

        buildOutputDirectory = continuum.getConfiguration().getBuildOutputDirectory().getAbsolutePath();

        baseUrl = continuum.getConfiguration().getUrl();

        companyLogo = continuum.getConfiguration().getCompanyLogo();

        companyName = continuum.getConfiguration().getCompanyName();

        companyUrl = continuum.getConfiguration().getCompanyUrl();
    }

    public String execute()
        throws Exception
    {
        continuum.getConfiguration().setGuestAccountEnabled( guestAccountEnabled );

        continuum.getConfiguration().setWorkingDirectory( new File( workingDirectory ) );

        continuum.getConfiguration().setBuildOutputDirectory( new File( buildOutputDirectory ) );

        continuum.getConfiguration().setUrl( baseUrl );

        continuum.getConfiguration().setCompanyLogo( companyLogo );

        continuum.getConfiguration().setCompanyName( companyName );

        continuum.getConfiguration().setCompanyUrl( companyUrl );

        try
        {
            continuum.getConfiguration().setInitialized( true );
            continuum.getConfiguration().store();            
        }
        catch ( ConfigurationStoringException e )
        {
            addActionError( "Can't store configuration :" + e.getMessage() );

            return INPUT;
        }

        return SUCCESS;
    }

    public String doDefault()
        throws Exception
    {
        return SUCCESS;
    }

    public String doEdit()
        throws Exception
    {
        return INPUT;
    }

    public boolean isGuestAccountEnabled()
    {
        return guestAccountEnabled;
    }

    public void setGuestAccountEnabled( boolean guestAccountEnabled )
    {
        this.guestAccountEnabled = guestAccountEnabled;
    }

    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( String workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public String getBuildOutputDirectory()
    {
        return buildOutputDirectory;
    }

    public void setBuildOutputDirectory( String buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }

    public String getCompanyLogo()
    {
        return companyLogo;
    }

    public void setCompanyLogo( String companyLogo )
    {
        this.companyLogo = companyLogo;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName( String companyName )
    {
        this.companyName = companyName;
    }

    public String getCompanyUrl()
    {
        return companyUrl;
    }

    public void setCompanyUrl( String companyUrl )
    {
        this.companyUrl = companyUrl;
    }
}
