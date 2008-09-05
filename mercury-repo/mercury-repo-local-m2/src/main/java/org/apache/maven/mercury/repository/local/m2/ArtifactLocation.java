package org.apache.maven.mercury.repository.local.m2;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;

public class ArtifactLocation
{
  public static final String SEP = "/";
  public static final char   SEP_CHAR = SEP.charAt( 0 );
  public static final String DASH = "-";
  public static final char   DASH_CHAR = DASH.charAt( 0 );
  public static final String POM_EXT = ".pom";

  private String prefix;
  
  private String gaPath;
  private String versionDir;
  private String baseName;
  private String version;
  private String classifier;
  private String type;
  
  private ArtifactBasicMetadata bmd;
  
  public ArtifactLocation( String prefix, ArtifactBasicMetadata bmd )
  {
    if( prefix == null || bmd == null || bmd.getGroupId() == null || bmd.getArtifactId() == null || bmd.getVersion() == null )
      return;
    
    this.bmd = bmd;

    this.prefix     = prefix;
    this.gaPath     = bmd.getGroupId().replace( '.', SEP_CHAR ) + SEP + bmd.getArtifactId();
    this.version    = bmd.getVersion();
    this.baseName   = bmd.getArtifactId();
    this.versionDir = this.version;
    this.classifier = bmd.getClassifier();
    this.type       = bmd.getType();
  }
  
  public String getRelPath()
  {
    return gaPath+SEP+versionDir+SEP+baseName+DASH+version+getDashedClassifier()+'.'+type;
  }
  
  public String getRelPomPath()
  {
    return gaPath+SEP+versionDir+SEP+baseName+DASH+version+POM_EXT;
  }
  
  public String getAbsPath()
  {
    if( prefix == null )
      return null;

    return getSeparatedPrefix() + getRelPath();
  }
  
  public String getAbsPomPath()
  {
    if( prefix == null )
      return null;

    return getSeparatedPrefix() + getRelPomPath();
  }
  
  public String getGavPath()
  {
    return getGaPath()+SEP+versionDir;
  }
  
  public String getBaseVersion()
  {
    if( version == null )
      return null;
    
    DefaultArtifactVersion dav = new DefaultArtifactVersion( version );
    return dav.getBase();
  }
  
  //---------------------------------------------------------
  public String getGaPath()
  {
    return gaPath;
  }
  public void setGaPath( String gaPath )
  {
    this.gaPath = gaPath;
  }
  public String getVersionDir()
  {
    return versionDir;
  }
  public void setVersionDir( String versionDir )
  {
    this.versionDir = versionDir;
  }
  public String getBaseName()
  {
    return baseName;
  }
  public void setBaseName( String baseName )
  {
    this.baseName = baseName;
  }
  public String getVersion()
  {
    return version;
  }
  public void setVersion( String version )
  {
    this.version = version;
  }
  public String getClassifier()
  {
    return classifier;
  }
  public String getDashedClassifier()
  {
    return (classifier == null||classifier.length()<1) ? "" : DASH+classifier;
  }
  public void setClassifier( String classifier )
  {
    this.classifier = classifier;
  }
  public String getType()
  {
    return type;
  }
  public void setType( String type )
  {
    this.type = type;
  }
  public String getPrefix()
  {
    return prefix;
  }
  public String getSeparatedPrefix()
  {
    if( prefix == null )
      return null;

    return prefix+(prefix.endsWith( SEP ) ? "" : SEP);
  }
  public void setPrefix( String prefix )
  {
    this.prefix = prefix;
  }

  @Override
  public String toString()
  {
    return bmd == null ? "no ArtifactBasicMetadata" : bmd.toString();
  }
  
}