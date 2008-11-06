package org.apache.maven.mercury.metadata;

import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface DependencyBuilder
{
  //------------------------------------------------------------------------
  /**
   * build the tree, using the repositories specified in the
   * constructor
   * 
   * @param startMD - root of the tree to build
   * @param targetPlatform - limitations to use when retrieving metadata. Format is G:A=V, where V is Version Range
   * @return the root of the tree built
   * @throws MetadataTreeException
   */
  public abstract MetadataTreeNode buildTree( ArtifactBasicMetadata startMD, ArtifactScopeEnum scope )
  throws MetadataTreeException;

  //-----------------------------------------------------
  public abstract List<ArtifactMetadata> resolveConflicts( MetadataTreeNode root )
  throws MetadataTreeException;
  //-----------------------------------------------------
  //-----------------------------------------------------

}
