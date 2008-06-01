package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class DefaultSatSolver
implements SatSolver
{
  protected SatContext context;
  protected IPBSolver solver = SolverFactory.newEclipseP2();
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
  // x ∨ y = ¬(¬x∧¬y)  
  // x ∨ y ∨ z  = ¬(¬x ∧ ¬(y ∨ z)) = ¬(¬x ∧ ¬(¬(¬y∧¬z))) = ¬(¬x ∧ ¬y ∧ ¬z )
  // x ∧ y => x+y >= 2, ¬x ∧ ¬y => x+y < 1
  //-----------------------------------------------------------------------
  public static SatSolver create( MetadataTreeNode tree )
  throws SatException
  {
    return new DefaultSatSolver( tree );
  }
  //-----------------------------------------------------------------------
  public DefaultSatSolver( MetadataTreeNode tree )
  throws SatException
  {
    if( tree == null)
      throw new SatException("cannot create a solver for an empty [null] tree");
    
    int nNodes = tree.countNodes();
    context = new SatContext( nNodes );
    setVars( tree );
    
    solver.newVar( context.varCount );
  }
  //-----------------------------------------------------------------------
  // test only factory & constructor
  protected static SatSolver create( int nVars )
  throws SatException
  {
    return new DefaultSatSolver( nVars );
  }
  
  protected DefaultSatSolver( int nVars )
  throws SatException
  {
    context = new SatContext( nVars );
    solver.newVar( nVars );
  }
  //-----------------------------------------------------------------------
  private final void setVars( MetadataTreeNode tree )
  throws SatException
  {
    if( tree == null )
      return;
    
    context.find( tree.getMd() );
    if( tree.getChildren() != null )
      for( MetadataTreeNode child : tree.getChildren() )
      {
        setVars( child );
      }
  }
  //-----------------------------------------------------------------------
  public SatConstraint addOrGroup( List<List<ArtifactMetadata>> orGroup )
  throws SatException
  {
    SatConstraint constraint = null;
    for( List<ArtifactMetadata> branch : orGroup )
    {
      if( constraint == null )
        constraint = new SatConstraint( branch, context );
      else
        constraint.addOr( branch, context );
    }
    
    if( constraint != null )
    {
      SatClause clause = constraint.getClause();
      try
      {
        solver.addPseudoBoolean( 
                      new VecInt( clause._vars )
                    , new Vec<BigInteger>( clause._coeff )
                    , true
                    , new BigInteger(""+constraint.cardinality) 
                                );
      }
      catch (ContradictionException e)
      {
        throw new SatException( e );
      }
    }
    
    return constraint;
  }
  //-----------------------------------------------------------------------
  public SatConstraint addPivot( List<ArtifactMetadata> pivot )
  throws SatException
  {
    SatConstraint constraint = new SatConstraint( pivot, context );

System.out.print("\n context is : "+context.toString() );
System.out.println("\n pivot is : "+pivot );

    try
    {
      int [] vars1 = constraint.getVarray();
      int varCount1 = vars1.length;

System.out.print("\n\n array is :");SatHelper.show(vars1);System.out.print("\n");
System.out.flush();
      
      solver.addPseudoBoolean( 
          SatHelper.getSmallOnes( vars1 )
        , SatHelper.getBigOnes( varCount1 )
        , true
        , new BigInteger("1") 
                    );

      int [] vars2 = constraint.getVarray();
      int varCount2 = vars2.length;

System.out.print(">= 1 is OK\n array is :");SatHelper.show(vars2);System.out.print("\n");
System.out.flush();

      solver.addPseudoBoolean( 
            SatHelper.getSmallOnes( vars2 )
          , SatHelper.getBigOnes( varCount2, true )
          , true
          , new BigInteger("-1") 
                    );

System.out.println(">= -1 is OK");
System.out.flush();
    }
    catch (ContradictionException e)
    {
e.printStackTrace();
      throw new SatException( e );
    }
    
    return constraint;
  }
  //-----------------------------------------------------------------------
  public List<ArtifactMetadata> solve()
  throws SatException
  {
    List<ArtifactMetadata> res = null;
    
    try
    {
      if( solver.isSatisfiable() )
      {
        res = new ArrayList<ArtifactMetadata>( context.varCount );
System.out.println("Resulting model : "+solver.model() );

        for( SatVar v : context.variables )
        {
          boolean yes = solver.model( v.getNo() );
          if( yes )
            res.add( v.getMd() );
        }
      }
      else
      {
System.out.println("Failed to solve the problem");
      }
    }
    catch (TimeoutException e)
    {
      throw new SatException( e );
    }
    return res;
  }
  //-----------------------------------------------------------------------
  private int addConstraint( ArtifactMetadata md )
  {
    return -1;
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
