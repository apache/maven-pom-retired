package org.apache.maven.mercury.metadata;

/**
 * classical depth comparator: shallower is better by default, but that could 
 * be changed by appropriate constructor
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class ClassicDepthSorter
implements MetadataTreeArtifactSorter
{
  boolean _closerBetter = true;
  
  public ClassicDepthSorter()
  {
  }

  public ClassicDepthSorter( boolean closerBetter )
  {
    _closerBetter = closerBetter;
  }
  
  public int compare( MetadataTreeNode n1, MetadataTreeNode n2 )
  {
    return _closerBetter ? n2.getDepth() - n1.getDepth() : n1.getDepth() - n2.getDepth() ;
  }
}
