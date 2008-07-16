package org.apache.maven.mercury.metadata.sat;

import java.util.Comparator;

import org.apache.maven.mercury.metadata.MetadataTreeNode;


/**
 * utility class for DefaultSatSolver. Assumes good data - no null's
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
class MetadataTreeNodeGAVComparator
implements Comparator<MetadataTreeNode>
{

  public int compare(MetadataTreeNode n1, MetadataTreeNode n2)
  {
    return n1.getMd().getGAV().compareTo( n2.getMd().getGAV() );
  }

}
