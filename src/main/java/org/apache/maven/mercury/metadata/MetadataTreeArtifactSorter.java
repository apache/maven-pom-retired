package org.apache.maven.mercury.metadata;

/**
 * These objects form a chain of sorters for the second 
 * phase of the resolution process - conflict resolution
 * 
 * @author Oleg Gusakov
 */
public interface MetadataTreeArtifactSorter
{
  public int compare( ArtifactMetadata md1,  ArtifactMetadata md2  )
  ;
}
