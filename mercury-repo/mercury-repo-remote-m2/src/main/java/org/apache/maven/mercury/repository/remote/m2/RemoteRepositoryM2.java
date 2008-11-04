package org.apache.maven.mercury.repository.remote.m2;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryUpdateIntervalPolicy;
import org.apache.maven.mercury.repository.api.RepositoryUpdatePolicy;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;


public class RemoteRepositoryM2
extends AbstractRepository
implements RemoteRepository
{
    private Server _server;
    
    /** default update policy */
    private RepositoryUpdatePolicy _updatePolicy = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.DEFAULT_UPDATE_POLICY );
    
    //----------------------------------------------------------------------------------
    public RemoteRepositoryM2( Server server  )
    {
      this( server.getId(), server );
    }
    //----------------------------------------------------------------------------------
    public RemoteRepositoryM2( String id, Server server  )
    {
        super( id, DEFAULT_REPOSITORY_TYPE );
        this._server = server;
        setDependencyProcessor( new MavenDependencyProcessor() );
    }
    //----------------------------------------------------------------------------------
    public Server getServer()
    {
        return _server;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader()
    throws RepositoryException
    {
      return new RemoteRepositoryReaderM2( this, getDependencyProcessor() );
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( String protocol )
    throws RepositoryException
    {
      return getReader();
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter()
    throws RepositoryException
    {
      return new RemoteRepositoryWriterM2(this);
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
    public boolean isReadable()
    {
      return true;
    }
    //----------------------------------------------------------------------------------
    public boolean isWriteable()
    {
      return true;
    }
    //----------------------------------------------------------------------------------
    public String getType()
    {
      return DEFAULT_REPOSITORY_TYPE;
    }
    //----------------------------------------------------------------------------------
    public RepositoryUpdatePolicy getUpdatePolicy()
    {
      return _updatePolicy;
    }
    //----------------------------------------------------------------------------------
    public void setUpdatePolicy( RepositoryUpdatePolicy updatePolicy )
    {
      this._updatePolicy = updatePolicy;
    }
    //----------------------------------------------------------------------------------
}
