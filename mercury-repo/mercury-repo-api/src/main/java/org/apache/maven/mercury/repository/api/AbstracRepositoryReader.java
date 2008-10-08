package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;
import org.apache.maven.mercury.builder.api.MetadataReaderException;


/**
 * This is to keep MetadataProcessor for all readers
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstracRepositoryReader
implements RepositoryReader, MetadataReader
{
  protected DependencyProcessor _mdProcessor;
  
  protected RepositoryMetadataCache _mdCache;
  
  public void setDependencyProcessor( DependencyProcessor mdProcessor )
  {
    _mdProcessor = mdProcessor;
  }
  
  public DependencyProcessor getDependencyProcessor()
  {
    return _mdProcessor;
  }
  
  public void setMetadataCache( RepositoryMetadataCache mdCache )
  {
    this._mdCache = mdCache;
  }
  
  public RepositoryMetadataCache getMetadataCache()
  {
    return _mdCache;
  }
  
  public boolean hasMetadataCache()
  {
    return _mdCache != null;
  }
  
  public byte[] readMetadata( ArtifactBasicMetadata bmd  )
  throws MetadataReaderException
  {
    return readRawData( bmd, "", "pom" );
  }
  
}
