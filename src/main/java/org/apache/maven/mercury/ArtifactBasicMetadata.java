package org.apache.maven.mercury;

import org.apache.maven.mercury.repository.api.RepositoryReader;

/**
 * this is the most primitive metadata there is, usually used to query repository for "real" metadata
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactBasicMetadata
{
  public static final String DEFAULT_ARTIFACT_TYPE = "jar";
  
  /** 
   * standard glorified artifact coordinates
   */
  protected String groupId;

  protected String artifactId;

  protected String version;

  // This is Maven specific. jvz/
  protected String classifier;

  protected String type = DEFAULT_ARTIFACT_TYPE;
  
  
  // oleg: resolution convenience transient data
  
  /** which reader found it */
  transient RepositoryReader _reader;
  
  //------------------------------------------------------------------
  /**
   * create basic out of <b>group:artifact:version:classifier:type</b> string, use 
   * empty string to specify missing component - for instance query for common-1.3.zip
   * can be specified as ":common:1.3::zip" - note missing groupId and classifier. 
   */
  public static ArtifactBasicMetadata create( String query )
  {
    ArtifactBasicMetadata mdq = new ArtifactBasicMetadata();
    
    if( query == null )
      return null;
    
    String [] tokens = query.split(":");
    
    if( tokens == null || tokens.length < 1 )
      return mdq;

    int count = tokens.length;
    
    mdq.groupId = nullify( tokens[0] );
  
    if( count > 1 )
      mdq.artifactId = nullify( tokens[1] );
    
    if( count > 2 )
      mdq.version = nullify( tokens[2] );
    
    if( count > 3 )
      mdq.classifier = nullify( tokens[3] );
    
    if( count > 4 )
      mdq.type = nullify( tokens[4] );
    
    return mdq;
  }
  //---------------------------------------------------------------------------
  private static final String nullify( String s )
  {
    if( s == null || s.length() < 1 )
      return null;
    return s;
  }
  //---------------------------------------------------------------------
  public boolean sameGAV( ArtifactMetadata md )
  {
    if( md == null )
      return false;
    
    return 
        sameGA( md )
        && version != null
        && version.equals( md.getVersion() )
    ;
  }
  //---------------------------------------------------------------------
  public boolean sameGA( ArtifactMetadata md )
  {
    if( md == null )
      return false;
    
    return
        groupId != null
        && artifactId != null
        && groupId.equals( md.getGroupId() )
        && artifactId.equals( md.getArtifactId() )
    ;
  }

  public String getGA()
  {
    return toDomainString();
  }

  public String getGAV()
  {
    return toString();
  }
  
  @Override
  public String toString()
  {
      return groupId + ":" + artifactId + ":" + version;
  }

  public String toDomainString()
  {
      return groupId + ":" + artifactId;
  }

  public String getCheckedType()
  {
      return type == null ? "jar" : type;
  }
  //---------------------------------------------------------------------------
  public String getGroupId()
  {
    return groupId;
  }
  public void setGroupId(
      String groupId )
  {
    this.groupId = groupId;
  }
  public String getArtifactId()
  {
    return artifactId;
  }
  public void setArtifactId(
      String artifactId )
  {
    this.artifactId = artifactId;
  }
  public String getVersion()
  {
    return version;
  }
  public void setVersion(
      String version )
  {
    this.version = version;
  }
  public String getClassifier()
  {
    return classifier;
  }
  public void setClassifier(
      String classifier )
  {
    this.classifier = classifier;
  }
  public String getType()
  {
    return type;
  }
  public void setType(
      String type )
  {
    this.type = type;
  }
  
  
  public RepositoryReader getReader()
  {
    return _reader;
  }
  public void setReader( RepositoryReader reader )
  {
    this._reader = reader;
  }
  
  
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
}
