package org.apache.maven.mercury.metadata;


public class ClassicVersionSorterTest
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
    MetadataTreeArtifactSorter sorter = new ClassicVersionSorter();
    
    int res = sorter.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be the same cc1bb1",  res == 0 );
    
    res = sorter.compare( bb1, cc1bb2 );
    assertTrue( "bb1 should be older'n cc1bb2",  res < 0 );
  }

  public void testOldestBest()
  {
    MetadataTreeArtifactSorter sorter = new ClassicVersionSorter(false);
    
    int res = sorter.compare( bb1, cc1bb1 );
    
    assertTrue( "bb1 should be the same cc1bb1",  res == 0 );
    
    res = sorter.compare( bb1, cc1bb2 );
    assertTrue( "bb1 should be older'n cc1bb2",  res > 0 );
  }
}
