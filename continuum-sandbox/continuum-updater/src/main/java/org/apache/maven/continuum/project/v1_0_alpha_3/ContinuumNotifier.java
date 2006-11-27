/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;
import java.util.Map;

/**
 * Class ContinuumNotifier.
 * 
 * @version $Revision$ $Date$
 */
public class ContinuumNotifier implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field type
     */
    private String type = "mail";

    /**
     * Field configuration
     */
    private java.util.Map configuration;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addConfiguration
     * 
     * @param key
     * @param value
     */
    public void addConfiguration(Object key, String value)
    {
        getConfiguration().put( key, value );
    } //-- void addConfiguration(Object, String) 

    /**
     * Method getConfiguration
     */
    public java.util.Map getConfiguration()
    {
        if ( this.configuration == null )
        {
            this.configuration = new java.util.HashMap();
        }
        
        return this.configuration;
    } //-- java.util.Map getConfiguration() 

    /**
     * Method getType
     */
    public String getType()
    {
        return this.type;
    } //-- String getType() 

    /**
     * Method setConfiguration
     * 
     * @param configuration
     */
    public void setConfiguration(java.util.Map configuration)
    {
        this.configuration = configuration;
    } //-- void setConfiguration(java.util.Map) 

    /**
     * Method setType
     * 
     * @param type
     */
    public void setType(String type)
    {
        this.type = type;
    } //-- void setType(String) 

}
