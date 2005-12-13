package org.apache.maven.continuum.web.action;

import com.opensymphony.xwork.ActionSupport;

import java.util.List;

public class SummaryAction
    extends ActionSupport
{
    private List projects;

    public String execute()
    {
        return SUCCESS;
    }
}
