package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.ArtifactScopeEnum;
import org.apache.maven.mercury.metadata.ArtifactMetadata;
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
    ss.context.findOrAdd( b1 );
    ss.context.findOrAdd( b2 );
    
    assert ss.context != null : "created solver has a null context";
    assert ss.context.varCount == 2 : "expected 2 variables in the context, but found "+ss.context.varCount;

    ss.context.findOrAdd( a1 );
    assert ss.context.varCount == 3 : "expected 3 variables in the context, but found "+ss.context.varCount;

    ss.context.findOrAdd( b1 );
    assert ss.context.varCount == 3 : "expected 3 variables in the context, but found "+ss.context.varCount;
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
    
    int m[] = ss.solver.model();

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
