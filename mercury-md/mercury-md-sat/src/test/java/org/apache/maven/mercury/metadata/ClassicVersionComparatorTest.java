package org.apache.maven.mercury.metadata;

import java.util.Comparator;


public class ClassicVersionComparatorTest
extends AbstractSimpleTreeTest
{
  
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();
  }

  public void testNewestBest()
  {
    Comparator<MetadataTreeNode> comparator = new ClassicVersionComparator();
    
    int res = comparator.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be the same cc1bb1",  res == 0 );
    
    res = comparator.compare( bb1, cc1bb2 );
    assertTrue( "bb1 should be older'n cc1bb2",  res < 0 );
  }

  public void testOldestBest()
  {
    Comparator<MetadataTreeNode> comparator = new ClassicVersionComparator(false);
    
    int res = comparator.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be the same cc1bb1",  res == 0 );
    
    res = comparator.compare( bb1, cc1bb2 );
    assertTrue( "bb1 should be older'n cc1bb2",  res > 0 );
  }
}
