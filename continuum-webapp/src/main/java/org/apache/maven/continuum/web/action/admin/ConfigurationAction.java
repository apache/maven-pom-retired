package org.apache.maven.continuum.web.action.admin;

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

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Preparable;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.ConfigurationStoringException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.security.policy.UserSecurityPolicy;
import org.codehaus.plexus.security.rbac.Resource;
import org.codehaus.plexus.security.system.SecuritySystem;
import org.codehaus.plexus.security.ui.web.interceptor.SecureAction;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionBundle;
import org.codehaus.plexus.security.ui.web.interceptor.SecureActionException;
import org.codehaus.plexus.security.user.User;
import org.codehaus.plexus.security.user.UserManager;
import org.codehaus.plexus.security.user.UserNotFoundException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import javax.servlet.http.HttpServletRequest;

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
    implements Preparable, SecureAction
{
    
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    private boolean guestAccountEnabled;

    private String workingDirectory;

    private String buildOutputDirectory;

    private String deploymentRepositoryDirectory;

    private String baseUrl;

    private String companyLogo;

    private String companyName;

    private String companyUrl;


    public void prepare()
    {
        try{


        ConfigurationService configuration = getContinuum().getConfiguration();

        guestAccountEnabled = getGuestAccountLockingStatus();

        workingDirectory = configuration.getWorkingDirectory().getAbsolutePath();

        buildOutputDirectory = configuration.getBuildOutputDirectory().getAbsolutePath();

        baseUrl = configuration.getUrl();

        if ( StringUtils.isEmpty( baseUrl ) )
        {
            HttpServletRequest request = ServletActionContext.getRequest();
            baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();
            getLogger().info( "baseUrl='" + baseUrl + "'" );
        }

        companyLogo = configuration.getCompanyLogo();

        companyName = configuration.getCompanyName();

        companyUrl = configuration.getCompanyUrl();
        } catch ( Exception e)
        {
            e.printStackTrace( );
        }
    }

    public String save()
        throws ConfigurationStoringException, ContinuumStoreException
    {

        try
        {
        ConfigurationService configuration = getContinuum().getConfiguration();

        configuration.setGuestAccountEnabled( guestAccountEnabled );

        resolveGuestAccountLockingStatus();

        configuration.setWorkingDirectory( new File( workingDirectory ) );

        configuration.setBuildOutputDirectory( new File( buildOutputDirectory ) );

        configuration.setDeploymentRepositoryDirectory( new File( deploymentRepositoryDirectory ) );

        configuration.setUrl( baseUrl );

        configuration.setCompanyLogo( companyLogo );

        configuration.setCompanyName( companyName );

        configuration.setCompanyUrl( companyUrl );

        configuration.setInitialized( true );
        configuration.store();
        }
        catch (Exception e)
        {
            e.printStackTrace( );
        }
        return SUCCESS;

    }


    private void resolveGuestAccountLockingStatus()
    {

        UserManager userManager = securitySystem.getUserManager();
        UserSecurityPolicy policy = securitySystem.getPolicy();

        User guest;

        try
        {
            guest = userManager.findUser( "guest" );
            guest.setLocked( guestAccountEnabled );
            userManager.updateUser( guest );
        }
        catch ( UserNotFoundException ne )
        {
            policy.setEnabled( false );

            guest = userManager.createUser( "guest", "Guest", "" );
            guest.setLocked( guestAccountEnabled );
            guest = userManager.addUser( guest );

        }
        finally
        {
            policy.setEnabled( true );
        }       
    }

    private boolean getGuestAccountLockingStatus()
    {
        UserManager userManager = securitySystem.getUserManager();
        UserSecurityPolicy policy = securitySystem.getPolicy();

        User guest;

        try
        {
            guest = userManager.findUser( "guest" );

            return guest.isLocked();
        }
        catch ( UserNotFoundException ne )
        {
            policy.setEnabled( false );

            guest = userManager.createUser( "guest", "Guest", "" );
            guest = userManager.addUser( guest );

            return guest.isLocked();
        }
        finally
        {
            policy.setEnabled( true );
        }
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

    public String getDeploymentRepositoryDirectory()
    {
        return deploymentRepositoryDirectory;
    }

    public void setDeploymentRepositoryDirectory( String deploymentRepositoryDirectory )
    {
        this.deploymentRepositoryDirectory = deploymentRepositoryDirectory;
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


    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION, Resource.GLOBAL );

        return bundle;
    }
}
