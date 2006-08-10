package org.apache.maven.continuum.buildcontroller;

import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class holds build context information.
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 */
public class BuildContext
{

    private long startTime;

    private Project project;

    private BuildDefinition buildDefinition;

    private BuildResult oldBuildResult;

    private ScmResult oldScmResult;

    private Map actionContext;

    private ScmResult scmResult;

    private int trigger;

    private BuildResult buildResult;

    public void setStartTime( long startTime )
    {
        this.startTime = startTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public void setBuildDefinition( BuildDefinition buildDefinition )
    {
        this.buildDefinition = buildDefinition;
    }

    public BuildDefinition getBuildDefinition()
    {
        return buildDefinition;
    }

    public void setBuildResult( BuildResult build )
    {
        this.buildResult = build;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public void setOldBuildResult( BuildResult buildResult )
    {
        this.oldBuildResult = buildResult;
    }

    public BuildResult getOldBuildResult()
    {
        return oldBuildResult;
    }

    public void setOldScmResult( ScmResult oldScmResult )
    {
        this.oldScmResult = oldScmResult;
    }

    public ScmResult getOldScmResult()
    {
        return oldScmResult;
    }

    public void setScmResult( ScmResult scmResult )
    {
        this.scmResult = scmResult;
    }

    public ScmResult getScmResult()
    {
        return scmResult;
    }

    public Map getActionContext()
    {
        if ( actionContext == null )
        {
            actionContext = new HashMap();
        }
        return actionContext;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }

}
