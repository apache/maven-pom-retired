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
  /**
   * returns true if the range is soft. i.e. 
   *  1). is not "hard" [|(x,y)|]
   *  2). allows (,) on the all possible versions in the current tree
   * 
   * @param version to test
   * @return 
   */
  public boolean isSoft();
}
