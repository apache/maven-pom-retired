package org.apache.maven.mercury.metadata;

import java.util.Collection;

import org.apache.maven.mercury.repository.Repository;

/**
 * 
 * @author Jason van Zyl
 *  
 */
public class MetadataResolution
{
    /** resolved MD  */
    private ArtifactMetadata artifactMetadata;

    /** repositories, added by this POM  */
    private Collection<Repository> metadataRepositories;
    //-------------------------------------------------------------------
    public MetadataResolution( ArtifactMetadata artifactMetadata )
    {
        this.artifactMetadata = artifactMetadata;
    }
    //-------------------------------------------------------------------
    public MetadataResolution( ArtifactMetadata artifactMetadata,
    		Collection<Repository> metadataRepositories )
    {
    	this( artifactMetadata );
        this.metadataRepositories = metadataRepositories;
    }
    //-------------------------------------------------------------------
	public Collection<Repository> getMetadataRepositories()
	{
		return metadataRepositories;
	}

	public void setMetadataRepositories(
			Collection<Repository> metadataRepositories)
	{
		this.metadataRepositories = metadataRepositories;
	}
    //-------------------------------------------------------------------
	public ArtifactMetadata getArtifactMetadata()
	{
		return artifactMetadata;
	}

	public void setArtifactMetadata(ArtifactMetadata artifactMetadata)
	{
		this.artifactMetadata = artifactMetadata;
	}
    //-------------------------------------------------------------------
    //-------------------------------------------------------------------
}
