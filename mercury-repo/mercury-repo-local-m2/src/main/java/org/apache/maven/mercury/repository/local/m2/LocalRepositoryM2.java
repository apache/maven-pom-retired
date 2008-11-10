package org.apache.maven.mercury.repository.local.m2;

import java.io.File;

import org.apache.maven.mercury.MavenDependencyProcessor;
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
    
    public static final String METADATA_FILE_NAME = "maven-metadata-local.xml";

    //----------------------------------------------------------------------------------
    private void setDirectory( File directory )
    {
      if( directory == null )
        throw new IllegalArgumentException( "null.directory" );
      
      if( !directory.exists() )
        directory.mkdirs();

      this.directory = directory;
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( Server server )
    {
        super( server.getId(), DEFAULT_REPOSITORY_TYPE );
        setDirectory( new File( server.getURL().getFile() ) );
        this.server = server;
        
        setDependencyProcessor( new MavenDependencyProcessor() );
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( String id, File directory )
    {
        super( id, DEFAULT_REPOSITORY_TYPE );
        setDirectory( directory );
        
        setDependencyProcessor( new MavenDependencyProcessor() );
    }
    //----------------------------------------------------------------------------------
    public LocalRepositoryM2( String id, File directory, String type )
    {
        super( id, type );
        setDirectory( directory );
        
        setDependencyProcessor( new MavenDependencyProcessor() );
    }
    //----------------------------------------------------------------------------------
    public File getDirectory()
    {
        return directory;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader() 
    {
      return new LocalRepositoryReaderM2( this, getDependencyProcessor() );
    }
    //----------------------------------------------------------------------------------
    // TODO oleg: what happens in multi-threaded execution?? 
    public RepositoryReader getReader( String protocol )
    {
       return getReader();
    }
    //----------------------------------------------------------------------------------
    // TODO oleg: what happens in multi-threaded execution?? 
    public RepositoryWriter getWriter()
    {
      return new LocalRepositoryWriterM2(this);
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
    public String getMetadataName()
    {
      return METADATA_FILE_NAME;
    }
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
}
