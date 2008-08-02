package org.apache.maven.mercury.repository.remote.m2;

import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;


public class RemoteRepositoryM2
extends AbstractRepository
implements RemoteRepository
{
    private Server _server;
    //----------------------------------------------------------------------------------
    public RemoteRepositoryM2( String id, Server server  )
    {
        super( id, DEFAULT_REPOSITORY_TYPE );
        this._server = server;
    }
    //----------------------------------------------------------------------------------
    public Server getServer()
    {
        return _server;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( MetadataProcessor processor )
    throws RepositoryException
    {
      return new RemoteRepositoryReaderM2( this, processor );
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( MetadataProcessor processor, String protocol )
    throws RepositoryException
    {
      return getReader(processor);
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter()
    {
      // TODO Auto-generated method stub
      return null;
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter( String protocol )
        throws NonExistentProtocolException
    {
      // TODO Auto-generated method stub
      return null;
    }
    //----------------------------------------------------------------------------------
    public boolean isLocal()
    {
     return false;
    }
    //----------------------------------------------------------------------------------
    public boolean isReadOnly()
    {
      return false;
    }
    //----------------------------------------------------------------------------------
    public String getType()
    {
      return DEFAULT_REPOSITORY_TYPE;
    }
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
}
