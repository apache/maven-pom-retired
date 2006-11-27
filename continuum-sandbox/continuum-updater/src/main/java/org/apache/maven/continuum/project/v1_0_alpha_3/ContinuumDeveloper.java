/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;

/**
 * Class ContinuumDeveloper.
 * 
 * @version $Revision$ $Date$
 */
public class ContinuumDeveloper implements java.io.Serializable {


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
     * Field email
     */
    private String email;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getEmail
     */
    public String getEmail()
    {
        return this.email;
    } //-- String getEmail() 

    /**
     * Method getId
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Method getName
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Method setEmail
     * 
     * @param email
     */
    public void setEmail(String email)
    {
        this.email = email;
    } //-- void setEmail(String) 

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
     * Method setName
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

}
