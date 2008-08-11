package org.apache.maven.mercury.crypto.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class StreamVerifierAttributes
{
  protected boolean isLenient = true;
  protected boolean isSufficient = false;
  protected String  extension = "none";
  
  /**
   * 
   */
  public StreamVerifierAttributes( String extension, boolean isLenient, boolean isSufficient)
  {
    this.extension = extension;
    this.isLenient = isLenient;
    this.isSufficient = isSufficient;
  }
  
  /**
   * 
   */
  public StreamVerifierAttributes()
  {
  }

  public boolean isLenient()
  {
      return isLenient;
  }

  public boolean isSufficient()
  {
      return isSufficient;
  }

  public String getExtension()
  {
    return extension == null
             ? extension
             : extension.startsWith( "." )
                         ? extension 
                         : "."+extension
           ;
  }
  
  
}
