package org.apache.maven.mercury.metadata.sat;

import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public interface SatSolver
{
  public static final int DEFAULT_TREE_SIZE = 128; //nodes
  
  public List<ArtifactMetadata> solve()
  throws SatException;
}
