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
    ss = (DefaultSatSolver) DefaultSatSolver.create(3);
    or = new ArrayList< List<ArtifactMetadata> >(1);
    or.add( SatHelper.createList("t:a:1","t:b:1" ) );
    ss.addOrGroup(or);
    
    assert ss.context != null : "created solver has a null context";
    assert ss.context.varCount == 2 : "expected 2 variables in the context, but found "+ss.context.varCount;

    or = new ArrayList< List<ArtifactMetadata> >(1);
    or.add( SatHelper.createList("t:a:1","t:b:2" ) );
    ss.addOrGroup(or);
    
    assert ss.context.varCount == 3 : "expected 3 variables in the context, but found "+ss.context.varCount;
  }
  //----------------------------------------------------------------------
  public void testSimpleResolution()
  throws SatException
  {
    title = "simple 3-node tree";
    ss = (DefaultSatSolver) DefaultSatSolver.create(6);
    
    or = new ArrayList< List<ArtifactMetadata> >(3);
    
    ss.addPivot( SatHelper.createList("t:b:1","t:b:2","t:b:3") );

    or.add( SatHelper.createList("t:a:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:b:2") );
    or.add( SatHelper.createList("t:a:1","t:b:3") );
    ss.addOrGroup(or);

    List<ArtifactMetadata> res = ss.solve();
    
    assert res != null : "Failed to solve "+title;
    assert res.size() == 2 : "result contains "+res.size()+" artifacts instead of 2";
    
    if( res != null )
    {
      System.out.println("\nResult:");
      for( ArtifactMetadata md : res )
      {
        System.out.print(" "+md);
      }
    }
  }
  //----------------------------------------------------------------------
  public void testResolution()
  throws SatException
  {
    title = "simple 2 ranges tree";
    System.out.println("\n"+title);
    
    ss = (DefaultSatSolver) DefaultSatSolver.create(6);
    
    or = new ArrayList< List<ArtifactMetadata> >(3);
    
    ss.addPivot( SatHelper.createList("t:b:1","t:b:2","t:b:3") );

    or.add( SatHelper.createList("t:a:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:b:2") );
    or.add( SatHelper.createList("t:a:1","t:b:3") );
    ss.addOrGroup(or);

    or.clear();

    ss.addPivot( SatHelper.createList("t:c:1","t:c:2") );

    or.add( SatHelper.createList("t:a:1","t:c:1","t:b:1") );
    or.add( SatHelper.createList("t:a:1","t:c:2","t:b:2") );
    or.add( SatHelper.createList("t:a:1","t:c:2","t:b:3") );
    ss.addOrGroup(or);
    
    System.out.println( "Context: "+ss.context.toString() );

    List<ArtifactMetadata> res = ss.solve();
    
    assert res != null : "Failed to solve "+title;
//    assert res.size() == 2 : "result contains "+res.size()+" artifacts instead of 2";
    
    System.out.println("\n"+title+" result:");
    for( ArtifactMetadata md : res )
    {
      System.out.print(" "+md);
    }
  }
  //----------------------------------------------------------------------
  //----------------------------------------------------------------------
}
