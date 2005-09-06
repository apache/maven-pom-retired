/*
 * $Id$
 */

package org.apache.maven.continuum.scm.v1_0_0;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;
import org.apache.maven.continuum.project.v1_0_0.AntProject;
import org.apache.maven.continuum.project.v1_0_0.ContinuumBuild;
import org.apache.maven.continuum.project.v1_0_0.ContinuumDeveloper;
import org.apache.maven.continuum.project.v1_0_0.ContinuumNotifier;
import org.apache.maven.continuum.project.v1_0_0.ContinuumProject;
import org.apache.maven.continuum.project.v1_0_0.MavenOneProject;
import org.apache.maven.continuum.project.v1_0_0.MavenTwoProject;
import org.apache.maven.continuum.project.v1_0_0.ShellProject;

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
