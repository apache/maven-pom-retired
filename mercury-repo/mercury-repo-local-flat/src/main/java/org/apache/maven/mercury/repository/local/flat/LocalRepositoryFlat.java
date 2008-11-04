package org.apache.maven.mercury.repository.local.flat;

import java.io.File;

import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.NonExistentProtocolException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;

public class LocalRepositoryFlat
extends AbstractRepository
implements LocalRepository
{
  public static final String FLAT_REPOSITORY_TYPE = "flat";
  
    private File directory;
    
    private boolean createPoms         = false;
    private boolean createGroupFolders = false;

    //----------------------------------------------------------------------------------
    public LocalRepositoryFlat( String id, File directory, boolean createGroupFolders, boolean createPoms )
    {
        super( id, FLAT_REPOSITORY_TYPE );
        this.directory = directory;
        this.createGroupFolders = createGroupFolders;
        this.createPoms = createPoms;
    }
    //----------------------------------------------------------------------------------
    public File getDirectory()
    {
        return directory;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader() 
    {
      return RepositoryReader.NULL_READER;
    }
    //----------------------------------------------------------------------------------
    public RepositoryReader getReader( String protocol )
    {
       return RepositoryReader.NULL_READER;
    }
    //----------------------------------------------------------------------------------
    public RepositoryWriter getWriter()
    {
      return new LocalRepositoryWriterFlat(this);
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
      return false;
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
    public boolean isCreatePoms()
    {
      return createPoms;
    }
    public void setCreatePoms( boolean createPoms )
    {
      this.createPoms = createPoms;
    }
    public boolean isCreateGroupFolders()
    {
      return createGroupFolders;
    }
    public void setCreateGroupFolders( boolean createGroupFolders )
    {
      this.createGroupFolders = createGroupFolders;
    }
    //----------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------
}
