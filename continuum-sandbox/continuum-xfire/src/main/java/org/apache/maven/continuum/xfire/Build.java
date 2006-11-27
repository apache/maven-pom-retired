package org.apache.maven.continuum.xfire;

import java.util.Date;

public class Build
{
    private String id;
    private int state;
    private boolean forced;
    private Date startTime;
    private Date endTime;
    private String error;
    private int exitCode;

    private ScmResult scmResult;

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    public void setExitCode(int exitCode)
    {
        this.exitCode = exitCode;
    }

    public boolean isForced()
    {
        return forced;
    }

    public void setForced(boolean forced)
    {
        this.forced = forced;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public ScmResult getScmResult()
    {
        return scmResult;
    }

    public void setScmResult(ScmResult scmResult)
    {
        this.scmResult = scmResult;
    }
}
