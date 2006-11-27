/*
 * $Id$
 */

package org.apache.maven.continuum.scm.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;

/**
 * Class ScmFile.
 * 
 * @version $Revision$ $Date$
 */
public class ScmFile implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field path
     */
    private String path;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getPath
     */
    public String getPath()
    {
        return this.path;
    } //-- String getPath() 

    /**
     * Method setPath
     * 
     * @param path
     */
    public void setPath(String path)
    {
        this.path = path;
    } //-- void setPath(String) 

}
