package org.apache.maven.mercury.artifact.version;

import org.apache.maven.mercury.artifact.QualityRange;

/**
 * lack of IoC container makes me throw this class in.
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class VersionRangeFactory
{
  
  public static VersionRange create( String version )
  throws VersionException
  {
    return new MavenVersionRange( version );
  }
  
  public static VersionRange create( String version, final QualityRange qRange )
  throws VersionException
  {
    return new MavenVersionRange( version, qRange );
  }

}
