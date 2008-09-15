package org.apache.maven.mercury.repository.local.m2;

import java.io.File;

import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;

public class LocalRepositoryM2
extends AbstractRepository
implements LocalRepository
{
    private File directory;
    
    private static final String METADATA_NAME = "maven-metadata-local.xml";

    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( Server server )
    {
        super( server.getId(), DEFAULT_REPOSITORY_TYPE );
        this.directory = new File( server.getURL().getFile() );
        this.server = server;
        this.metadataName = METADATA_NAME;
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( String id, File directory )
    {
        super( id, DEFAULT_REPOSITORY_TYPE );
        this.directory = directory;
        this.metadataName = METADATA_NAME;
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( String id, File directory, String type )
    {
        super( id, type );
        this.directory = directory;
        this.metadataName = METADATA_NAME;
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
        reader = new LocalRepositoryReaderM2( this, processor );

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
        writer = new LocalRepositoryWriterM2(this);
      
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
