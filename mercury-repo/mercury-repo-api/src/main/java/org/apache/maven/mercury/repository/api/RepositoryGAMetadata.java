package org.apache.maven.mercury.repository.api;

import java.util.Collection;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.artifact.version.VersionComparator;
import org.apache.maven.mercury.util.TimeUtil;
import org.apache.maven.mercury.util.Util;

/**
 * This is a data object to carry GA level repository 
 * metadata, namely - a list of versions and last check timestamp
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryGAMetadata
{
  ArtifactCoordinates ga;

  /** a list of last discovered versions, ordered ascending */
  protected TreeSet<String> versions;
  
  /** GMT timestamp of the last metadata check */
  protected long lastCheck;

  /**
   * @param versions
   * @param lastCheck
   */
  public RepositoryGAMetadata( ArtifactCoordinates ga, Collection<String> versions )
  {
    this.ga = ga;
    this.versions = new TreeSet<String>( new VersionComparator() );
    
    if( ! Util.isEmpty( versions ) )
      this.versions.addAll( versions );
    
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }

  public TreeSet<String> getVersions()
  {
    return versions;
  }

  public long getLastCheck()
  {
    return lastCheck;
  }
  
  public void update( Collection<String> versions )
  {
    this.versions.addAll( versions );
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }
  
  public ArtifactCoordinates getGA()
  {
    return ga;
  }

}
