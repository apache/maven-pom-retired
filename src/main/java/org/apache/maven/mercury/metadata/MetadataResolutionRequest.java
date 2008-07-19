package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;


/** @author Oleg Gusakov */
public class MetadataResolutionRequest
{
    protected ArtifactMetadata query;
    protected LocalRepository localRepository;
    protected List<RemoteRepository> remoteRepositories;

    public MetadataResolutionRequest()
    {
    }

    public MetadataResolutionRequest( ArtifactMetadata query,
                                      LocalRepository localRepository,
                                      List<RemoteRepository> remoteRepositories )
    {
        this.query = query;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }

    public ArtifactMetadata getQuery()
    {
        return query;
    }

    public MetadataResolutionRequest setQuery( ArtifactMetadata query )
    {
        this.query = query;        
        return this;
    }

    public LocalRepository getLocalRepository()
    {
        return localRepository;
    }

    public MetadataResolutionRequest addRemoteRepository( RemoteRepository repository )
    {
        if ( remoteRepositories == null )
        {
            remoteRepositories = new ArrayList<RemoteRepository>();
        }
        
        remoteRepositories.add( repository );
        
        return this;
    }
    
    public MetadataResolutionRequest setLocalRepository( LocalRepository localRepository )
    {
        this.localRepository = localRepository;
        return this;
    }

    public List<RemoteRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }

    public MetadataResolutionRequest setRemoteRepositories( List<RemoteRepository> remoteRepositories )
    {
        this.remoteRepositories = remoteRepositories;
        return this;
    }
}
