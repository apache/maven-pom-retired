package org.apache.maven.mercury.artifact.version;

import java.util.Collection;

import org.apache.maven.mercury.artifact.Artifact;
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
  
  public static VersionRange create( final String version )
  throws VersionException
  {
    return new MavenVersionRange( version );
  }
  
  public static VersionRange create( final String version, final QualityRange qRange )
  throws VersionException
  {
    return new MavenVersionRange( version, qRange );
  }
  //--------------------------------------------------------------------------------------------
  /**
   * helpful latest version calculator
   * 
   * @param versions
   * @param noSnapshots
   * @return
   */
  public static final String findLatest( final Collection<String> versions, final boolean noSnapshots )
  {
    DefaultArtifactVersion tempDav = null;
    DefaultArtifactVersion tempDav2 = null;
    String version = null;

    // find latest
    for( String vn : versions )
    {
      // RELEASE?
      if( noSnapshots && vn.endsWith( Artifact.SNAPSHOT_VERSION ))
        continue;
      
      if( version == null )
      {
        version = vn;
        tempDav = new DefaultArtifactVersion( vn );
        continue;
      }
      
      tempDav2 = new DefaultArtifactVersion( vn );
      if( tempDav2.compareTo( tempDav ) > 0 )
      {
        version = vn;
        tempDav = tempDav2;
      }
    }
    return version;
  }

}
