package org.apache.maven.mercury.metadata.builder;

import java.util.List;

import org.apache.maven.mercury.ArtifactMetadata;

public interface MetadataBuilder
{

  public List<ArtifactMetadata> find( ArtifactMetadata query )
  throws MetadataBuilderException;

}
