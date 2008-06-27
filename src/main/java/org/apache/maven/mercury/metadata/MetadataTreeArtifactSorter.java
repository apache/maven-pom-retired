package org.apache.maven.mercury.metadata;

/**
 * These objects form a chain of sorters for the second 
 * phase of the resolution process - conflict resolution
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public interface MetadataTreeArtifactSorter
{
  public int compare( MetadataTreeNode n1,  MetadataTreeNode n2  )
  ;
}
