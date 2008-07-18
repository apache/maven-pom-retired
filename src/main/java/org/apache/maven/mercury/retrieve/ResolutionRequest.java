package org.apache.maven.mercury.retrieve;

import java.util.Set;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.metadata.ArtifactMetadata;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;

/**
 * A resolution request allows you to either use an existing MavenProject, or a coordinate (gid:aid:version)
 * to process a POMs dependencies.
 *
 * @author Jason van Zyl
 */
public class ResolutionRequest
{
    private ArtifactMetadata _md;
    private LocalRepository localRepository;
    private Set<RemoteRepository> remoteRepostories;

    public ArtifactMetadata getMd()
    {
        return _md;
    }

    public ResolutionRequest setMd( ArtifactMetadata md )
    {
        this._md = md;
        return this;
    }

    public boolean hasMd()
    {
        return _md != null;
    }
    
    private void checkMd()
    {
      if( _md == null )
        _md = new ArtifactMetadata();
    }

    public String getGroupId()
    {
      checkMd();
      return _md.getGroupId();
    }

    public ResolutionRequest setGroupId( String groupId )
    {
      checkMd();
      this._md.setGroupId( groupId );

      return this;
    }

    public String getArtifactId()
    {
      checkMd();
      return _md.getArtifactId();
    }

    public ResolutionRequest setArtifactId( String artifactId )
    {
      checkMd();
      this._md.setArtifactId( artifactId );

      return this;
    }

    public String getVersion()
    {
      checkMd();
      return _md.getVersion();
    }

    public ResolutionRequest setVersion( String version )
    {
      checkMd();
      this._md.setVersion(version);
      return this;
    }

    public LocalRepository getLocalRepository()
    {
        return localRepository;
    }

    public ResolutionRequest setLocalRepository( LocalRepository localRepository )
    {
        this.localRepository = localRepository;

        return this;
    }

    public Set<RemoteRepository> getRemoteRepostories()
    {
        return remoteRepostories;
    }

    public ResolutionRequest setRemoteRepostories( Set<RemoteRepository> remoteRepostories )
    {
        this.remoteRepostories = remoteRepostories;

        return this;
    }

    // ------------------------------------------------------------------------
    //
    // ------------------------------------------------------------------------

    public String toString()
    {
        StringBuffer sb = new StringBuffer()
            .append( "groupId = " + getGroupId() )
            .append( "artifactId = " + getArtifactId() )
            .append( "version = " + getVersion() );

        return sb.toString();
    }
}
