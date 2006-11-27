/*
 * $Id$
 */

package org.apache.maven.continuum.scm.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;
import java.util.List;

/**
 * Class UpdateScmResult.
 * 
 * @version $Revision$ $Date$
 */
public class UpdateScmResult extends ScmResult 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field updatedFiles
     */
    private java.util.List updatedFiles;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addUpdatedFile
     * 
     * @param scmFile
     */
    public void addUpdatedFile(ScmFile scmFile)
    {
        getUpdatedFiles().add( scmFile );
    } //-- void addUpdatedFile(ScmFile) 

    /**
     * Method getUpdatedFiles
     */
    public java.util.List getUpdatedFiles()
    {
        if ( this.updatedFiles == null )
        {
            this.updatedFiles = new java.util.ArrayList();
        }
        
        return this.updatedFiles;
    } //-- java.util.List getUpdatedFiles() 

    /**
     * Method removeUpdatedFile
     * 
     * @param scmFile
     */
    public void removeUpdatedFile(ScmFile scmFile)
    {
        getUpdatedFiles().remove( scmFile );
    } //-- void removeUpdatedFile(ScmFile) 

    /**
     * Method setUpdatedFiles
     * 
     * @param updatedFiles
     */
    public void setUpdatedFiles(java.util.List updatedFiles)
    {
        this.updatedFiles = updatedFiles;
    } //-- void setUpdatedFiles(java.util.List) 

}
