package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.ArtifactScopeEnum;
import org.apache.maven.mercury.metadata.ClassicDepthComparator;
import org.apache.maven.mercury.metadata.ClassicVersionComparator;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

import junit.framework.TestCase;

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
  
  //----------------------------------------------------------------------
  protected void setUp()
  throws Exception
  {
    super.setUp();
  }
  //----------------------------------------------------------------------
  public void testContext()
  throws SatException
  {
    title = "context test";
    System.out.println("\n\n==========================\n"+title+"\n");
    
    ss = (DefaultSatSolver) DefaultSatSolver.create(3);
    ss._context.findOrAdd( b1 );
    ss._context.findOrAdd( b2 );
    
    assert ss._context != null : "created solver has a null context";
    assert ss._context.varCount == 2 : "expected 2 variables in the context, but found "+ss._context.varCount;

    ss._context.findOrAdd( a1 );
    assert ss._context.varCount == 3 : "expected 3 variables in the context, but found "+ss._context.varCount;

    ss._context.findOrAdd( b1 );
    assert ss._context.varCount == 3 : "expected 3 variables in the context, but found "+ss._context.varCount;
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
    
    ss = (DefaultSatSolver) DefaultSatSolver.create( aa1, ArtifactScopeEnum.compile );
    
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
    assertEquals( "bad first element's GAV", "b:b:1", first.getMd().getGAV() );
    assertEquals( "bad first element's depth", 1, first.getDepth() );
    
    MetadataTreeNode second = bbl.get(1);
    assertNotNull("bad second element", second );
    assertEquals( "bad second element's GAV", "b:b:2", second.getMd().getGAV() );
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

    ss = (DefaultSatSolver) DefaultSatSolver.create(na1, ArtifactScopeEnum.compile);

    List<ArtifactMetadata> res = ss.solve();
    
    int m[] = ss._solver.model();

    System.out.print("model: " );
    for( int i=0; i<m.length; i++ )
      System.out.print(" "+m[i]);
    System.out.println("");

    assert res != null : "Failed to solve "+title;
    
    System.out.print("Result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
    System.out.println("");

    assert res.size() == 3 : "result contains "+res.size()+" artifacts instead of 3";
    assert res.contains(a1) : "result does not contain "+a1;
  }
  //----------------------------------------------------------------------
  public void ntestT()
  {
   for( int i=0; i<2; i++)
     for( int j=0; j<2; j++)
       System.out.println(i+", "+j+" -> "+f(i,j));
  }
  private int f( int a, int b )
  {
    return -a + b;
  }
  //----------------------------------------------------------------------
  //----------------------------------------------------------------------
}
