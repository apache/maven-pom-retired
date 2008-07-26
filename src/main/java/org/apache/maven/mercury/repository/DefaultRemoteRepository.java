package org.apache.maven.mercury.repository;

import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;

public class DefaultRemoteRepository
extends AbstractRepository
implements RemoteRepository
{
    private String url;
    
    public DefaultRemoteRepository( String id, String url, MetadataProcessor processor  )
    {
        super( id, processor );
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public RepositoryReader getReader()
    {
      return null;
    }

    public RepositoryReader getReader( String protocol )
    {
      // TODO Auto-generated method stub
      return null;
    }

    public RepositoryWriter getWriter()
    {
      // TODO Auto-generated method stub
      return null;
    }

    public RepositoryWriter getWriter( String protocol )
        throws NonExistentProtocolException
    {
      // TODO Auto-generated method stub
      return null;
    }

    public boolean isLocal()
    {
     return false;
    }
}
