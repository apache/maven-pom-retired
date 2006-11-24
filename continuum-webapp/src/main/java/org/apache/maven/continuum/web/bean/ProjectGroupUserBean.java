package org.apache.maven.continuum.web.bean;

import java.util.Collection;
import java.util.Iterator;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.security.rbac.Role;
import org.codehaus.plexus.security.user.User;

public class ProjectGroupUserBean
{
    public static final String ROLE_ADMINISTRATOR = "Group Project Administrator";
    
    public static final String ROLE_DEVELOPER = "Project Developer";
    
    public static final String ROLE_USER = "Project User";
    
    private User user;
    
    private ProjectGroup projectGroup;
    
    private Collection roles;
    
    public boolean isAdministrator()
    {
        for ( Iterator i = roles.iterator(); i.hasNext(); )
        {
            Role role = (Role) i.next();
            if ( role.getName().indexOf( ROLE_ADMINISTRATOR ) > -1 )
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean isDeveloper()
    {
        for ( Iterator i = roles.iterator(); i.hasNext(); )
        {
            Role role = (Role) i.next();
            if ( role.getName().indexOf( projectGroup.getName() ) > -1 && 
                 role.getName().indexOf( ROLE_DEVELOPER ) > -1 )
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean isUser()
    {
        for ( Iterator i = roles.iterator(); i.hasNext(); )
        {
            Role role = (Role) i.next();
            if ( role.getName().indexOf( projectGroup.getName() ) > -1 && 
                 role.getName().indexOf( ROLE_USER ) > -1 )
            {
                return true;
            }
        }
        
        return false;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    public void addRole( String role )
    {
        roles.add( role );
    }
    
    public Collection getRoles()
    {
        return roles;
    }

    public void setRoles( Collection roles )
    {
        this.roles = roles;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }
    
    public String getUsername()
    {
        return user.getUsername();
    }
    
    public String getUserFullName()
    {
        return user.getFullName();
    }
    
    public String toString()
    {
        return user.getUsername() + ": " + roles + ": "+(isAdministrator()?"A":"-") + (isDeveloper()?"D":"-") + (isUser()?"U":"-"); 
    }

}
