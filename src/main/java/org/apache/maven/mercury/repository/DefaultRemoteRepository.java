package org.apache.maven.mercury.repository;

import org.apache.maven.mercury.repository.layout.RepositoryLayout;

public class DefaultRemoteRepository
extends AbstractRepository
implements RemoteRepository
{
    private String url;
    
    public DefaultRemoteRepository( String id, RepositoryLayout layout, String url )
    {
        super( id, layout );
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }
}
