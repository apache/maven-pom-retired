package org.apache.maven.mercury.metadata;

public class ClassicDepthSorterTest
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
    MetadataTreeArtifactSorter sorter = new ClassicVersionSorter();
    
    int res = sorter.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be closer'n cc1bb1",  res > 0 );
    
    res = sorter.compare( bb1, cc1 );
    assertTrue( "bb1 should be the same as cc1",  res == 0 );
    
    res = sorter.compare( cc1, aa1 );
    assertTrue( "cc1 should be deeper'n aa11",  res < 0 );
  }

  public void testFarestBest()
  {
    MetadataTreeArtifactSorter sorter = new ClassicDepthSorter(false);
    
    int res = sorter.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be closer'n cc1bb1",  res < 0 );
    
    res = sorter.compare( bb1, cc1 );
    assertTrue( "bb1 should be the same as cc1",  res == 0 );
    
    res = sorter.compare( cc1, aa1 );
    assertTrue( "cc1 should be deeper'n aa11",  res > 0 );
  }
}
