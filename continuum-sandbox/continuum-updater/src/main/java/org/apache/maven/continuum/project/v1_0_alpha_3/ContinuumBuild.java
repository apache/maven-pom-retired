/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.apache.maven.continuum.scm.v1_0_alpha_3.UpdateScmResult;

import java.util.*;

/**
 * Class ContinuumBuild.
 * 
 * @version $Revision$ $Date$
 */
public class ContinuumBuild implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field project
     */
    private ContinuumProject project;

    /**
     * Field id
     */
    private String id;

    /**
     * Field state
     */
    private int state = 0;

    /**
     * Field forced
     */
    private boolean forced = false;

    /**
     * Field startTime
     */
    private long startTime = 0;

    /**
     * Field endTime
     */
    private long endTime = 0;

    /**
     * Field error
     */
    private String error;

    /**
     * Field success
     */
    private boolean success = false;

    /**
     * Field standardOutput
     */
    private String standardOutput;

    /**
     * Field standardError
     */
    private String standardError;

    /**
     * Field exitCode
     */
    private int exitCode = 0;

    /**
     * Field updateScmResult
     */
    private UpdateScmResult updateScmResult;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method breakContinuumProjectAssociation
     * 
     * @param continuumProject
     */
    public void breakContinuumProjectAssociation(ContinuumProject continuumProject)
    {
        if ( this.project != continuumProject )
        {
            throw new IllegalStateException( "continuumProject isn't associated." );
        }
        
        this.project = null;
    } //-- void breakContinuumProjectAssociation(ContinuumProject) 

    /**
     * Method createContinuumProjectAssociation
     * 
     * @param continuumProject
     */
    public void createContinuumProjectAssociation(ContinuumProject continuumProject)
    {
        if ( this.project != null )
        {
            breakContinuumProjectAssociation( this.project );
        }
        
        this.project = continuumProject;
    } //-- void createContinuumProjectAssociation(ContinuumProject) 

    /**
     * Method getEndTime
     */
    public long getEndTime()
    {
        return this.endTime;
    } //-- long getEndTime() 

    /**
     * Method getError
     */
    public String getError()
    {
        return this.error;
    } //-- String getError() 

    /**
     * Method getExitCode
     */
    public int getExitCode()
    {
        return this.exitCode;
    } //-- int getExitCode() 

    /**
     * Method getId
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Method getProject
     */
    public ContinuumProject getProject()
    {
        return this.project;
    } //-- ContinuumProject getProject() 

    /**
     * Method getStandardError
     */
    public String getStandardError()
    {
        return this.standardError;
    } //-- String getStandardError() 

    /**
     * Method getStandardOutput
     */
    public String getStandardOutput()
    {
        return this.standardOutput;
    } //-- String getStandardOutput() 

    /**
     * Method getStartTime
     */
    public long getStartTime()
    {
        return this.startTime;
    } //-- long getStartTime() 

    /**
     * Method getState
     */
    public int getState()
    {
        return this.state;
    } //-- int getState() 

    /**
     * Method getUpdateScmResult
     */
    public UpdateScmResult getUpdateScmResult()
    {
        return this.updateScmResult;
    } //-- UpdateScmResult getUpdateScmResult() 

    /**
     * Method isForced
     */
    public boolean isForced()
    {
        return this.forced;
    } //-- boolean isForced() 

    /**
     * Method isSuccess
     */
    public boolean isSuccess()
    {
        return this.success;
    } //-- boolean isSuccess() 

    /**
     * Method setEndTime
     * 
     * @param endTime
     */
    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    } //-- void setEndTime(long) 

    /**
     * Method setError
     * 
     * @param error
     */
    public void setError(String error)
    {
        this.error = error;
    } //-- void setError(String) 

    /**
     * Method setExitCode
     * 
     * @param exitCode
     */
    public void setExitCode(int exitCode)
    {
        this.exitCode = exitCode;
    } //-- void setExitCode(int) 

    /**
     * Method setForced
     * 
     * @param forced
     */
    public void setForced(boolean forced)
    {
        this.forced = forced;
    } //-- void setForced(boolean) 

    /**
     * Method setId
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    } //-- void setId(String) 

    /**
     * Method setProject
     * 
     * @param project
     */
    public void setProject(ContinuumProject project)
    {
        if ( this.project != null )
        {
            this.project.breakContinuumBuildAssociation( this );
        }
        
        this.project = project;
        
        if ( project != null )
        {
            this.project.createContinuumBuildAssociation( this );
        }
    } //-- void setProject(ContinuumProject) 

    /**
     * Method setStandardError
     * 
     * @param standardError
     */
    public void setStandardError(String standardError)
    {
        this.standardError = standardError;
    } //-- void setStandardError(String) 

    /**
     * Method setStandardOutput
     * 
     * @param standardOutput
     */
    public void setStandardOutput(String standardOutput)
    {
        this.standardOutput = standardOutput;
    } //-- void setStandardOutput(String) 

    /**
     * Method setStartTime
     * 
     * @param startTime
     */
    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    } //-- void setStartTime(long) 

    /**
     * Method setState
     * 
     * @param state
     */
    public void setState(int state)
    {
        this.state = state;
    } //-- void setState(int) 

    /**
     * Method setSuccess
     * 
     * @param success
     */
    public void setSuccess(boolean success)
    {
        this.success = success;
    } //-- void setSuccess(boolean) 

    /**
     * Method setUpdateScmResult
     * 
     * @param updateScmResult
     */
    public void setUpdateScmResult(UpdateScmResult updateScmResult)
    {
        this.updateScmResult = updateScmResult;
    } //-- void setUpdateScmResult(UpdateScmResult) 

}
