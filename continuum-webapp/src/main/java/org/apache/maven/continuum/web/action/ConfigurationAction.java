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

import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.opensymphony.xwork.ActionSupport;

public class ConfigurationAction
    extends ActionSupport
{
    private Continuum continuum;

    private boolean guestAccountEnabled;

    private String workingDirectory;

    private String buildOutputDirectory;

    private String baseUrl;

    private String companyLogo;

    private String companyName;

    private String companyUrl;

    public String execute()
        throws Exception
    {
        boolean hasErrors = false;

        // TODO : Using WebWork Validators
        if ( StringUtils.isEmpty( workingDirectory ) )
        {
            addFieldError( "workingDirectory", getText( "configuration.missing.workingDirectory" ) );

            hasErrors = true;
        }

        if ( StringUtils.isEmpty( buildOutputDirectory ) )
        {
            addFieldError( "buildOutputDirectory", getText( "configuration.missing.buildOutputDirectory" ) );

            hasErrors = true;
        }

        if ( StringUtils.isEmpty( baseUrl ) )
        {
            addFieldError( "baseUrl", getText( "configuration.missing.baseUrl" ) );

            hasErrors = true;
        }
        else
        {
            try
            {
                URL url = new URL( baseUrl );
            }
            catch ( MalformedURLException e )
            {
                addFieldError( "baseUrl", getText( "configuration.invalid.baseUrl" ) );

                hasErrors = true;
            }
        }

        if ( hasErrors )
        {
            return INPUT;
        }
        else
        {
            continuum.getConfiguration().setGuestAccountEnabled( guestAccountEnabled );

            continuum.getConfiguration().setWorkingDirectory( new File( workingDirectory ) );

            continuum.getConfiguration().setBuildOutputDirectory( new File( buildOutputDirectory ) );

            continuum.getConfiguration().setUrl( baseUrl );

            continuum.getConfiguration().setCompanyLogo( companyLogo );

            continuum.getConfiguration().setCompanyName( companyName );

            continuum.getConfiguration().setCompanyUrl( companyUrl );

            return SUCCESS;
        }
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
        return continuum.getConfiguration().isGuestAccountEnabled();
    }

    public void setGuestAccountEnabled( boolean guestAccountEnabled )
    {
        this.guestAccountEnabled = guestAccountEnabled;
    }

    public String getWorkingDirectory()
    {
        return continuum.getConfiguration().getWorkingDirectory().getAbsolutePath();
    }

    public void setWorkingDirectory( String workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public String getBuildOutputDirectory()
    {
        return continuum.getConfiguration().getBuildOutputDirectory().getAbsolutePath();
    }

    public void setBuildOutputDirectory( String buildOutputDirectory )
    {
        this.buildOutputDirectory = buildOutputDirectory;
    }

    public String getBaseUrl()
    {
        return continuum.getConfiguration().getUrl();
    }

    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }

    public String getCompanyLogo()
    {
        return continuum.getConfiguration().getCompanyLogo();
    }

    public void setCompanyLogo( String companyLogo )
    {
        this.companyLogo = companyLogo;
    }

    public String getCompanyName()
    {
        return continuum.getConfiguration().getCompanyName();
    }

    public void setCompanyName( String companyName )
    {
        this.companyName = companyName;
    }

    public String getCompanyUrl()
    {
        return continuum.getConfiguration().getCompanyUrl();
    }

    public void setCompanyUrl( String companyUrl )
    {
        this.companyUrl = companyUrl;
    }
}
