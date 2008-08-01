package org.apache.maven.mercury.metadata;

import java.util.Comparator;

/**
 * classical depth comparator: shallower is better by default, but that could 
 * be changed by appropriate constructor
 * 
 * @author Oleg Gusakov
 * @version $Id: ClassicDepthComparator.java 676808 2008-07-15 06:32:05Z ogusakov $
 */
public class ClassicDepthComparator
implements Comparator<MetadataTreeNode>
{
  boolean _closerBetter = true;
  
  public ClassicDepthComparator()
  {
  }

  public ClassicDepthComparator( boolean closerBetter )
  {
    _closerBetter = closerBetter;
  }
  
  public int compare( MetadataTreeNode n1, MetadataTreeNode n2 )
  {
    return _closerBetter ? n2.getDepth() - n1.getDepth() : n1.getDepth() - n2.getDepth() ;
  }
}
