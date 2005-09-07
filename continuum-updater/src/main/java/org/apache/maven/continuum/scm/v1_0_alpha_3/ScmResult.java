/*
 * $Id$
 */

package org.apache.maven.continuum.scm.v1_0_alpha_3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.*;

/**
 * Class ScmResult.
 * 
 * @version $Revision$ $Date$
 */
public abstract class ScmResult implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field success
     */
    private boolean success = false;

    /**
     * Field providerMessage
     */
    private String providerMessage;

    /**
     * Field commandOutput
     */
    private String commandOutput;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getCommandOutput
     */
    public String getCommandOutput()
    {
        return this.commandOutput;
    } //-- String getCommandOutput() 

    /**
     * Method getProviderMessage
     */
    public String getProviderMessage()
    {
        return this.providerMessage;
    } //-- String getProviderMessage() 

    /**
     * Method isSuccess
     */
    public boolean isSuccess()
    {
        return this.success;
    } //-- boolean isSuccess() 

    /**
     * Method setCommandOutput
     * 
     * @param commandOutput
     */
    public void setCommandOutput(String commandOutput)
    {
        this.commandOutput = commandOutput;
    } //-- void setCommandOutput(String) 

    /**
     * Method setProviderMessage
     * 
     * @param providerMessage
     */
    public void setProviderMessage(String providerMessage)
    {
        this.providerMessage = providerMessage;
    } //-- void setProviderMessage(String) 

    /**
     * Method setSuccess
     * 
     * @param success
     */
    public void setSuccess(boolean success)
    {
        this.success = success;
    } //-- void setSuccess(boolean) 

}
