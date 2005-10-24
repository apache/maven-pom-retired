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

import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class InitializationChecker
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private ConfigurationService configuration;

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    public void execute( Map map )
        throws Exception
    {
        ContinuumUser adminUser = new ContinuumUser();

        String username = getValue( map, "username" );

        if ( !StringUtils.isEmpty( username) )
        {
            adminUser.setUsername( username );
        }
        else
        {
            throw new Exception( "You must set a username." );
        }

        String password = getValue( map, "password" );

        if ( !StringUtils.isEmpty( password) && password.equals( getValue( map, "password.two" ) ) )
        {
            adminUser.setPassword( getValue( map, "password" ) );
        }
        else
        {
            throw new Exception( "Your password is incorrect." );
        }

        String fullName = getValue( map, "fullName" );

        if ( !StringUtils.isEmpty( fullName) )
        {
            adminUser.setFullName( fullName );
        }
        else
        {
            throw new Exception( "You must set a full name." );
        }

        String email = getValue( map, "email" );

        if ( !StringUtils.isEmpty( email) )
        {
            adminUser.setEmail( email );
        }
        else
        {
            throw new Exception( "You must set an email." );
        }

        adminUser.setGroup( store.getUserGroup( ContinuumSecurity.ADMIN_GROUP_NAME ) );

        store.addUser( adminUser );

        String workingDirectory = getValue( map, "workingDirectory" );

        if ( !StringUtils.isEmpty( workingDirectory) )
        {
            configuration.setWorkingDirectory( configuration.getFile( workingDirectory ) );
        }
        else
        {
            throw new Exception( "You must set a working directory." );
        }

        String buildOutputDirectory = getValue( map, "buildOutputDirectory" );

        if ( !StringUtils.isEmpty( buildOutputDirectory) )
        {
            configuration.setBuildOutputDirectory( configuration.getFile( buildOutputDirectory ) );
        }
        else
        {
            throw new Exception( "You must set a build output directory." );
        }

        String baseUrl = getValue( map, "baseUrl" );

        if ( !StringUtils.isEmpty( baseUrl) )
        {
            configuration.setUrl( baseUrl );
        }
        else
        {
            throw new Exception( "You must set a base Url." );
        }

        String companyName = getValue( map, "companyName" );

        if ( !StringUtils.isEmpty( companyName) )
        {
            configuration.setCompanyName( companyName );
        }

        String companyLogo = getValue( map, "companyLogo" );

        if ( !StringUtils.isEmpty( companyLogo) )
        {
            configuration.setCompanyLogo( companyLogo );
        }

        String companyUrl = getValue( map, "companyUrl" );

        if ( !StringUtils.isEmpty( companyUrl) )
        {
            configuration.setCompanyUrl( companyUrl );
        }

        boolean guestAccountEnabled = getBooleanValue( map, "guestAccountEnabled" );

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

        configuration.setInitialized( true );

        configuration.store();

        RunData data = (RunData) map.get( "data" );

        data.setTarget( "ConfigureEnd.vm" );
    }

    private String getValue( Map map, String param )
    {
        return (String) map.get( param );
    }

    private boolean getBooleanValue( Map map, String param )
    {
        if ( "on".equals( (String) map.get( param ) ) )
        {
            return true;
        }

        return false;
    }
}
