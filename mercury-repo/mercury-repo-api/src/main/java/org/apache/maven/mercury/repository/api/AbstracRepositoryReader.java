package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.builder.api.MetadataReader;


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
  protected MetadataProcessor _mdProcessor;
  
  public void setMetadataProcessor( MetadataProcessor mdProcessor )
  {
    _mdProcessor = mdProcessor;
  }
  
  public MetadataProcessor getMetadataProcessor()
  {
    return _mdProcessor;
  }
  
  public byte[] readMetadata( ArtifactBasicMetadata bmd  )
  throws MetadataReaderException
  {
    return readRawData( bmd, "", "pom" );
  }
  
}
