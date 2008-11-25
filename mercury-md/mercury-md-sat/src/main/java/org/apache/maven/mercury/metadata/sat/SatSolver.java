package org.apache.maven.mercury.metadata.sat;

import java.util.Comparator;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.event.EventGenerator;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public interface SatSolver
extends EventGenerator
{
  public static final int DEFAULT_TREE_SIZE = 128; //nodes
  
  public static final String EVENT_SOLVE = "solve";
  public static final String EVENT_CREATE_SOLVER = "create.sat.solver";
  
  /**
   * 
   * @param sorts - policies expressed as sorted list of node sorters - from most important to the least
   * @throws SatException
   */
  public void applyPolicies( List< Comparator<MetadataTreeNode> > comparators )
  throws SatException;
  
  /**
   * 
   * @return list of ArtifactMetedata's in the solution
   * @throws SatException
   */
  public List<ArtifactMetadata> solve()
  throws SatException;
}
