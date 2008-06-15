package org.apache.maven.mercury.metadata;

/**
 * This is a chain of filters that can stop an ArtifactMetadata 
 * from reaching the resolution tree
 * 
 * @author Oleg Gusakov
 */
public interface MetadataTreeArtifactKiller
{
  public boolean vet( ArtifactMetadata md )
  ;
}
