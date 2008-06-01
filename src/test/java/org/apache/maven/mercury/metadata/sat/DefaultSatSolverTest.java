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
  public void testResolution()
  throws SatException
  {
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

    List<ArtifactMetadata> res = ss.solve();
    
    if( res != null )
      for( ArtifactMetadata md : res )
      {
        System.out.println(md);
      }
    else
      System.out.println( "Result: "+res);
  }
  //----------------------------------------------------------------------
  //----------------------------------------------------------------------
}
