package org.apache.maven.mercury.repository;

import org.apache.maven.mercury.ArtifactBasicMetadata;

public interface MetadataReader
{
  public byte [] readMetadata( ArtifactBasicMetadata md )
  throws MetadataProcessingException;
}
