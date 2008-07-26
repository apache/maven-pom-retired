package org.apache.maven.mercury.repository;

import java.util.List;

import org.apache.maven.mercury.ArtifactBasicMetadata;

public interface MetadataProcessor
{
    List<ArtifactBasicMetadata> getDependencies( ArtifactBasicMetadata bmd, MetadataReader mdReader )
    throws MetadataProcessingException;
}
