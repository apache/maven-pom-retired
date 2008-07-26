package org.apache.maven.mercury;

import java.util.Collection;
import java.util.List;


/**
 * Artifact Metadata that is resolved independent of Artifact itself. It's
 * built on top of ArtifactBasicMetadata
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class ArtifactMetadata
extends ArtifactBasicMetadata
{
    // in addition to basic coordinates

    private boolean release;

    /** 
     * explanation: why this MD was chosen over it's siblings
     * in the resulting structure (classpath for now) 
     */
    protected String why;

    /** dependencies of the artifact behind this metadata */
    protected List<ArtifactBasicMetadata> dependencies;

    /** is metadata found anywhere */
    protected boolean resolved = false;

    /** does the actual artifact for this metadata exists */
    protected boolean artifactExists = false;

    /** artifact URI */
    protected String artifactUri;

    /** error message  */
    private String error;

    /**
     * for testing - required for mock MetadataSource
     */
    public ArtifactMetadata()
    {
    }
    //------------------------------------------------------------------
    /**
     * group:artifact:version:classifier:packaging
     */
    public ArtifactMetadata( String name )
    {
      if( name == null )
        return;
      
      String [] tokens = name.split(":");
      
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
    }
    private static final String nullify( String s )
    {
      if( s == null || s.length() < 1 )
        return null;
      return s;
    }
    public ArtifactMetadata( String groupId, String name, String version )
    {
        this( groupId, name, version, null );
    }

    public ArtifactMetadata( String groupId, String name, String version, String type )
    {
        this( groupId, name, version, type, null );
    }

    public ArtifactMetadata( String groupId, String name, String version, String type, ArtifactScopeEnum artifactScope )
    {
        this( groupId, name, version, type, artifactScope, null );
    }

    public ArtifactMetadata( String groupId, String name, String version, String type, ArtifactScopeEnum artifactScope, String classifier )
    {
        this( groupId, name, version, type, artifactScope, classifier, null );
    }

    public ArtifactMetadata( String groupId, String name, String version, String type, ArtifactScopeEnum artifactScope, String classifier, String artifactUri )
    {
        this( groupId, name, version, type, artifactScope, classifier, artifactUri, null, true, null );
    }

    public ArtifactMetadata( String groupId, String name, String version, String type, ArtifactScopeEnum artifactScope, String classifier, String artifactUri, String why, boolean resolved,
                             String error )
    {
        this.groupId = groupId;
        this.artifactId = name;
        this.version = version;
        this.type = type;
        this.artifactScope = artifactScope;
        this.classifier = classifier;
        this.artifactUri = artifactUri;
        this.why = why;
        this.resolved = resolved;
        this.error = error;
    }

    public ArtifactMetadata( ArtifactBasicMetadata bmd )
    {
      this( bmd.getGroupId(), bmd.getArtifactId(), bmd.getVersion(), bmd.getType(), null, bmd.getClassifier() );
    }

    public ArtifactMetadata( String groupId, String name, String version, String type, String scopeString, String classifier, String artifactUri, String why, boolean resolved, String error )
    {
        this( groupId, name, version, type, scopeString == null ? ArtifactScopeEnum.DEFAULT_SCOPE : ArtifactScopeEnum.valueOf( scopeString ), classifier, artifactUri, why, resolved, error );
    }

    public ArtifactMetadata( Artifact af )
    {
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public void setResolved( boolean resolved )
    {
        this.resolved = resolved;
    }

    public boolean isArtifactExists()
    {
        return artifactExists;
    }

    public void setArtifactExists( boolean artifactExists )
    {
        this.artifactExists = artifactExists;
    }

    public List<ArtifactBasicMetadata> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies( List<ArtifactBasicMetadata> dependencies )
    {
        this.dependencies = dependencies;
    }

    public String getArtifactUri()
    {
        return artifactUri;
    }

    public void setArtifactUri( String artifactUri )
    {
        this.artifactUri = artifactUri;
    }

    public String getWhy()
    {
        return why;
    }

    public void setWhy( String why )
    {
        this.why = why;
    }

    public String getError()
    {
        return error;
    }

    public void setError( String error )
    {
        this.error = error;
    }

    public boolean isError()
    {
        return error == null;
    }

    public void setRelease( boolean release )
    {
        this.release = release;
    }

    public boolean isRelease()
    {
        return release;
    }
    
    
}
