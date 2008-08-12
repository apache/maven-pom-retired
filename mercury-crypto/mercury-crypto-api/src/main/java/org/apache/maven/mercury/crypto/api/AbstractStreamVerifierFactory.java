package org.apache.maven.mercury.crypto.api;

/**
 * Helper for implementing stream verifier factories, takes care of attributes
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
