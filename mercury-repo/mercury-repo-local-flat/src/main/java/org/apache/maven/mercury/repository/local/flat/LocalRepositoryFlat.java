package org.apache.maven.mercury.repository.local.flat;

import java.io.File;

import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;

public class LocalRepositoryFlat
extends AbstractRepository
implements LocalRepository
{
  public static final String FLAT_REPOSITORY_TYPE = "flat";
  
    private File directory;
    
    private static final String METADATA_NAME = "maven-metadata-local.xml";

    //----------------------------------------------------------------------------------
    public LocalRepositoryFlat( String id, File directory )
    {
        super( id, FLAT_REPOSITORY_TYPE );
        this.directory = directory;
    }
    //----------------------------------------------------------------------------------
    public File getDirectory()
    {
        return directory;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( DependencyProcessor processor ) 
    {
      if( reader == null )
        reader = new LocalRepositoryReaderFlat( this, processor );

      return reader;
    }
    //----------------------------------------------------------------------------------
    // TODO oleg: what happens in multi-threaded execution?? 
    public RepositoryReader getReader( DependencyProcessor processor, String protocol )
    {
       return getReader(processor);
    }
    //----------------------------------------------------------------------------------
    // TODO oleg: what happens in multi-threaded execution?? 
    public RepositoryWriter getWriter()
    {
      if( writer == null )
        writer = new LocalRepositoryWriterFlat(this);
      
      return writer;
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter( String protocol )
        throws NonExistentProtocolException
    {
      return getWriter();
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
