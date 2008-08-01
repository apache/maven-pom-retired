/*
 * $Id$
 */

package org.apache.maven.mercury.repository.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Mapping information for a single plugin within this group
 * 
 * @version $Revision$ $Date$
 */
public class Plugin implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name
     */
    private String name;

    /**
     * Field prefix
     */
    private String prefix;

    /**
     * Field artifactId
     */
    private String artifactId;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get The plugin artifactId
     */
    public String getArtifactId()
    {
        return this.artifactId;
    } //-- String getArtifactId() 

    /**
     * Get Display name for the plugin.
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Get The plugin invocation prefix (i.e. eclipse for
     * eclipse:eclipse)
     */
    public String getPrefix()
    {
        return this.prefix;
    } //-- String getPrefix() 

    /**
     * Set The plugin artifactId
     * 
     * @param artifactId
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    } //-- void setArtifactId(String) 

    /**
     * Set Display name for the plugin.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set The plugin invocation prefix (i.e. eclipse for
     * eclipse:eclipse)
     * 
     * @param prefix
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    } //-- void setPrefix(String) 


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
