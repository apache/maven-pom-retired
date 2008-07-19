package org.apache.maven.mercury.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;

public class HttpMetadataSource
implements MetadataSource
{
  LocalRepository _localRepository;
  Set<RemoteRepository> _remoteRepositories;

  public Collection<ArtifactMetadata> expand(
                            ArtifactMetadata metadataQuery
                          , LocalRepository localRepository
                          , List<RemoteRepository> remoteRepositories
                                            )
  throws MetadataRetrievalException
  {
    if( localRepository == null )
      throw new MetadataRetrievalException("null localRepo specified");
    
    return null;
  }

  public MetadataResolution retrieve(
                            ArtifactMetadata metadata
                          , LocalRepository localRepository
                          , List<RemoteRepository> remoteRepositories
                                    )
  throws MetadataRetrievalException
  {
    // TODO Auto-generated method stub
    return null;
  }

}
