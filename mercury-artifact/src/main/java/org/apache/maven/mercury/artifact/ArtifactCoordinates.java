package org.apache.maven.mercury.artifact;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactCoordinates
{
  
  /** 
   * standard glorified artifact coordinates
   */
  protected String groupId;

  protected String artifactId;

  protected String version;
  
  /**
   * @param groupId
   * @param artifactId
   * @param version
   */
  public ArtifactCoordinates(
                        String groupId,
                        String artifactId,
                        String version
                                )
  {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

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
}
