/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;

/**
 * Class ShellProject.
 * 
 * @version $Revision$ $Date$
 */
public class ShellProject extends ContinuumProject 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field executable
     */
    private String executable;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getExecutable
     */
    public String getExecutable()
    {
        return this.executable;
    } //-- String getExecutable() 

    /**
     * Method setExecutable
     * 
     * @param executable
     */
    public void setExecutable(String executable)
    {
        this.executable = executable;
    } //-- void setExecutable(String) 

}
