package org.apache.maven.mercury.repository.api;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.artifact.version.VersionComparator;
import org.apache.maven.mercury.repository.metadata.Metadata;
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
public class RepositoryGAMetadata
{
  private static final Language _lang = new DefaultLanguage( RepositoryGAVMetadata.class );
  
  protected ArtifactCoordinates ga;

  /** a list of last discovered versions, ordered ascending */
  protected TreeSet<String> versions = new TreeSet<String>( new VersionComparator() );
  
  /** GMT timestamp of the last metadata check */
  protected long lastCheck;
  
  /** is set true by cache implementation when determined that it's time to refresh */
  protected transient boolean expired = false;

  protected RepositoryGAMetadata()
  {
  }

  /**
   * @param versions
   * @param lastCheck
   */
  public RepositoryGAMetadata( ArtifactCoordinates ga, Collection<String> versions )
  {
    this.ga = ga;
    
    if( ! Util.isEmpty( versions ) )
      this.versions.addAll( versions );
    
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }

  /**
   * construct from maven 2.x maven-metadata.xml object
   * 
   * @param md
   * @throws MetadataException 
   */
  public RepositoryGAMetadata( Metadata md )
  throws MetadataException
  {
    if( md == null )
      throw new IllegalArgumentException( _lang.getMessage( "empty.md" ) );
    
    this.ga = new ArtifactCoordinates( md.getGroupId(), md.getArtifactId(), md.getVersion() );

    List<String> vers = null;
    
    if( md.getVersioning() != null )
    {
      vers = md.getVersioning().getVersions();
      this.versions.addAll( vers );
    }
    
    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }
  
  /**
   * copy constructor
   * 
   * @param md
   * @throws MetadataException 
   */
  public RepositoryGAMetadata( RepositoryGAMetadata md )
  throws MetadataException
  {    
    this.ga = md.getGA();

    if( !Util.isEmpty( md.getVersions() ) )
    {
      this.versions.addAll( md.getVersions() );
    }

    this.lastCheck = TimeUtil.getUTCTimestampAsLong();
  }

  public TreeSet<String> getVersions()
  {
    return versions;
  }

  public long getLastCheckTs()
  {
    return lastCheck;
  }
  
  public long getLastCheckMillis()
  throws ParseException
  {
    return TimeUtil.toMillis(  lastCheck );
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

  public boolean isExpired()
  {
    return expired;
  }

  public void setExpired( boolean expired )
  {
    this.expired = expired;
  }

}
