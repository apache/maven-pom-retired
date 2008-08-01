package org.apache.maven.mercury.metadata;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

/**
 * This is a member of a chain of filters that can stop an ArtifactMetadata 
 * from reaching the resolution tree
 * 
 * @author Oleg Gusakov
 */
public interface MetadataTreeArtifactFilter
{
  public boolean veto( ArtifactBasicMetadata md )
  ;
}
