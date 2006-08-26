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
import java.util.Collections;

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import com.opensymphony.xwork.Preparable;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="configuration"
 */
public class ConfigurationAction
    extends ContinuumActionSupport
    implements Preparable
{

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    private boolean guestAccountEnabled;

    private String username;

    private String password;

    private String passwordTwo;

    private String fullName;

    private String email;

    private String workingDirectory;

    private String buildOutputDirectory;

    private String baseUrl;

    private String companyLogo;

    private String companyName;

    private String companyUrl;

    public void prepare()
    {
        ConfigurationService configuration = getContinuum().getConfiguration();

        guestAccountEnabled = configuration.isGuestAccountEnabled();

        workingDirectory = configuration.getWorkingDirectory().getAbsolutePath();

        buildOutputDirectory = configuration.getBuildOutputDirectory().getAbsolutePath();

        baseUrl = configuration.getUrl();

        companyLogo = configuration.getCompanyLogo();

        companyName = configuration.getCompanyName();

        companyUrl = configuration.getCompanyUrl();
    }

    public String execute()
        throws ConfigurationStoringException, ContinuumStoreException
    {
        //todo switch this to validation

        ContinuumUser adminUser = new ContinuumUser();

        adminUser.setUsername( username );
        adminUser.setPassword( password );
        adminUser.setEmail( email );
        adminUser.setFullName( fullName );
        adminUser.setGroup( store.getUserGroup( ContinuumSecurity.ADMIN_GROUP_NAME ) );

        store.addUser( adminUser );
        
        ConfigurationService configuration = getContinuum().getConfiguration();

        if ( guestAccountEnabled )
        {
            configuration.setGuestAccountEnabled( guestAccountEnabled );
        }
        else
        {
            configuration.setGuestAccountEnabled( false );

            UserGroup guestGroup = store.getUserGroup( ContinuumSecurity.GUEST_GROUP_NAME );

            guestGroup.setPermissions( Collections.EMPTY_LIST );

            store.updateUserGroup( guestGroup );
        }

        configuration.setWorkingDirectory( new File( workingDirectory ) );

        configuration.setWorkingDirectory( new File( workingDirectory ) );

        configuration.setBuildOutputDirectory( new File( buildOutputDirectory ) );

        configuration.setUrl( baseUrl );

        configuration.setCompanyLogo( companyLogo );

        configuration.setCompanyName( companyName );

        configuration.setInitialized( true );
        configuration.store();

        configuration.setInitialized( true );
        configuration.store();            

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

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getPasswordTwo()
    {
        return passwordTwo;
    }

    public void setPasswordTwo( String passwordTwo )
    {
        this.passwordTwo = passwordTwo;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
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
