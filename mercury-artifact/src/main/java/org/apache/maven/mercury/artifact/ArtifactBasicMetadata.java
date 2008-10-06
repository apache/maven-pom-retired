package org.apache.maven.mercury.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;


/**
 * this is the most primitive metadata there is, usually used to query repository for "real" metadata.
 * It holds everything a project.dependencies.dependency element can have
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
  
  /** 
   * relocation chain after processing by
   * ProjectBuilder
   */
  protected List<ArtifactCoordinates> relocations;
  protected ArtifactCoordinates effectiveCoordinates;

  // This is Maven specific. jvz/
  protected String classifier;

  protected String type = DEFAULT_ARTIFACT_TYPE;
  
  protected ArtifactScopeEnum artifactScope;

  protected String scope;

  protected boolean optional;
  
  protected Collection<ArtifactBasicMetadata> inclusions;
  
  protected Collection<ArtifactBasicMetadata> exclusions;

  /** transient helper objects, used by DependencyBuilder */
  transient Object tracker;
  transient Boolean local = false;
    
  //------------------------------------------------------------------
  public ArtifactBasicMetadata()
  {
  }
  //------------------------------------------------------------------
  /**
   * create basic out of <b>group:artifact:version:classifier:type</b> string, use 
   * empty string to specify missing component - for instance query for common-1.3.zip
   * can be specified as ":common:1.3::zip" - note missing groupId and classifier. 
   */
  public ArtifactBasicMetadata( String query )
  {
    if( query == null )
      return;
    
    String [] tokens = query.split(":");
    
    if( tokens == null || tokens.length < 1 )
      return;

    int count = tokens.length;
    
    this.groupId = nullify( tokens[0] );
  
    if( count > 1 )
      this.artifactId = nullify( tokens[1] );
    
    if( count > 2 )
      this.version = nullify( tokens[2] );
    
    if( count > 3 )
      this.classifier = nullify( tokens[3] );
    
    if( count > 4 )
      this.type = nullify( tokens[4] );
    
    if( this.type == null || this.type.length() < 1 )
      this.type = DEFAULT_ARTIFACT_TYPE;
  }
  //------------------------------------------------------------------
  /**
   * create basic out of <b>group:artifact:version:classifier:type</b> string, use 
   * empty string to specify missing component - for instance query for common-1.3.zip
   * can be specified as ":common:1.3::zip" - note missing groupId and classifier. 
   */
  public static ArtifactBasicMetadata create( String query )
  {
    ArtifactBasicMetadata mdq = new ArtifactBasicMetadata( query );
    
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
  public boolean sameGAV( ArtifactBasicMetadata md )
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
  public boolean sameGA( ArtifactBasicMetadata md )
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
  
  private static final String nvl( String val, String dflt )
  {
    return val == null ? dflt : val;
  }
  private static final String nvl( String val )
  {
    return nvl(val,"");
  }
  
  @Override
  public String toString()
  {
      return nvl(groupId) + ":" + nvl(artifactId) + ":" + nvl(version) + ":" + nvl(classifier) + ":" + nvl(type,DEFAULT_ARTIFACT_TYPE);
  }

  public String toDomainString()
  {
      return groupId + ":" + artifactId;
  }
  
  public String getBaseName()
  {
    return artifactId + "-" + version + (classifier == null ? "" :"-"+classifier);
  }
  
  public String getBaseName( String classifier )
  {
    return artifactId + "-" + version + ((classifier == null||classifier.length()<1) ? "" :"-"+classifier);
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
  public boolean hasVersion()
  {
    return version != null && version.length() > 0;
  }

  public void setVersion( String version )
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

  public String getScope()
  {
      return getArtifactScope().getScope();
  }

  public ArtifactScopeEnum getScopeAsEnum()
  {
      return artifactScope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : artifactScope;
  }
  
  public ArtifactScopeEnum getArtifactScope()
  {
      return artifactScope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : artifactScope;
  }

  public void setArtifactScope( ArtifactScopeEnum artifactScope )
  {
      this.artifactScope = artifactScope;
  }

  public void setScope( String scope )
  {
      this.artifactScope = scope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : ArtifactScopeEnum.valueOf( scope );
  }
  public boolean isOptional()
  {
    return optional;
  }
  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }
  public void setOptional(String optional)
  {
    this.optional = "true".equals(optional) ? true : false;
  }

  public Object getTracker()
  {
      return tracker;
  }

  public void setTracker( Object tracker )
  {
      this.tracker = tracker;
  }
  
  public boolean hasClassifier()
  {
    return classifier != null && classifier.length() > 0;
  }

  public Boolean isLocal()
  {
    return local;
  }

  public void setLocal( Boolean local )
  {
    this.local = local;
  }
  
  public ArtifactCoordinates getEffectiveCoordinates()
  {
    if( relocations == null || relocations.isEmpty() )
      return new ArtifactCoordinates( groupId, artifactId, version );
    
    return relocations.get( relocations.size() - 1 );
  }
  
  public ArtifactBasicMetadata addRelocation( ArtifactCoordinates coord )
  {
    if( coord == null )
      return this;

    if( relocations == null )
      relocations = new ArrayList<ArtifactCoordinates>(2);
    
    if( coord.getGroupId() == null )
      coord.setGroupId( groupId );
    
    if( coord.getArtifactId() == null )
      coord.setArtifactId( artifactId );
    
    if( coord.getVersion() == null )
      coord.setVersion( version );
    
    relocations.add( coord );
    effectiveCoordinates = coord;
    
    return this;
  }
  
  public String getEffectiveGroupId()
  {
    return effectiveCoordinates == null ? groupId: effectiveCoordinates.getGroupId();
  }
  
  public String getEffectiveArtifactId()
  {
    return effectiveCoordinates == null ? artifactId: effectiveCoordinates.getArtifactId();
  }
  
  public String getEffectiveersion()
  {
    return effectiveCoordinates == null ? version: effectiveCoordinates.getVersion();
  }
  
  public boolean hasInclusions()
  {
    return inclusions == null ? false : ! inclusions.isEmpty();
  }
  public Collection<ArtifactBasicMetadata> getInclusions()
  {
    return inclusions;
  }
  public void setInclusions( Collection<ArtifactBasicMetadata> inclusions )
  {
    this.inclusions = inclusions;
  }
  
  public boolean hasExclusions()
  {
    return exclusions == null ? false : ! exclusions.isEmpty();
  }
  public Collection<ArtifactBasicMetadata> getExclusions()
  {
    return exclusions;
  }
  public void setExclusions( Collection<ArtifactBasicMetadata> exclusions )
  {
    this.exclusions = exclusions;
  }
  
  /**
   * run dependency through inclusion/exclusion filters. Inclusion filter 
   * is always a "hole"-filter, which is then enhanced by exclusion "cork"-filter
   * 
   * @param dep dependency to vet
   * @return vet result
   * @throws VersionException 
   */
  public boolean allowDependency( ArtifactBasicMetadata dep )
  throws VersionException
  {
    boolean includeDependency = true;
    if( hasInclusions() )
      includeDependency = ! passesFilter( inclusions, dep );
    
    if( !includeDependency )
      return false;
    
    if( !hasExclusions() )
      return true;
    
    if( passesFilter( exclusions, dep ) )
      return true;
    
    return false;
    
  }
  
  private boolean passesFilter( Collection<ArtifactBasicMetadata> filter, ArtifactBasicMetadata dep )
  throws VersionException
  {
    for( ArtifactBasicMetadata filterMd : filter )
    {
      if( filterMd.sameGA( dep ) )
      {
        if( !filterMd.hasVersion() )
          return false; // no version in the filter - catch by GA
        VersionRange vr = new VersionRange( filterMd.getVersion() );
        if( vr.includes( dep.getVersion() ))
          return false; // catch by version query
      }
    }

    return true;
  }
  
  
  @Override
  public boolean equals( Object obj )
  {
    if( obj == null || !( obj instanceof ArtifactBasicMetadata ) )
      return false;
    
    String me = toString();
    String him = obj.toString();
    
    return toString().equals( obj.toString() );
  }
  
  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }
  
  
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
}
