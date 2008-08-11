package org.apache.maven.mercury.crypto.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractStreamVerifierFactory
{
  protected StreamVerifierAttributes attributes;

  /**
   * @param attributes
   */
  public AbstractStreamVerifierFactory( StreamVerifierAttributes attributes )
  {
    this.attributes = attributes;
  }

  public StreamVerifierAttributes getAttributes()
  {
    return attributes == null ? new StreamVerifierAttributes() : attributes;
  }
  
}
