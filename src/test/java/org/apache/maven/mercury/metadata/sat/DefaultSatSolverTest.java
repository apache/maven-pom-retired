package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

import junit.framework.TestCase;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class DefaultSatSolverTest
    extends TestCase
{
  DefaultSatSolver ss;
  String title;
  List< List<ArtifactMetadata> > or;
  SatConstraint constraint; 
  
  //----------------------------------------------------------------------
  protected void setUp()
  throws Exception
  {
    super.setUp();
    
    or = new ArrayList< List<ArtifactMetadata> >(8);

  }
  //----------------------------------------------------------------------
  public void testContext()
  throws SatException
  {
    ss = (DefaultSatSolver) DefaultSatSolver.create(3);

    ss.addPivot(SatHelper.createList("t:b:1","t:b:2" ));
    
    assert ss.context != null : "created solver has a null context";
    assert ss.context.varCount == 2 : "expected 2 variables in the context, but found "+ss.context.varCount;

    or = new ArrayList< List<ArtifactMetadata> >(1);

    or.add( SatHelper.createList("t:a:1","t:b:1" ) );
    ss.addOrGroup(or);
    
    assert ss.context.varCount == 3 : "expected 3 variables in the context, but found "+ss.context.varCount;

    or = new ArrayList< List<ArtifactMetadata> >(1);
    or.add( SatHelper.createList("t:a:1","t:b:2" ) );
    ss.addOrGroup(or);
    
    assert ss.context.varCount == 3 : "expected 3 variables in the context, but found "+ss.context.varCount;
  }
  //----------------------------------------------------------------------
  public void testSimplestResolution()
  throws SatException
  {
    title = "simplest 3-node tree";
    System.out.println("\n"+title+"\n");

    ss = (DefaultSatSolver) DefaultSatSolver.create(3);

    constraint = ss.addPivot( SatHelper.createList("t:b:1","t:b:2") );
    System.out.println("Pivot: "+constraint.toString() );

    or.add( SatHelper.createList("t:a:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:b:2") );
    constraint = ss.addOrGroup(or);
    System.out.println("Constraint: "+constraint.toString() );    
    
    System.out.println("\nContext: "+ss.context.toString()+"\n" );

    List<ArtifactMetadata> res = ss.solve();
System.out.print("model: " );
int m[] = ss.solver.model();
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

    assert res.size() == 2 : "result contains "+res.size()+" artifacts instead of 2";
  }
  //----------------------------------------------------------------------
  public void testSimpleResolution()
  throws SatException
  {
    title = "simple 4-node tree";
    System.out.println("\n"+title+"\n");

    ss = (DefaultSatSolver) DefaultSatSolver.create(4);

    constraint = ss.addPivot( SatHelper.createList("t:b:1","t:b:2","t:b:3") );
    System.out.println("Pivot: "+constraint.toString() );
    
    or.add( SatHelper.createList("t:a:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:b:2") );
    or.add( SatHelper.createList("t:a:1","t:b:3") );
    constraint = ss.addOrGroup(or);
    System.out.println("Constraint: "+constraint.toString() );    
    
    System.out.println("\nContext: "+ss.context.toString()+"\n" );

    List<ArtifactMetadata> res = ss.solve();
System.out.print("model: " );
int m[] = ss.solver.model();
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

    assert res.size() == 2 : "result contains "+res.size()+" artifacts instead of 2";
  }
  //----------------------------------------------------------------------
  public void testClassicResolution()
  throws SatException
  {
    title = "Classical ranges tree";
    System.out.println("\n"+title+"\n");
    
    ss = (DefaultSatSolver) DefaultSatSolver.create(6);

    constraint = ss.addPivot( SatHelper.createList("t:b:1","t:b:2","t:b:3") );
    System.out.println("Pivot: "+constraint.toString() );
    
    constraint = ss.addPivot( SatHelper.createList("t:c:1","t:c:2") );
    System.out.println("Pivot: "+constraint.toString() );

    or.add( SatHelper.createList("t:a:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:b:2") );
    or.add( SatHelper.createList("t:a:1","t:b:3") );
    constraint = ss.addOrGroup(or);
    System.out.println("Constraint: "+constraint.toString() );    

    or.clear();

    or.add( SatHelper.createList("t:a:1","t:c:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:c:2","t:b:2") );
    or.add( SatHelper.createList("t:a:1","t:c:2","t:b:3") );
    constraint = ss.addOrGroup(or);
    System.out.println("Constraint: "+constraint.toString() );    

    System.out.println( "Context: "+ss.context.toString() );

    List<ArtifactMetadata> res = ss.solve();
    if(res==null)
      System.out.println("\n"+title+" unsatified");
    
    assert res != null : "Failed to solve "+title;
//    assert res.size() == 2 : "result contains "+res.size()+" artifacts instead of 2";
    
    System.out.println("\n"+title+" result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
  }
  //----------------------------------------------------------------------
  public void testT()
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
