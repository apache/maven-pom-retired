package org.apache.maven.mercury.repository;

import java.io.File;

import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;

public class DefaultLocalRepository
extends AbstractRepository
implements LocalRepository
{
    private File directory;
    
    public DefaultLocalRepository( String id, File directory )
    {
        super( id );
        this.directory = directory;
    }

    public File getDirectory()
    {
        return directory;
    }

    public RepositoryReader getReader()
    {
      // TODO Auto-generated method stub
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
      return null;
    }

    public boolean isLocal()
    {
      return true;
    }
}
