package org.apache.maven.mercury.repository;

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
