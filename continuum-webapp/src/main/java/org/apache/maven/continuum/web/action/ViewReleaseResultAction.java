package org.apache.maven.continuum.web.action;

import org.apache.maven.plugins.release.ReleaseResult;

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="viewReleaseResult"
 */
public class ViewReleaseResultAction
    extends ContinuumActionSupport
{
    private String releaseId;

    private ReleaseResult result;

    public String execute()
        throws Exception
    {
        result = (ReleaseResult) getContinuum().getReleaseManager().getReleaseResults().get( releaseId );

        return SUCCESS;
    }

    public String getReleaseId()
    {
        return releaseId;
    }

    public void setReleaseId( String releaseId )
    {
        this.releaseId = releaseId;
    }

    public ReleaseResult getResult()
    {
        return result;
    }

    public void setResult( ReleaseResult result )
    {
        this.result = result;
    }
}
