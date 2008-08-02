package org.apache.maven.mercury.repository.local.m2;

import java.io.File;

import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;

public class LocalRepositoryM2
extends AbstractRepository
implements LocalRepository
{
    private File directory;
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( String id, File directory )
    {
        super( id, DEFAULT_REPOSITORY_TYPE );
        this.directory = directory;
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( String id, File directory, String type )
    {
        super( id, type );
        this.directory = directory;
    }
    //----------------------------------------------------------------------------------
    public File getDirectory()
    {
        return directory;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( MetadataProcessor processor ) 
    {
      if( reader == null )
        reader = new LocalRepositoryReaderM2( this, processor );

      return reader;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( MetadataProcessor processor, String protocol )
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
      return null;
    }
    //----------------------------------------------------------------------------------
    public boolean isLocal()
    {
      return true;
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
