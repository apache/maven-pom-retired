package org.apache.maven.mercury.artifact.version;

/**
 * interface to the version range processor. To be implemented for various syntaxes/interpreters
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface VersionRange
{
  /** returns true if the supplied version fits into the range */
  public boolean includes( String version );
}
