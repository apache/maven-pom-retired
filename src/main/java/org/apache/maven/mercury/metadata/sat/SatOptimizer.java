package org.apache.maven.mercury.metadata.sat;

import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.sat4j.pb.ObjectiveFunction;

/**
 * Sat solver optimizer - calculates and returns the minimization function 
 * 
 * @author Oleg Gusakov
 *
 */
public interface SatOptimizer
{
  /**
   * returns the function to be minimized
   * 
   * @param tn
   * @param context
   * @return
   * @throws SatException
   */
  public ObjectiveFunction getOptimizer( MetadataTreeNode tn, SatContext context )
  throws SatException
  ;
}
