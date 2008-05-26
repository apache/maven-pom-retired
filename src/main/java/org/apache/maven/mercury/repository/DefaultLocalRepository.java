package org.apache.maven.mercury.repository;

import java.io.File;

import org.apache.maven.mercury.repository.layout.RepositoryLayout;

public class DefaultLocalRepository
    extends AbstractRepository
    implements LocalRepository
{
    private File directory;
    
    public DefaultLocalRepository( String id, RepositoryLayout layout, File directory )
    {
        super( id, layout );
        this.directory = directory;
    }

    public File getDirectory()
    {
        return directory;
    }
}
