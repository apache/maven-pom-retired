package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class SatFeedTest
extends TestCase
{
  IPBSolver pbSolver;
  
  int [][] svars = new int [][]
  { 
            { 1 }
          , { 2 }
  };
  BigInteger [] scoeffs = new BigInteger [] { BigInteger.ONE };
  
  int [][] vars = new int [][]
  { 
          { 1, 2, 3 }
        , { 1, 2, 4 }
        , { 2, 3, 4 }
  };
  BigInteger [] coeffs = new BigInteger [] { BigInteger.ONE, BigInteger.ONE, BigInteger.ONE };
  //---------------------------------------------------------------
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    pbSolver = SolverFactory.newEclipseP2(); //.newDefault();
    pbSolver.setTimeout( 60 );
//    int [] explainVars = new int [] { 0, 1, 2, 3 };
//    pbSolver.setListOfVariablesForExplanation( new VecInt(explainVars) );
  }
  //---------------------------------------------------------------
  private void showRes( String msg )
  {

    System.out.println("\n----  "+msg+"  -------------" );
    int [] m = pbSolver.model();

    if( m != null )
    {
      for( int i=1; i<=pbSolver.nVars(); i++ )
        System.out.print(" x"+i+"="+pbSolver.model(i) );

      System.out.print( "\nModel: " );
      for( int i=0; i<m.length; i++ )
        System.out.print(" "+m[i]);
    }
    else
      System.out.print(" null model :(");
    
    System.out.println("\n----------------------------");
  }
  //---------------------------------------------------------------
  public void testSimpleFeed()
  throws ContradictionException, TimeoutException
  {
    String title = "x1 is true";
    pbSolver.newVar(2);
    // x1 >= 1
    pbSolver.addPseudoBoolean(
        new VecInt(svars[0])
      , new Vec<BigInteger>( scoeffs )
      , true, new BigInteger("1") 
      );
    // x2 < 1
    pbSolver.addPseudoBoolean(
        new VecInt(svars[1])
      , new Vec<BigInteger>( scoeffs )
      , false, new BigInteger("1") 
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testFeed()
  throws ContradictionException, TimeoutException
  {
    String title = "4 var system";
    pbSolver.newVar(4);
    // x1 + x2 + x3 >= 2
    pbSolver.addPseudoBoolean(
        new VecInt(vars[0])
      , new Vec<BigInteger>( coeffs )
      , true, new BigInteger("2") 
      );
    // x1 + x2 + x4 >= 2
    pbSolver.addPseudoBoolean(
        new VecInt(vars[1])
      , new Vec<BigInteger>( coeffs )
      , true, new BigInteger("2") 
      );
    // x2 + x3 + x4 < 2
    pbSolver.addPseudoBoolean(
        new VecInt(vars[2])
      , new Vec<BigInteger>( coeffs )
      , false, new BigInteger("2") 
      );

    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testEqalityFeed()
  throws ContradictionException, TimeoutException
  {
    String title = "Equality";
    pbSolver.newVar(2);
    // atleast( 1, {x1,x2} )
    // x1+x2 >= 1 
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2 )
      , SatHelper.getBigOnes(2)
      , true, new BigInteger("1") 
      );
    // atmost( 1 ,{x1,x2} )
    // (1-x1) + (1-x2)  >= (2-1)=1
    // 2-x1-x2 >= 1
    // -x1-x2 >= -1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2 )
      , SatHelper.getBigOnes( 2, true )
      , true, new BigInteger("-1")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testEqality3Feed()
  throws ContradictionException, TimeoutException
  {
    String title = "Equality 3 vars";
    pbSolver.newVar(3);
    // atleast( 1, {x1,x2,x3} )
    // x1+x2 >= 1 
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3 )
      , SatHelper.getBigOnes(3)
      , true, new BigInteger("1") 
      );
    // atmost( 1 ,{x1,x2} )
    // (1-x1) + (1-x2)  >= (2-1)=1
    // 2-x1-x2 >= 1
    // -x1-x2 >= -1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3 )
      , SatHelper.getBigOnes( 3, true )
      , true, new BigInteger("-1")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testEqality4Feed()
  throws ContradictionException, TimeoutException
  {
    String title = "Equality 4 vars";
    pbSolver.newVar(4);
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3, 4 )
      , SatHelper.getBigOnes(4)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3, 4 )
      , SatHelper.getBigOnes( 4, true )
      , true, new BigInteger("-1")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testEqalityRealFeed()
  throws ContradictionException, TimeoutException
  {
    String title = "Equality Real problem";
    pbSolver.newVar(4);
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3, 4 )
      , SatHelper.getBigOnes(4)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3, 4 )
      , SatHelper.getBigOnes( 4, true )
      , true, new BigInteger("-1")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testSmallestRealProblem()
  throws ContradictionException, TimeoutException
  {
    //      x2
    // x1 <or
    //      x3
    String title = "Smallest Real problem";
    pbSolver.newVar(3);
    
    // x2 + x3 = 1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 2, 3 )
      , SatHelper.getBigOnes(2)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 2, 3 )
      , SatHelper.getBigOnes( 2, true )
      , true, new BigInteger("-1")
      );

    // x1 + x2 >= 2 OR x1 + x3 >= 2
    // x1 + x2 + x3 >= 2
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3 )
      , SatHelper.getBigOnes( 3 )
      , true, new BigInteger("2")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    
    assert pbSolver.model(1) : "x1 should be true";
    assert pbSolver.model(2) || pbSolver.model(3) : "x2 or x3 should be true";
    
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testTwoStepRealProblem()
  throws ContradictionException, TimeoutException
  {
    //           x3
    //      x2 <or
    //      /    x4 
    // x1 <
    //      \
    //      x5 - x6
    //        \ or
    //         x7
    String title = "2-step real problem";
    pbSolver.newVar(7);
    
    // x3 + x4 = 1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 3, 4 )
      , SatHelper.getBigOnes(2)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 3, 4 )
      , SatHelper.getBigOnes( 2, true )
      , true, new BigInteger("-1")
      );
    
    // x6 + x7 = 1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 6, 7 )
      , SatHelper.getBigOnes(2)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 6, 7 )
      , SatHelper.getBigOnes( 2, true )
      , true, new BigInteger("-1")
      );

    // x1 + x2 + x3 >= 2 OR x1 + x2 + x4 >= 2
    // x1 + x2 + x3 +x4 >= 3
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3, 4 )
      , SatHelper.getBigOnes( 4 )
      , true, new BigInteger("3")
      );

    // x1 + x5 + x6 >= 3 OR x1 + x5 + x7 >= 3
    // x1 + x5 + x6 + x7 >= 3
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 5, 6, 7 )
      , SatHelper.getBigOnes( 4 )
      , true, new BigInteger("3")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    
    assert pbSolver.model(1) : "x1 should be true";
    assert pbSolver.model(2) : "x2 should be true";
    assert pbSolver.model(3) || pbSolver.model(4) : "x3 or x4 should be true";
    assert ! (pbSolver.model(3) && pbSolver.model(4)) : "x3 or x4 should be false";
    assert pbSolver.model(5) : "x5 should be true";
    assert pbSolver.model(6) || pbSolver.model(7) : "x6 or x7 should be true";
    assert ! (pbSolver.model(6) && pbSolver.model(7)) : "x6 or x7 should be false";
    
    showRes( title );
  }
  //---------------------------------------------------------------
  public void testTwoOrsInOneBranch()
  throws ContradictionException, TimeoutException
  {
    //             x3
    //        x2 <or
    //        /    x4 
    //       /      \
    //      /        x5 - x6
    //     /          \ or
    //    /            +- x7
    // x1 <
    //     \ x8 - x7
    String title = "really real problem";
    pbSolver.newVar(8);
    
    // x3 + x4 = 1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 3, 4 )
      , SatHelper.getBigOnes(2)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 3, 4 )
      , SatHelper.getBigOnes( 2, true )
      , true, new BigInteger("-1")
      );
    
    // x6 + x7 = 1
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 6, 7 )
      , SatHelper.getBigOnes(2)
      , true, new BigInteger("1") 
      );
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 6, 7 )
      , SatHelper.getBigOnes( 2, true )
      , true, new BigInteger("-1")
      );

    // x1 + x2 + x3 >= 3 OR x1 + x2 + x4 + x5 + x6 >= 5 OR x1 + x2 + x4 + x5 + x7 >= 5
    //  x1 + x2 + x3 >= 3 OR x1 + x2 + x4 + x5 + x6 +x7 >= 5
    //  x1 + x2 + x3 + x4 + x5 + x6 +x7 >= 5
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 2, 3, 4, 5, 6, 7 )
      , SatHelper.getBigOnes( 7 )
      , true, new BigInteger("5")
      );

    // x1 + x8 + x7 >= 3
    pbSolver.addPseudoBoolean(
        SatHelper.getSmallOnes( 1, 8, 7 )
      , SatHelper.getBigOnes( 3 )
      , true, new BigInteger("3")
      );
    
    boolean satisfiable = pbSolver.isSatisfiable();
    assert satisfiable : "Cannot find a solution to "+title+" problem";
    
    assert pbSolver.model(1)
         && pbSolver.model(2)
         && pbSolver.model(4)
         && pbSolver.model(5)
         && pbSolver.model(7)
         && pbSolver.model(8)
    : "x1 & x2 & x4 & x5 & x7 & x8 should be true";
     assert !pbSolver.model(3) : "x3 should be false";
     assert !pbSolver.model(6) : "x6 should be false";
    
    showRes( title );
  }
  //---------------------------------------------------------------
  //---------------------------------------------------------------
}
