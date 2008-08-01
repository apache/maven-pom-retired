/*
 * $Id$
 */

package org.apache.maven.mercury.repository.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Versioning information for an artifact
 * 
 * @version $Revision$ $Date$
 */
public class Versioning implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field latest
     */
    private String latest;

    /**
     * Field release
     */
    private String release;

    /**
     * Field snapshot
     */
    private Snapshot snapshot;

    /**
     * Field versions
     */
    private java.util.List versions;

    /**
     * Field lastUpdated
     */
    private String lastUpdated;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addVersion
     * 
     * @param string
     */
    public void addVersion(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Versioning.addVersions(string) parameter must be instanceof " + String.class.getName() );
        }
        getVersions().add( string );
    } //-- void addVersion(String) 

    /**
     * Get When the metadata was last updated
     */
    public String getLastUpdated()
    {
        return this.lastUpdated;
    } //-- String getLastUpdated() 

    /**
     * Get What the latest version in the directory is, including
     * snapshots
     */
    public String getLatest()
    {
        return this.latest;
    } //-- String getLatest() 

    /**
     * Get What the latest version in the directory is, of the
     * releases
     */
    public String getRelease()
    {
        return this.release;
    } //-- String getRelease() 

    /**
     * Get The current snapshot data in use for this version
     */
    public Snapshot getSnapshot()
    {
        return this.snapshot;
    } //-- Snapshot getSnapshot() 

    /**
     * Method getVersions
     */
    public java.util.List getVersions()
    {
        if ( this.versions == null )
        {
            this.versions = new java.util.ArrayList();
        }
        
        return this.versions;
    } //-- java.util.List getVersions() 

    /**
     * Method removeVersion
     * 
     * @param string
     */
    public void removeVersion(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "Versioning.removeVersions(string) parameter must be instanceof " + String.class.getName() );
        }
        getVersions().remove( string );
    } //-- void removeVersion(String) 

    /**
     * Set When the metadata was last updated
     * 
     * @param lastUpdated
     */
    public void setLastUpdated(String lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    } //-- void setLastUpdated(String) 

    /**
     * Set What the latest version in the directory is, including
     * snapshots
     * 
     * @param latest
     */
    public void setLatest(String latest)
    {
        this.latest = latest;
    } //-- void setLatest(String) 

    /**
     * Set What the latest version in the directory is, of the
     * releases
     * 
     * @param release
     */
    public void setRelease(String release)
    {
        this.release = release;
    } //-- void setRelease(String) 

    /**
     * Set The current snapshot data in use for this version
     * 
     * @param snapshot
     */
    public void setSnapshot(Snapshot snapshot)
    {
        this.snapshot = snapshot;
    } //-- void setSnapshot(Snapshot) 

    /**
     * Set Versions available for the artifact
     * 
     * @param versions
     */
    public void setVersions(java.util.List versions)
    {
        this.versions = versions;
    } //-- void setVersions(java.util.List) 


            public void updateTimestamp()
            {
                setLastUpdatedTimestamp( new java.util.Date() );
            }

            public void setLastUpdatedTimestamp( java.util.Date date )
            {
                java.util.TimeZone timezone = java.util.TimeZone.getTimeZone( "UTC" );
                java.text.DateFormat fmt = new java.text.SimpleDateFormat( "yyyyMMddHHmmss" );
                fmt.setTimeZone( timezone );
                setLastUpdated( fmt.format( date ) );
            }
          
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
