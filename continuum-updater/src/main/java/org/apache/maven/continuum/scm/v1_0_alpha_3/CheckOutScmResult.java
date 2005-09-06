/*
 * $Id$
 */

package org.apache.maven.continuum.scm.v1_0_0;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;
import java.util.List;
import org.apache.maven.continuum.project.v1_0_0.AntProject;
import org.apache.maven.continuum.project.v1_0_0.ContinuumBuild;
import org.apache.maven.continuum.project.v1_0_0.ContinuumDeveloper;
import org.apache.maven.continuum.project.v1_0_0.ContinuumNotifier;
import org.apache.maven.continuum.project.v1_0_0.ContinuumProject;
import org.apache.maven.continuum.project.v1_0_0.MavenOneProject;
import org.apache.maven.continuum.project.v1_0_0.MavenTwoProject;
import org.apache.maven.continuum.project.v1_0_0.ShellProject;

/**
 * Class CheckOutScmResult.
 * 
 * @version $Revision$ $Date$
 */
public class CheckOutScmResult extends ScmResult 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field checkedOutFiles
     */
    private java.util.List checkedOutFiles;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addCheckedOutFile
     * 
     * @param scmFile
     */
    public void addCheckedOutFile(ScmFile scmFile)
    {
        getCheckedOutFiles().add( scmFile );
    } //-- void addCheckedOutFile(ScmFile) 

    /**
     * Method getCheckedOutFiles
     */
    public java.util.List getCheckedOutFiles()
    {
        if ( this.checkedOutFiles == null )
        {
            this.checkedOutFiles = new java.util.ArrayList();
        }
        
        return this.checkedOutFiles;
    } //-- java.util.List getCheckedOutFiles() 

    /**
     * Method removeCheckedOutFile
     * 
     * @param scmFile
     */
    public void removeCheckedOutFile(ScmFile scmFile)
    {
        getCheckedOutFiles().remove( scmFile );
    } //-- void removeCheckedOutFile(ScmFile) 

    /**
     * Method setCheckedOutFiles
     * 
     * @param checkedOutFiles
     */
    public void setCheckedOutFiles(java.util.List checkedOutFiles)
    {
        this.checkedOutFiles = checkedOutFiles;
    } //-- void setCheckedOutFiles(java.util.List) 

}
