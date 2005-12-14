package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.Continuum;

import com.opensymphony.xwork.ActionSupport;

import java.util.Collection;

public class SummaryAction
    extends ActionSupport
{
    private Continuum continuum;
    
    private Collection projects;

    public String execute()
        throws Exception
    {
        try
        {
            projects = continuum.getProjects();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public Collection getProjects()
    {
        return projects;
    }
}
