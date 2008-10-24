package org.apache.maven.mercury.artifact.version;

import org.apache.maven.mercury.artifact.api.Configurable;

/**
 * interface to the version range processor. To be implemented for various syntaxes/interpreters
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface VersionRange
extends Configurable
{
  /**
   * returns true if the supplied version fits into the range
   * 
   * @param version to test
   * @return 
   */
  public boolean includes( String version );
}
