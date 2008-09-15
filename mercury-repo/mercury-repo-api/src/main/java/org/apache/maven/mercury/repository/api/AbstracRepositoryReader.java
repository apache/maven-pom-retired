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
  
  public void setMetadataProcessor( DependencyProcessor mdProcessor )
  {
    _mdProcessor = mdProcessor;
  }
  
  public DependencyProcessor getMetadataProcessor()
  {
    return _mdProcessor;
  }
  
  public byte[] readMetadata( ArtifactBasicMetadata bmd  )
  throws MetadataReaderException
  {
    return readRawData( bmd, "", "pom" );
  }
  
}
