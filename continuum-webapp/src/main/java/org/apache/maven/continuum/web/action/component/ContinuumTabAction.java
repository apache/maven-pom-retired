package org.apache.maven.continuum.web.action.component;

import org.codehaus.plexus.xwork.action.PlexusActionSupport;

/**
 * 
 * @plexus.component
 *   role="com.opensymphony.xwork.Action" role-hint="continuumTab"
 *   
**/

public class ContinuumTabAction
    extends PlexusActionSupport
{
    protected String tabName;
    
    public String getTabName()
    {
        return tabName;
    }
    
    public void setTabName( String name )
    {
        tabName = name;
    }    
}
