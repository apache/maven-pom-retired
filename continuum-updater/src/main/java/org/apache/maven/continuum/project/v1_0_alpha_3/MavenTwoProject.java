/*
 * $Id$
 */

package org.apache.maven.continuum.project.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;

/**
 * Class MavenTwoProject.
 * 
 * @version $Revision$ $Date$
 */
public class MavenTwoProject extends ContinuumProject 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field goals
     */
    private String goals;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getGoals
     */
    public String getGoals()
    {
        return this.goals;
    } //-- String getGoals() 

    /**
     * Method setGoals
     * 
     * @param goals
     */
    public void setGoals(String goals)
    {
        this.goals = goals;
    } //-- void setGoals(String) 

}
