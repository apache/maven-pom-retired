package org.apache.maven.mercury.repository.api;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.artifact.version.VersionComparator;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.util.TimeUtil;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

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
  private static final Language _lang = new DefaultLanguage( RepositoryGAVMetadata.class );
  
  protected ArtifactCoordinates gav;
  
  /** a list of last discovered snapshots, ordered descending */
  protected TreeSet<String> snapshots = new TreeSet<String>( new VersionComparator() );

  /** a list of last discovered versions, ordered ascending */
  protected Collection<String> classifiers;
  
  /** GMT timestamp of the last metadata check */
  protected long lastCheck;
  
  /** is set true by cache implementation when determined that it's time to refresh */
  protected transient boolean expired = false;
  
  protected RepositoryGAVMetadata()
  {
  }

  /**
   * initialization of md object from scratch
   * 
   * @param versions
   * @param lastCheck
   */
  public RepositoryGAVMetadata( ArtifactCoordinates gav, Collection<String> snapshots, Collection<String> classifiers )
  {
    this.gav = gav;

    if( !Util.isEmpty( snapshots ) )
      this.snapshots.addAll( snapshots );
    
    this.classifiers = classifiers;
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }
  
  /**
   * construct from maven 2.x maven-metadata.xml object
   * 
   * @param md
   * @throws MetadataException 
   */
  public RepositoryGAVMetadata( Metadata md )
  throws MetadataException
  {
    if( md == null )
      throw new IllegalArgumentException( _lang.getMessage( "empty.mdbytes" ) );
    
    this.gav = new ArtifactCoordinates( md.getGroupId(), md.getArtifactId(), md.getVersion() );

    List<String> versions = null;
    
    if( md.getVersioning() != null )
      versions = md.getVersioning().getVersions(); 
      
    if( !Util.isEmpty( versions ) )
      this.snapshots.addAll( versions );
    
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }

  /**
   * copy constructor
   * 
   * @param md
   * @throws MetadataException 
   */
  public RepositoryGAVMetadata( RepositoryGAVMetadata md )
  throws MetadataException
  {
    this.gav = md.getGAV();

    if( ! Util.isEmpty( md.getSnapshots() ) )
      this.snapshots.addAll( md.getSnapshots() );
    
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

  public boolean isExpired()
  {
    return expired;
  }

  public void setExpired( boolean expired )
  {
    this.expired = expired;
  }

}
