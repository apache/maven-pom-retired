package org.apache.maven.mercury.metadata.sat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.metadata.ClassicDepthComparator;
import org.apache.maven.mercury.metadata.ClassicVersionComparator;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class DefaultSatSolverTest
    extends TestCase
{
  DefaultSatSolver ss;
  String title;
  
  MetadataTreeNode tree;
  
  ArtifactMetadata a1 = new ArtifactMetadata("t:a:1");
  ArtifactMetadata b1 = new ArtifactMetadata("t:b:1");
  ArtifactMetadata b2 = new ArtifactMetadata("t:b:2");
  ArtifactMetadata b3 = new ArtifactMetadata("t:b:3");
  ArtifactMetadata c1 = new ArtifactMetadata("t:c:1");
  ArtifactMetadata c2 = new ArtifactMetadata("t:c:2");

  ArtifactMetadata d1 = new ArtifactMetadata("t:d:1");
  
  //----------------------------------------------------------------------
  protected void setUp()
  throws Exception
  {
    super.setUp();
  }
  //----------------------------------------------------------------------
  public void testDedupe()
  {
    List<MetadataTreeNode> list = new ArrayList<MetadataTreeNode>();
    
    list.add(  new MetadataTreeNode( new ArtifactMetadata("a:a:1"), null, null ) );
    list.add(  new MetadataTreeNode( new ArtifactMetadata("a:a:1"), null, null ) );
    list.add(  new MetadataTreeNode( new ArtifactMetadata("a:a:1"), null, null ) );
    list.add(  new MetadataTreeNode( new ArtifactMetadata("b:b:1"), null, null ) );
    list.add(  new MetadataTreeNode( new ArtifactMetadata("b:b:1"), null, null ) );
    list.add(  new MetadataTreeNode( new ArtifactMetadata("a:a:1"), null, null ) );
    
    DefaultSatSolver.removeDuplicateGAVs( list );
    
    System.out.println(list);
    
    assertEquals( 2, list.size() );
  }
  
  
  private int f( int a, int b )
  {
    return -a + b;
  }
  //----------------------------------------------------------------------
  //       d:d:1 - c:c:[2,4)
  //      / 
  // a:a:1
  //      \     
  //       b:b:2 - c:c:1
  //----------------------------------------------------------------------
  public void testReNumeration()
  throws SatException, IOException
  {
    title = "testReNumeration";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    MetadataTreeNode na1 = new MetadataTreeNode( a1, null, null )
      .addQuery(d1)
      .addQuery(b2)
    ;
    
    ArtifactBasicMetadata c2q = new ArtifactBasicMetadata("t:c:[2,4)");
    
    MetadataTreeNode nd1 = new MetadataTreeNode( d1, na1, d1 )
      .addQuery( c2q )
    ;
    MetadataTreeNode nb2 = new MetadataTreeNode( b2, na1, b2 )
      .addQuery( c1 )
    ;
    
    MetadataTreeNode nc2 = new MetadataTreeNode( c2, nd1, c2q );

    MetadataTreeNode nc1 = new MetadataTreeNode( c1, nb2, c1 );
    
    na1
      .addChild(nd1)
      .addChild(nb2)
    ;
    
    nd1
      .addChild(nc2)
    ;
    
    nb2
      .addChild(nc1)
    ;
    
    MetadataTreeNode.reNumber( na1, 1 );
    
    assertEquals( 1, na1.getId() );
    
    assertEquals( 2, nd1.getId() );
    
  }
  //----------------------------------------------------------------------
  public void testOptimization()
  throws SatException
  {
    title = "optimization test";
    System.out.println("\n\n==========================\n"+title+"\n");

    //       b:b:1
    //      / 
    // a:a:1       b:b:2
    //    \     /
    //     c:c:1
    //          \ b:b:1
    
    ArtifactMetadata mdaa1 = new ArtifactMetadata("a:a:1");
    ArtifactMetadata mdbb1 = new ArtifactMetadata("b:b:1");
    ArtifactMetadata mdbb2 = new ArtifactMetadata("b:b:2");
    ArtifactMetadata mdcc1 = new ArtifactMetadata("c:c:1");
    
    MetadataTreeNode aa1;
    MetadataTreeNode bb1;
    MetadataTreeNode cc1;
    MetadataTreeNode cc1bb1;
    MetadataTreeNode cc1bb2;
    
    aa1 = new MetadataTreeNode( mdaa1, null, mdaa1 );
    bb1 = new MetadataTreeNode( mdbb1, aa1, mdbb1 );
    aa1.addChild(bb1);
    cc1 = new MetadataTreeNode( mdcc1, aa1, mdcc1 );
    aa1.addChild(cc1);
    cc1bb1 = new MetadataTreeNode( mdbb1, cc1, mdbb1 );
    cc1.addChild( cc1bb1 );
    cc1bb2 = new MetadataTreeNode( mdbb2, cc1, mdbb2 );
    cc1.addChild( cc1bb2 );
    
    ss = (DefaultSatSolver) DefaultSatSolver.create( aa1 );
    
    List< Comparator<MetadataTreeNode>> comparators = new ArrayList<Comparator<MetadataTreeNode>>(2);
    comparators.add( new ClassicDepthComparator() );
    comparators.add( new ClassicVersionComparator() );
    
    Map<String, List<MetadataTreeNode>> buckets = new HashMap<String, List<MetadataTreeNode>>(128);
    DefaultSatSolver.fillBuckets( buckets, aa1 );
    DefaultSatSolver.sortBuckets( buckets, comparators );

    assertNotNull("no resulting map", buckets ); 
    assertEquals( "bad map size", 3, buckets.size() ); 
    
    System.out.println("optimized buckets: "+buckets);
    
    List<MetadataTreeNode> bbl = buckets.get("b:b");
    assertNotNull("no b:b list", bbl );
    assertEquals( "bad b:b list size", 2, bbl.size() ); 
    
    MetadataTreeNode first = bbl.get(0);
    assertNotNull("bad first element", first );
    assertEquals( "bad first element's GAV", "b:b:1::jar", first.getMd().getGAV() );
    assertEquals( "bad first element's depth", 1, first.getDepth() );
    
    MetadataTreeNode second = bbl.get(1);
    assertNotNull("bad second element", second );
    assertEquals( "bad second element's GAV", "b:b:2::jar", second.getMd().getGAV() );
    assertEquals( "bad second element's depth", 2, second.getDepth() );
  }
  //----------------------------------------------------------------------
  //
  // a1
  //    and (b1 or b2 or b3)
  //    and ((c1 and b1) or (c2 and (b2 or b3))
  //
  //----------------------------------------------------------------------
  public void testClassictResolution()
  throws SatException
  {
    title = "simplest 3-node tree";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    MetadataTreeNode na1 = new MetadataTreeNode( a1, null, null )
      .addQuery(b1)
      .addQuery(c1)
    ;
    
    MetadataTreeNode nb1 = new MetadataTreeNode( b1, na1, b1 );
    MetadataTreeNode nb2 = new MetadataTreeNode( b2, na1, b1 );
    MetadataTreeNode nb3 = new MetadataTreeNode( b3, na1, b1 );
    
    MetadataTreeNode nc1 = new MetadataTreeNode( c1, na1, c1 )
      .addQuery(b1)
    ;
    MetadataTreeNode nc2 = new MetadataTreeNode( c2, na1, c1 )
      .addQuery(b1)
    ;
    
    na1
      .addChild(nb1)
      .addChild(nb2)
      .addChild(nb3)
      .addChild(nc1)
      .addChild(nc2)
    ;
    
    nc1
      .addChild(nb1)
    ;
    
    nc2
      .addChild(nb2)
      .addChild(nb3)
    ;

    ss = (DefaultSatSolver) DefaultSatSolver.create(na1);

    List<ArtifactMetadata> res = ss.solve();
    
    int m[] = ss._solver.model();

    System.out.print("model: " );
    for( int i=0; i<m.length; i++ )
      System.out.print(" "+m[i]);
    System.out.println("");

    assertNotNull( res );
    
    System.out.print("Result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
    System.out.println("");

    assertEquals( 3, res.size() );
    
    assertTrue( res.contains( a1 ) );
    assertTrue( res.contains( b2 ) );
    assertTrue( res.contains( c2 ) );
  }
  //----------------------------------------------------------------------
  //       b:b:1 - c:c:2
  //      / 
  // a:a:1
  //      \     
  //       b:b:2 - c:c:1
  //----------------------------------------------------------------------
  public void testSingleVersionResolution()
  throws SatException
  {
    title = "testSingleVersionResolution";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    MetadataTreeNode na1 = new MetadataTreeNode( a1, null, null )
      .addQuery(b1)
      .addQuery(b2)
    ;
    
    MetadataTreeNode nb1 = new MetadataTreeNode( b1, na1, b1 )
      .addQuery( c2 )
    ;
    MetadataTreeNode nb2 = new MetadataTreeNode( b2, na1, b2 )
      .addQuery( c1 )
    ;
    
    MetadataTreeNode nc2 = new MetadataTreeNode( c2, nb1, c2 );

    MetadataTreeNode nc1 = new MetadataTreeNode( c1, nb2, c1 );
    
    na1
      .addChild(nb1)
      .addChild(nb2)
    ;
    
    nb1
      .addChild(nc2)
    ;
    
    nb2
      .addChild(nc1)
    ;
    
    List<Comparator<MetadataTreeNode>> cl = new ArrayList<Comparator<MetadataTreeNode>>(2);
    cl.add( new ClassicDepthComparator() );
    cl.add( new ClassicVersionComparator() );

    ss = (DefaultSatSolver) DefaultSatSolver.create(na1);
    
    ss.applyPolicies( cl );

    List<ArtifactMetadata> res = ss.solve();
    
    int m[] = ss._solver.model();

    System.out.print("model: " );

    for( int i=0; i<m.length; i++ )
      System.out.print(" "+m[i]);
    System.out.println("");

    assertNotNull( res );
    
    System.out.print("Result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
    System.out.println("");
    
    assertEquals( 3, res.size() );
    
    assertTrue( res.contains( a1 ) );
    assertTrue( res.contains( b2 ) );
    assertTrue( res.contains( c1 ) );
  }
  //----------------------------------------------------------------------
  //       b:b:1 - c:c:[2,4)
  //      / 
  // a:a:1
  //      \     
  //       b:b:2 - c:c:1
  //----------------------------------------------------------------------
  public void testStrictRangeResolution()
  throws SatException
  {
    title = "testSingleVersionResolution";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    MetadataTreeNode na1 = new MetadataTreeNode( a1, null, null )
      .addQuery(b1)
      .addQuery(b2)
    ;
    
    ArtifactBasicMetadata c2q = new ArtifactBasicMetadata("t:c:[2,4)");
    
    MetadataTreeNode nb1 = new MetadataTreeNode( b1, na1, b1 )
      .addQuery( c2q )
    ;
    MetadataTreeNode nb2 = new MetadataTreeNode( b2, na1, b2 )
      .addQuery( c1 )
    ;
    
    MetadataTreeNode nc2 = new MetadataTreeNode( c2, nb1, c2q );

    MetadataTreeNode nc1 = new MetadataTreeNode( c1, nb2, c1 );
    
    na1
      .addChild(nb1)
      .addChild(nb2)
    ;
    
    nb1
      .addChild(nc2)
    ;
    
    nb2
      .addChild(nc1)
    ;
    
    List<Comparator<MetadataTreeNode>> cl = new ArrayList<Comparator<MetadataTreeNode>>(2);
    cl.add( new ClassicDepthComparator() );
    cl.add( new ClassicVersionComparator() );

    ss = (DefaultSatSolver) DefaultSatSolver.create(na1);
    
    ss.applyPolicies( cl );

    List<ArtifactMetadata> res = ss.solve();
    
    int m[] = ss._solver.model();

    System.out.print("model: " );

    for( int i=0; i<m.length; i++ )
      System.out.print(" "+m[i]);
    System.out.println("");

    assertNotNull( res );
    
    System.out.print("Result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
    System.out.println("");
    
    assertEquals( 3, res.size() );
    
    assertTrue( res.contains( a1 ) );
    assertTrue( res.contains( b2 ) );
    assertTrue( res.contains( c1 ) );
  }
  //----------------------------------------------------------------------
  //       d:d:1 - c:c:[2,4)
  //      / 
  // a:a:1
  //      \     
  //       b:b:2 - c:c:1
  //----------------------------------------------------------------------
  public void testStrictRangeResolution2()
  throws SatException
  {
    title = "testSingleVersionResolution";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    MetadataTreeNode na1 = new MetadataTreeNode( a1, null, null )
      .addQuery(d1)
      .addQuery(b2)
    ;
    
    ArtifactBasicMetadata c2q = new ArtifactBasicMetadata("t:c:[2,4)");
    
    MetadataTreeNode nd1 = new MetadataTreeNode( d1, na1, d1 )
      .addQuery( c2q )
    ;
    MetadataTreeNode nb2 = new MetadataTreeNode( b2, na1, b2 )
      .addQuery( c1 )
    ;
    
    MetadataTreeNode nc2 = new MetadataTreeNode( c2, nd1, c2q );

    MetadataTreeNode nc1 = new MetadataTreeNode( c1, nb2, c1 );
    
    na1
      .addChild(nd1)
      .addChild(nb2)
    ;
    
    nd1
      .addChild(nc2)
    ;
    
    nb2
      .addChild(nc1)
    ;
    
    List<Comparator<MetadataTreeNode>> cl = new ArrayList<Comparator<MetadataTreeNode>>(2);
    cl.add( new ClassicDepthComparator() );
    cl.add( new ClassicVersionComparator() );

    ss = (DefaultSatSolver) DefaultSatSolver.create(na1);
    
    ss.applyPolicies( cl );

    List<ArtifactMetadata> res = ss.solve();
    
    int m[] = ss._solver.model();

    System.out.print("model: " );

    for( int i=0; i<m.length; i++ )
      System.out.print(" "+m[i]);
    System.out.println("");

    assertNotNull( res );
    
    System.out.print("Result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
    System.out.println("");
    
    assertEquals( 4, res.size() );
    
    assertTrue( res.contains( a1 ) );
    assertTrue( res.contains( d1 ) );
    assertTrue( res.contains( b2 ) );
    assertTrue( res.contains( c2 ) );
  }
  
  //----------------------------------------------------------------------
  //       d:d:1 - c:c:[2,4)
  //      / 
  // a:a:1
  //      \     
  //       b:b:2 - c:c:1
  //----------------------------------------------------------------------
  public void testSolveAsTree()
  throws SatException, IOException
  {
    title = "testSolveAsTree";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    MetadataTreeNode na1 = new MetadataTreeNode( a1, null, null )
      .addQuery(d1)
      .addQuery(b2)
    ;
    
    ArtifactBasicMetadata c2q = new ArtifactBasicMetadata("t:c:[2,4)");
    
    MetadataTreeNode nd1 = new MetadataTreeNode( d1, na1, d1 )
      .addQuery( c2q )
    ;
    MetadataTreeNode nb2 = new MetadataTreeNode( b2, na1, b2 )
      .addQuery( c1 )
    ;
    
    MetadataTreeNode nc2 = new MetadataTreeNode( c2, nd1, c2q );

    MetadataTreeNode nc1 = new MetadataTreeNode( c1, nb2, c1 );
    
    na1
      .addChild(nd1)
      .addChild(nb2)
    ;
    
    nd1
      .addChild(nc2)
    ;
    
    nb2
      .addChild(nc1)
    ;
    
    List<Comparator<MetadataTreeNode>> cl = new ArrayList<Comparator<MetadataTreeNode>>(2);
    cl.add( new ClassicDepthComparator() );
    cl.add( new ClassicVersionComparator() );

    ss = (DefaultSatSolver) DefaultSatSolver.create(na1);
    
    ss.applyPolicies( cl );

    MetadataTreeNode res = ss.solveAsTree();
    
    assertNotNull( res );
    
    MetadataTreeNode.showNode( res, 0 );
    
    assertEquals( 4, res.countNodes() );
    
  }
  //----------------------------------------------------------------------
  //----------------------------------------------------------------------
}
