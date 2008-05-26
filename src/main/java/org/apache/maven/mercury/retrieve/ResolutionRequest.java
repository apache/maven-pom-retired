package org.apache.maven.mercury.retrieve;

import java.util.Set;

import org.apache.maven.mercury.Artifact;
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
    private Artifact artifact;

    private String groupId;

    private String artifactId;

    private String version;

    private LocalRepository localRepository;

    private Set<RemoteRepository> remoteRepostories;

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ResolutionRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;

        return this;
    }

    public boolean hasArtifact()
    {
        return artifact != null;
    }

    public String getGroupId()
    {
        if ( artifact != null )
        {
            return artifact.getGroupId();
        }

        return groupId;
    }

    public ResolutionRequest setGroupId( String groupId )
    {
        this.groupId = groupId;

        return this;
    }

    public String getArtifactId()
    {
        if ( artifact != null )
        {
            return artifact.getArtifactId();
        }

        return artifactId;
    }

    public ResolutionRequest setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;

        return this;
    }

    public String getVersion()
    {
        if ( artifact != null )
        {
            return artifact.getVersion();
        }

        return version;
    }

    public ResolutionRequest setVersion( String version )
    {
        this.version = version;

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
