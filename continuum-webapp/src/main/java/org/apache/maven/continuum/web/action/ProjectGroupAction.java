package org.apache.maven.continuum.web.action;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.bean.ProjectGroupUserBean;
import org.apache.maven.continuum.ContinuumException;
import org.codehaus.plexus.security.rbac.RBACManager;
import org.codehaus.plexus.security.rbac.RbacManagerException;
import org.codehaus.plexus.security.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.security.rbac.Role;
import org.codehaus.plexus.security.user.User;
import org.codehaus.plexus.security.user.UserManager;
import org.codehaus.plexus.util.StringUtils;

/**
 * ProjectGroupAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="projectGroup"
 */
public class ProjectGroupAction
    extends ContinuumConfirmAction
{
    private final static Map FILTER_CRITERIA = new HashMap();
    static
    {
        FILTER_CRITERIA.put( "username", "Username contains" );
        FILTER_CRITERIA.put( "fullName", "Name contains" );
        FILTER_CRITERIA.put( "email", "Email contains" );
    }
    
    /**
     * @plexus.requirement
     */
    private UserManager manager;

    /**
     * @plexus.requirement
     */
    private RBACManager rbac;

    private int projectGroupId;

    private ProjectGroup projectGroup;

    private boolean confirmed;
    
    private List projectGroupUsers;
    
    private String filterProperty;
    
    private String filterKey;
    
    private boolean ascending = true;

    public String summary()
        throws ContinuumException
    {
        projectGroup = getContinuum().getProjectGroup( projectGroupId );

        return SUCCESS;
    }

    public String members()
        throws ContinuumException
    {
        projectGroup = getContinuum().getProjectGroup( projectGroupId );

        populateProjectGroupUsers( projectGroup );
        
        return SUCCESS;
    }

    public String buildDefinitions()
        throws ContinuumException
    {
        return summary();
    }

    public String notifiers()
        throws ContinuumException
    {
        return summary();
    }

    public String remove()
        throws ContinuumException
    {
        //todo add confirm page like removing build definition
        if ( confirmed )
        {
            getContinuum().removeProjectGroup( projectGroupId );
        }
        else
        {
            return CONFIRM;
        }

        return SUCCESS;
    }

    public String build()
        throws ContinuumException
    {
        getContinuum().buildProjectGroup( projectGroupId );

        return SUCCESS;
    }

    private void populateProjectGroupUsers( ProjectGroup group ) 
    {
        List users;
        
        if ( StringUtils.isEmpty( filterKey ) )
        {
            users = manager.getUsers( ascending );
        }
        else
        {
            users = findUsers( filterProperty, filterKey, ascending );
        }
        
        projectGroupUsers = new ArrayList();
        
        for ( Iterator i = users.iterator(); i.hasNext(); )
        {
            ProjectGroupUserBean pgUser = new ProjectGroupUserBean();
            
            User user = (User) i.next();
            
            pgUser.setUser( user );
            
            pgUser.setProjectGroup( group );
            
            try
            {
                Collection effectiveRoles = rbac.getEffectivelyAssignedRoles( user.getUsername() );
                for ( Iterator j = effectiveRoles.iterator(); j.hasNext(); )
                {
                    Role role = (Role) j.next();
                    if( role.getName().indexOf( projectGroup.getName() ) > -1 )
                    {
                        pgUser.setRoles( effectiveRoles );
                        projectGroupUsers.add( pgUser );
                        break;
                    }
                }
                
            }
            catch ( RbacObjectNotFoundException e )
            {
                pgUser.setRoles( Collections.EMPTY_LIST );
            }
            catch ( RbacManagerException e )
            {
                pgUser.setRoles( Collections.EMPTY_LIST );
            }
        }
    }
    
    private List findUsers( String searchProperty, String searchKey, boolean orderAscending )
    {
        List users = null;
            
        if ( "username".equals( searchProperty ) )
        {
            users = manager.findUsersByUsernameKey( searchKey, orderAscending );
        }
        else if ( "fullName".equals( getFilterProperty() ) )
        {
            users = manager.findUsersByFullNameKey( searchKey, orderAscending );
        }
        else if ( "email".equals( getFilterProperty() ) )
        {
            users = manager.findUsersByEmailKey( searchKey, orderAscending );
        }
        else
        {
            users = Collections.EMPTY_LIST;
        }
        
        return users;
    }
    
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public List getProjectGroupUsers()
    {
        return projectGroupUsers;
    }

    public boolean isAscending()
    {
        return ascending;
    }

    public void setAscending( boolean ascending )
    {
        this.ascending = ascending;
    }

    public String getFilterKey()
    {
        return filterKey;
    }

    public void setFilterKey( String filterKey )
    {
        this.filterKey = filterKey;
    }

    public String getFilterProperty()
    {
        return filterProperty;
    }

    public void setFilterProperty( String filterProperty )
    {
        this.filterProperty = filterProperty;
    }

    public Map getCriteria()
    {
        return FILTER_CRITERIA;
    }
    
}
