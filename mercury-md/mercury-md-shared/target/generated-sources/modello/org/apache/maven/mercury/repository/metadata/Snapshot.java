/*
 * $Id$
 */

package org.apache.maven.mercury.repository.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Snapshot data for the current version
 * 
 * @version $Revision$ $Date$
 */
public class Snapshot implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field timestamp
     */
    private String timestamp;

    /**
     * Field buildNumber
     */
    private int buildNumber = 0;

    /**
     * Field localCopy
     */
    private boolean localCopy = false;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get The incremental build number
     */
    public int getBuildNumber()
    {
        return this.buildNumber;
    } //-- int getBuildNumber() 

    /**
     * Get The time it was deployed
     */
    public String getTimestamp()
    {
        return this.timestamp;
    } //-- String getTimestamp() 

    /**
     * Get Whether to use a local copy instead (with filename that
     * includes the base version)
     */
    public boolean isLocalCopy()
    {
        return this.localCopy;
    } //-- boolean isLocalCopy() 

    /**
     * Set The incremental build number
     * 
     * @param buildNumber
     */
    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    } //-- void setBuildNumber(int) 

    /**
     * Set Whether to use a local copy instead (with filename that
     * includes the base version)
     * 
     * @param localCopy
     */
    public void setLocalCopy(boolean localCopy)
    {
        this.localCopy = localCopy;
    } //-- void setLocalCopy(boolean) 

    /**
     * Set The time it was deployed
     * 
     * @param timestamp
     */
    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    } //-- void setTimestamp(String) 


    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }
}
