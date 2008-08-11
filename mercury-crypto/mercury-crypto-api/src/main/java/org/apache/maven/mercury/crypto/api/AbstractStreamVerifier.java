package org.apache.maven.mercury.crypto.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractStreamVerifier
{
  protected StreamVerifierAttributes attributes;

  /**
   * @param attributes
   */
  public AbstractStreamVerifier( StreamVerifierAttributes attributes )
  {
    this.attributes = attributes;
  }

  public StreamVerifierAttributes getAttributes()
  {
    return attributes;
  }
  
}
