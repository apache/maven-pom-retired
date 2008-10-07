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
public class RepositoryGAVMetadata
{
  ArtifactCoordinates gav;
  
  /** a list of last discovered snapshots, ordered descending */
  protected TreeSet<String> snapshots;

  /** a list of last discovered versions, ordered ascending */
  protected Collection<String> classifiers;
  
  /** GMT timestamp of the last metadata check */
  protected long lastCheck;

  /**
   * @param versions
   * @param lastCheck
   */
  public RepositoryGAVMetadata( ArtifactCoordinates gav, Collection<String> snapshots, Collection<String> classifiers )
  {
    this.gav = gav;

    this.snapshots = new TreeSet<String>( new VersionComparator() );
    
    if( !Util.isEmpty( snapshots ) )
      this.snapshots.addAll( snapshots );
    
    this.classifiers = classifiers;
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }

  public TreeSet<String> getSnapshots()
  {
    return snapshots;
  }

  public Collection<String> getClassifiers()
  {
    return classifiers;
  }

  /**
   * find the most recent snapshot timestamp
   * 
   * @return
   */
  public String getSnapshot()
  {
    return snapshots.last();
  }

  public long getLastCheck()
  {
    return lastCheck;
  }

  public void updateSnapshots( Collection<String> snapshots )
  {
    this.snapshots.clear();

    if( !Util.isEmpty( snapshots ) )
      this.snapshots.addAll( snapshots );
    
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }

  public void updateClassifiers( Collection<String> classifiers )
  {
    this.classifiers = classifiers;
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }
  
  
  public ArtifactCoordinates getGAV()
  {
    return gav;
  }

}
