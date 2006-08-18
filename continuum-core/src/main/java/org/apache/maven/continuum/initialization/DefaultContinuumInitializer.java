package org.apache.maven.continuum.initialization;

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

import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 * @todo use this, reintroduce default project group
 */
public class DefaultContinuumInitializer
    extends AbstractLogEnabled
    implements ContinuumInitializer
{
    // ----------------------------------------------------------------------
    // Default values for the default project group
    // ----------------------------------------------------------------------

    public static final String DEFAULT_PROJECT_GROUP_NAME = "DEFAULT_PROJECT_GROUP";

    public static final String DEFAULT_PROJECT_GROUP_ID = "DEFAULT";

    public static final String DEFAULT_PROJECT_GROUP_DESCRIPTION = "Default Project Group";

    // ----------------------------------------------------------------------
    // Default values for the default schedule
    // ----------------------------------------------------------------------

    //TODO: move this to an other place
    public static final String DEFAULT_SCHEDULE_NAME = "DEFAULT_SCHEDULE";

    private SystemConfiguration systemConf;

    // ----------------------------------------------------------------------
    //  Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize()
        throws ContinuumInitializationException
    {
        getLogger().info( "Continuum initializer running ..." );

        try
        {
            // System Configuration
            systemConf = store.getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = store.addSystemConfiguration( systemConf );
            }

            // Schedule
            Schedule s = store.getScheduleByName( DEFAULT_SCHEDULE_NAME );

            if ( s == null )
            {
                Schedule defaultSchedule = createDefaultSchedule();

                store.addSchedule( defaultSchedule );
            }

            // Permission
            createPermissions();

            createGroups();

            createGuestUser();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Can't initialize default schedule.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Schedule createDefaultSchedule()
    {
        Schedule schedule = new Schedule();

        schedule.setName( DEFAULT_SCHEDULE_NAME );

        schedule.setDescription( systemConf.getDefaultScheduleDescription() );

        schedule.setCronExpression( systemConf.getDefaultScheduleCronExpression() );

        schedule.setActive( true );

        return schedule;
    }

    private void createPermissions()
        throws ContinuumStoreException
    {
        createPermission( "addProject", "Add Projects" );

        createPermission( "editProject", "Edit Projects" );

        createPermission( "deleteProject", "Delete Projects" );

        createPermission( "buildProject", "Build Projects" );

        createPermission( "showProject", "Show Projects" );

        createPermission( "addBuildDefinitionToProject", "Add Build Definitions" );

        createPermission( "editBuildDefinition", "Edit Build Definitions" );

        createPermission( "deleteBuildDefinition", "Delete Build Definitions" );

        createPermission( "addNotifier", "Add Notifiers" );

        createPermission( "editNotifier", "Edit Notifiers" );

        createPermission( "deleteNotifier", "Delete Notifiers" );

        createPermission( "manageConfiguration", "Manage Continuum Configuration" );

        createPermission( "manageSchedule", "Manage Schedules" );

        createPermission( "manageUsers", "Manage Users/Groups" );
    }

    private Permission createPermission( String name, String description )
        throws ContinuumStoreException
    {
        Permission perm = store.getPermission( name );

        if ( perm == null )
        {
            perm = new Permission();

            perm.setName( name );

            perm.setDescription( description );

            perm = store.addPermission( perm );
        }

        return perm;
    }

    private void createGroups()
        throws ContinuumStoreException
    {
        // Continuum Administrator
        if ( store.getUserGroup( ContinuumSecurity.ADMIN_GROUP_NAME ) == null )
        {
            List adminPermissions = store.getPermissions();

            UserGroup adminGroup = new UserGroup();

            adminGroup.setName( ContinuumSecurity.ADMIN_GROUP_NAME );

            adminGroup.setDescription( "Continuum Admin Group" );

            adminGroup.setPermissions( adminPermissions );

            store.addUserGroup( adminGroup );
        }

        // Continuum Guest
        if ( store.getUserGroup( ContinuumSecurity.GUEST_GROUP_NAME ) == null )
        {
            UserGroup guestGroup = new UserGroup();

            guestGroup.setName( ContinuumSecurity.GUEST_GROUP_NAME );

            guestGroup.setDescription( "Continuum Guest Group" );

            List guestPermissions = new ArrayList();

            guestPermissions.add( store.getPermission( "buildProject" ) );

            guestPermissions.add( store.getPermission( "showProject" ) );

            guestGroup.setPermissions( guestPermissions );

            store.addUserGroup( guestGroup );
        }
    }

    private void createGuestUser()
        throws ContinuumStoreException
    {
        if ( store.getGuestUser() == null )
        {
            ContinuumUser guest = new ContinuumUser();

            guest.setUsername( "guest" );

            guest.setFullName( "Anonymous User" );

            guest.setGroup( store.getUserGroup( ContinuumSecurity.GUEST_GROUP_NAME ) );

            guest.setGuest( true );

            store.addUser( guest );
        }
    }
}
