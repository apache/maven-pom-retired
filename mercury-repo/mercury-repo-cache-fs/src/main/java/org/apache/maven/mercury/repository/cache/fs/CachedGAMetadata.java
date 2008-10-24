package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.repository.api.MetadataCorruptionException;
import org.apache.maven.mercury.repository.api.RepositoryGAMetadata;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class CachedGAMetadata
extends RepositoryGAMetadata
{
  public static final String ELEM_COORDINATES = "coordinates";
  public static final String ATTR_GROUP_ID = "groupId";
  public static final String ATTR_ARTIFACT_ID = "artifactId";
  public static final String ATTR_VERSION = "version";

  public static final String ELEM_VERSIONS = "versions";

  CachedMetadata cm;
  
  public CachedGAMetadata( File mdFile )
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataCorruptionException
  {
    cm = new CachedMetadata( mdFile );
    fromXml();
  }
  
  public CachedGAMetadata( RepositoryGAMetadata md )
  throws MetadataException
  {
    super( md );
    
    cm = new CachedMetadata();
    
    toXml();
  }
  
  /**
   * fill GA with data from cm
   * 
   * @throws MetadataCorruptionException 
   */
  private void fromXml()
  throws MetadataCorruptionException
  {
    ga = new ArtifactCoordinates( 
                    cm.getAttribute( ELEM_COORDINATES, ATTR_GROUP_ID, true ) 
                  , cm.getAttribute( ELEM_COORDINATES, ATTR_ARTIFACT_ID, true ) 
                  , null 
                                );
    
    List<String> verList = cm.findAttributes( ELEM_VERSIONS, ATTR_VERSION );
    
    if( ! Util.isEmpty( verList ) )
      this.versions.addAll( verList );

    String lChk = cm.getLastUpdate();

    lastCheck = Long.parseLong( lChk );
  }
  
  private void toXml()
  {
    cm.clean();
    
    cm.setAttribute( ELEM_COORDINATES, ATTR_GROUP_ID, ga.getGroupId() );
    cm.setAttribute( ELEM_COORDINATES, ATTR_ARTIFACT_ID, ga.getArtifactId() );
    
    if( !Util.isEmpty( versions ) )
      cm.setAttribute( ELEM_VERSIONS, ATTR_VERSION, versions );
    
    cm.setLastUpdate( ""+lastCheck );
  }
  
}
