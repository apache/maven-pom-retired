package org.apache.maven.mercury.metadata;

import java.util.Collection;

import org.apache.maven.mercury.repository.RemoteRepository;

/*
 * 
 * @author Jason van Zyl
 */
public class MetadataResolution
{
    /** resolved MD  */
    private ArtifactMetadata artifactMetadata;

    /** repositories, added by this POM  */
    private Collection<RemoteRepository> metadataRepositories;

    public MetadataResolution( ArtifactMetadata artifactMetadata )
    {
        this.artifactMetadata = artifactMetadata;
    }

    public MetadataResolution( ArtifactMetadata artifactMetadata, Collection<RemoteRepository> metadataRepositories )
    {
        this( artifactMetadata );
        this.metadataRepositories = metadataRepositories;
    }

    public Collection<RemoteRepository> getMetadataRepositories()
    {
        return metadataRepositories;
    }

    public void setMetadataRepositories( Collection<RemoteRepository> metadataRepositories )
    {
        this.metadataRepositories = metadataRepositories;
    }

    public ArtifactMetadata getArtifactMetadata()
    {
        return artifactMetadata;
    }

    public void setArtifactMetadata( ArtifactMetadata artifactMetadata )
    {
        this.artifactMetadata = artifactMetadata;
    }
}
