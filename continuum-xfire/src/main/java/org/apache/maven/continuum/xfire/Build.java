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
    private String standardOutput;
    private String standardError;
    private int exitCode;
    
    private UpdateScmResult updateScmResult;

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

    public String getStandardError()
    {
        return standardError;
    }

    public void setStandardError(String standardError)
    {
        this.standardError = standardError;
    }

    public String getStandardOutput()
    {
        return standardOutput;
    }

    public void setStandardOutput(String standardOutput)
    {
        this.standardOutput = standardOutput;
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

    public UpdateScmResult getUpdateScmResult()
    {
        return updateScmResult;
    }

    public void setUpdateScmResult(UpdateScmResult updateScmResult)
    {
        this.updateScmResult = updateScmResult;
    }
}
