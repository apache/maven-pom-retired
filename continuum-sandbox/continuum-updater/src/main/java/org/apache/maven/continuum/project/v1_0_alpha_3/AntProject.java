/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;

/**
 * Class AntProject.
 * 
 * @version $Revision$ $Date$
 */
public class AntProject extends ContinuumProject 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field executable
     */
    private String executable;

    /**
     * Field targets
     */
    private String targets;


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
     * Method getTargets
     */
    public String getTargets()
    {
        return this.targets;
    } //-- String getTargets() 

    /**
     * Method setExecutable
     * 
     * @param executable
     */
    public void setExecutable(String executable)
    {
        this.executable = executable;
    } //-- void setExecutable(String) 

    /**
     * Method setTargets
     * 
     * @param targets
     */
    public void setTargets(String targets)
    {
        this.targets = targets;
    } //-- void setTargets(String) 

}
