package org.apache.maven.mercury.metadata;

import java.util.Comparator;

public class ClassicDepthComparatorTest
extends AbstractSimpleTreeTest
{
  
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();
  }

  public void testNearestBest()
  {
    Comparator<MetadataTreeNode> comparator = new ClassicDepthComparator();
    
    int res = comparator.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be closer'n cc1bb1",  res > 0 );
    
    res = comparator.compare( bb1, cc1 );
    assertTrue( "bb1 should be the same as cc1",  res == 0 );
    
    res = comparator.compare( cc1, aa1 );
    assertTrue( "cc1 should be deeper'n aa11",  res < 0 );
  }

  public void testFarestBest()
  {
    Comparator<MetadataTreeNode> comparator = new ClassicDepthComparator(false);
    
    int res = comparator.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be closer'n cc1bb1",  res < 0 );
    
    res = comparator.compare( bb1, cc1 );
    assertTrue( "bb1 should be the same as cc1",  res == 0 );
    
    res = comparator.compare( cc1, aa1 );
    assertTrue( "cc1 should be deeper'n aa11",  res > 0 );
  }
}
