/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.apache.maven.continuum.scm.v1_0_alpha_3.CheckOutScmResult;

import java.util.*;
import java.util.List;

/**
 * Class ContinuumProject.
 * 
 * @version $Revision$ $Date$
 */
public abstract class ContinuumProject implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field id
     */
    private String id;

    /**
     * Field name
     */
    private String name;

    /**
     * Field scmUrl
     */
    private String scmUrl;

    /**
     * Field version
     */
    private String version;

    /**
     * Field workingDirectory
     */
    private String workingDirectory;

    /**
     * Field state
     */
    private int state = 0;

    /**
     * Field executorId
     */
    private String executorId;

    /**
     * Field lastBuildId
     */
    private String lastBuildId;

    /**
     * Field previousBuildId
     */
    private String previousBuildId;

    /**
     * Field buildNumber
     */
    private int buildNumber = 0;

    /**
     * Field builds
     */
    private java.util.List builds;

    /**
     * Field checkOutScmResult
     */
    private CheckOutScmResult checkOutScmResult;

    /**
     * Field checkOutErrorMessage
     */
    private String checkOutErrorMessage;

    /**
     * Field checkOutErrorException
     */
    private String checkOutErrorException;

    /**
     * Field mailType
     */
    private String mailType;

    /**
     * Field commandLineArguments
     */
    private String commandLineArguments;

    /**
     * Field url
     */
    private String url;

    /**
     * Field groupId
     */
    private String groupId;

    /**
     * Field testOutputDirectory
     */
    private String testOutputDirectory;

    /**
     * Field developers
     */
    private java.util.List developers;

    /**
     * Field notifiers
     */
    private java.util.List notifiers;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addBuild
     * 
     * @param continuumBuild
     */
    public void addBuild(ContinuumBuild continuumBuild)
    {
        getBuilds().add( continuumBuild );
        continuumBuild.createContinuumProjectAssociation( this );
    } //-- void addBuild(ContinuumBuild) 

    /**
     * Method addDeveloper
     * 
     * @param continuumDeveloper
     */
    public void addDeveloper(ContinuumDeveloper continuumDeveloper)
    {
        getDevelopers().add( continuumDeveloper );
    } //-- void addDeveloper(ContinuumDeveloper) 

    /**
     * Method addNotifier
     * 
     * @param continuumNotifier
     */
    public void addNotifier(ContinuumNotifier continuumNotifier)
    {
        getNotifiers().add( continuumNotifier );
    } //-- void addNotifier(ContinuumNotifier) 

    /**
     * Method breakContinuumBuildAssociation
     * 
     * @param continuumBuild
     */
    public void breakContinuumBuildAssociation(ContinuumBuild continuumBuild)
    {
        if ( ! getBuilds().contains( continuumBuild ) )
        {
            throw new IllegalStateException( "continuumBuild isn't associated." );
        }
        
        getBuilds().remove( continuumBuild );
    } //-- void breakContinuumBuildAssociation(ContinuumBuild) 

    /**
     * Method createContinuumBuildAssociation
     * 
     * @param continuumBuild
     */
    public void createContinuumBuildAssociation(ContinuumBuild continuumBuild)
    {
        Collection builds = getBuilds();
        
        if ( getBuilds().contains(continuumBuild) )
        {
            throw new IllegalStateException( "continuumBuild is already assigned." );
        }
        
        builds.add( continuumBuild );
    } //-- void createContinuumBuildAssociation(ContinuumBuild) 

    /**
     * Method getBuildNumber
     */
    public int getBuildNumber()
    {
        return this.buildNumber;
    } //-- int getBuildNumber() 

    /**
     * Method getBuilds
     */
    public java.util.List getBuilds()
    {
        if ( this.builds == null )
        {
            this.builds = new java.util.ArrayList();
        }
        
        return this.builds;
    } //-- java.util.List getBuilds() 

    /**
     * Method getCheckOutErrorException
     */
    public String getCheckOutErrorException()
    {
        return this.checkOutErrorException;
    } //-- String getCheckOutErrorException() 

    /**
     * Method getCheckOutErrorMessage
     */
    public String getCheckOutErrorMessage()
    {
        return this.checkOutErrorMessage;
    } //-- String getCheckOutErrorMessage() 

    /**
     * Method getCheckOutScmResult
     */
    public CheckOutScmResult getCheckOutScmResult()
    {
        return this.checkOutScmResult;
    } //-- CheckOutScmResult getCheckOutScmResult() 

    /**
     * Method getCommandLineArguments
     */
    public String getCommandLineArguments()
    {
        return this.commandLineArguments;
    } //-- String getCommandLineArguments() 

    /**
     * Method getDevelopers
     */
    public java.util.List getDevelopers()
    {
        if ( this.developers == null )
        {
            this.developers = new java.util.ArrayList();
        }
        
        return this.developers;
    } //-- java.util.List getDevelopers() 

    /**
     * Method getExecutorId
     */
    public String getExecutorId()
    {
        return this.executorId;
    } //-- String getExecutorId() 

    /**
     * Method getGroupId
     */
    public String getGroupId()
    {
        return this.groupId;
    } //-- String getGroupId() 

    /**
     * Method getId
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Method getLastBuildId
     */
    public String getLastBuildId()
    {
        return this.lastBuildId;
    } //-- String getLastBuildId() 

    /**
     * Method getMailType
     */
    public String getMailType()
    {
        return this.mailType;
    } //-- String getMailType() 

    /**
     * Method getName
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Method getNotifiers
     */
    public java.util.List getNotifiers()
    {
        if ( this.notifiers == null )
        {
            this.notifiers = new java.util.ArrayList();
        }
        
        return this.notifiers;
    } //-- java.util.List getNotifiers() 

    /**
     * Method getPreviousBuildId
     */
    public String getPreviousBuildId()
    {
        return this.previousBuildId;
    } //-- String getPreviousBuildId() 

    /**
     * Method getScmUrl
     */
    public String getScmUrl()
    {
        return this.scmUrl;
    } //-- String getScmUrl() 

    /**
     * Method getState
     */
    public int getState()
    {
        return this.state;
    } //-- int getState() 

    /**
     * Method getTestOutputDirectory
     */
    public String getTestOutputDirectory()
    {
        return this.testOutputDirectory;
    } //-- String getTestOutputDirectory() 

    /**
     * Method getUrl
     */
    public String getUrl()
    {
        return this.url;
    } //-- String getUrl() 

    /**
     * Method getVersion
     */
    public String getVersion()
    {
        return this.version;
    } //-- String getVersion() 

    /**
     * Method getWorkingDirectory
     */
    public String getWorkingDirectory()
    {
        return this.workingDirectory;
    } //-- String getWorkingDirectory() 

    /**
     * Method removeBuild
     * 
     * @param continuumBuild
     */
    public void removeBuild(ContinuumBuild continuumBuild)
    {
        continuumBuild.breakContinuumProjectAssociation( this );
        getBuilds().remove( continuumBuild );
    } //-- void removeBuild(ContinuumBuild) 

    /**
     * Method removeDeveloper
     * 
     * @param continuumDeveloper
     */
    public void removeDeveloper(ContinuumDeveloper continuumDeveloper)
    {
        getDevelopers().remove( continuumDeveloper );
    } //-- void removeDeveloper(ContinuumDeveloper) 

    /**
     * Method removeNotifier
     * 
     * @param continuumNotifier
     */
    public void removeNotifier(ContinuumNotifier continuumNotifier)
    {
        getNotifiers().remove( continuumNotifier );
    } //-- void removeNotifier(ContinuumNotifier) 

    /**
     * Method setBuildNumber
     * 
     * @param buildNumber
     */
    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    } //-- void setBuildNumber(int) 

    /**
     * Method setBuilds
     * 
     * @param builds
     */
    public void setBuilds(java.util.List builds)
    {
        this.builds = builds;
    } //-- void setBuilds(java.util.List) 

    /**
     * Method setCheckOutErrorException
     * 
     * @param checkOutErrorException
     */
    public void setCheckOutErrorException(String checkOutErrorException)
    {
        this.checkOutErrorException = checkOutErrorException;
    } //-- void setCheckOutErrorException(String) 

    /**
     * Method setCheckOutErrorMessage
     * 
     * @param checkOutErrorMessage
     */
    public void setCheckOutErrorMessage(String checkOutErrorMessage)
    {
        this.checkOutErrorMessage = checkOutErrorMessage;
    } //-- void setCheckOutErrorMessage(String) 

    /**
     * Method setCheckOutScmResult
     * 
     * @param checkOutScmResult
     */
    public void setCheckOutScmResult(CheckOutScmResult checkOutScmResult)
    {
        this.checkOutScmResult = checkOutScmResult;
    } //-- void setCheckOutScmResult(CheckOutScmResult) 

    /**
     * Method setCommandLineArguments
     * 
     * @param commandLineArguments
     */
    public void setCommandLineArguments(String commandLineArguments)
    {
        this.commandLineArguments = commandLineArguments;
    } //-- void setCommandLineArguments(String) 

    /**
     * Method setDevelopers
     * 
     * @param developers
     */
    public void setDevelopers(java.util.List developers)
    {
        this.developers = developers;
    } //-- void setDevelopers(java.util.List) 

    /**
     * Method setExecutorId
     * 
     * @param executorId
     */
    public void setExecutorId(String executorId)
    {
        this.executorId = executorId;
    } //-- void setExecutorId(String) 

    /**
     * Method setGroupId
     * 
     * @param groupId
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    } //-- void setGroupId(String) 

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
     * Method setLastBuildId
     * 
     * @param lastBuildId
     */
    public void setLastBuildId(String lastBuildId)
    {
        this.lastBuildId = lastBuildId;
    } //-- void setLastBuildId(String) 

    /**
     * Method setMailType
     * 
     * @param mailType
     */
    public void setMailType(String mailType)
    {
        this.mailType = mailType;
    } //-- void setMailType(String) 

    /**
     * Method setName
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Method setNotifiers
     * 
     * @param notifiers
     */
    public void setNotifiers(java.util.List notifiers)
    {
        this.notifiers = notifiers;
    } //-- void setNotifiers(java.util.List) 

    /**
     * Method setPreviousBuildId
     * 
     * @param previousBuildId
     */
    public void setPreviousBuildId(String previousBuildId)
    {
        this.previousBuildId = previousBuildId;
    } //-- void setPreviousBuildId(String) 

    /**
     * Method setScmUrl
     * 
     * @param scmUrl
     */
    public void setScmUrl(String scmUrl)
    {
        this.scmUrl = scmUrl;
    } //-- void setScmUrl(String) 

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
     * Method setTestOutputDirectory
     * 
     * @param testOutputDirectory
     */
    public void setTestOutputDirectory(String testOutputDirectory)
    {
        this.testOutputDirectory = testOutputDirectory;
    } //-- void setTestOutputDirectory(String) 

    /**
     * Method setUrl
     * 
     * @param url
     */
    public void setUrl(String url)
    {
        this.url = url;
    } //-- void setUrl(String) 

    /**
     * Method setVersion
     * 
     * @param version
     */
    public void setVersion(String version)
    {
        this.version = version;
    } //-- void setVersion(String) 

    /**
     * Method setWorkingDirectory
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    } //-- void setWorkingDirectory(String) 

}
