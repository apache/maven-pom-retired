package org.apache.maven.mercury.crypto.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractVerifier
{
  protected boolean isLenient;
  protected boolean isSufficient;
  
  /**
   * 
   */
  public AbstractVerifier(boolean isLenient, boolean isSufficient)
  {
    this.isLenient = isLenient;
    this.isSufficient = isSufficient;
  }
  
  public void setLenient (boolean lenient)
  {
      isLenient = lenient;
  }

  public boolean isLenient()
  {
      return isLenient;
  }

  public void setSufficient (boolean sufficient)
  {
      isSufficient = sufficient;
  }
  public boolean isSufficient()
  {
      return isSufficient;
  }

}
