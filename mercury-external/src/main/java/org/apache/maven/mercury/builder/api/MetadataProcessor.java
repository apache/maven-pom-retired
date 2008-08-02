package org.apache.maven.mercury.builder.api;

import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

public interface MetadataProcessor
{
    List<ArtifactBasicMetadata> getDependencies( ArtifactBasicMetadata bmd, MetadataReader mdReader )
    throws MetadataProcessingException;
}
