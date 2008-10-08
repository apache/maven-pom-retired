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
public class RepositoryGAMetadata
{
  private static final Language _lang = new DefaultLanguage( RepositoryGAVMetadata.class );
  
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

  /**
   * @param versions
   * @param lastCheck
   * @throws MetadataException 
   */
  public RepositoryGAMetadata( byte [] mdBytes )
  throws MetadataException
  {
    if( Util.isEmpty( mdBytes ) )
      throw new IllegalArgumentException( _lang.getMessage( "empty.mdbytes" ) );
    
    Metadata md = MetadataBuilder.getMetadata( mdBytes );
    
    this.ga = new ArtifactCoordinates( md.getGroupId(), md.getArtifactId(), md.getVersion() );

    this.versions = new TreeSet<String>( new VersionComparator() );
    
    List<String> vers = null;
    
    if( md.getVersioning() != null )
    {
      vers = md.getVersioning().getVersions();
      this.versions.addAll( vers );
    }
      
    
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
