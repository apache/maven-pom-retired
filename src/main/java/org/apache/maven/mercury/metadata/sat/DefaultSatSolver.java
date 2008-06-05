package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
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
    setPivots( tree );
    setOthers( tree );
    
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
  private final void setPivots( MetadataTreeNode tree )
  throws SatException
  {
    if( tree == null )
      return;

// TODO implement    
if(true) throw new SatException("Not implemented yet");
    
    context.addWeak( tree.getMd() );
    if( tree.getChildren() != null )
      for( MetadataTreeNode child : tree.getChildren() )
      {
        setPivots( child );
      }
  }
  //-----------------------------------------------------------------------
  private final void setOthers( MetadataTreeNode tree )
  throws SatException
  {
    if( tree == null )
      return;
    
    context.addStrong( tree.getMd() );
    if( tree.getChildren() != null )
      for( MetadataTreeNode child : tree.getChildren() )
      {
        setOthers( child );
      }
  }
  //-----------------------------------------------------------------------
  private List<ArtifactMetadata> groupCommonHear( List<List<ArtifactMetadata>> orGroup )
  throws SatException
  {
    if( orGroup == null || orGroup.size() < 1 )
      throw new SatException("cannot scan empty group for common head");
    
    int groupSize = orGroup.size();
    List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( orGroup.get(0) );
    
    // one member? - done
    if( groupSize == 1 )
      return res;
    
    int index = 0;

    for( List<ArtifactMetadata> branch : orGroup )
    {
      if( index++ == 0 )
        continue;

      for( ArtifactMetadata md : branch )
      {
        int len = res.size();
        
        for( int i=0; i<len; i++ )
        {
          ArtifactMetadata hmd = res.get(i);

          if( hmd.sameGAV(md) )
            continue;

          // remove the rest
          for( int j=i; j<len; j++ )
            res.remove(i);
          
          break;
        }
      }
    }
    
    return res;
  }
  //-----------------------------------------------------------------------
  private int calcWeekness( List<ArtifactMetadata> branch, int currentMin )
  {
    int res = 0;
    
    for( ArtifactMetadata md : branch )
    {
      SatVar v = context.find(md);
      
      if( v.isWeak() )
        ++res;
    }
    
    return res < currentMin ? res : currentMin;
  }
  //-----------------------------------------------------------------------
  public SatConstraint addOrGroup( List<List<ArtifactMetadata>> orGroup )
  throws SatException
  {
    if( orGroup == null || orGroup.size() < 1 )
      throw new SatException("cannot process empty OR group");
    
    try
    {
      SatConstraint constraint = null;
      int groupSize = orGroup.size();
      int maxLen = 0;
      int minWeekness = Integer.MAX_VALUE;
   
      for( List<ArtifactMetadata> branch : orGroup )
      {
        if( constraint == null )
          constraint = new SatConstraint( branch, context, SatContext.STRONG_VAR );
        else
          constraint.addOrGroupMember( branch, context );
  
        if( maxLen < branch.size() )
          maxLen = branch.size();
        
        minWeekness = calcWeekness(branch, minWeekness );
      }
  
      // second pass - adjust each branch with strong variable up to the max length
      for( List<ArtifactMetadata> branch : orGroup )
      {
        constraint.adjust( branch, maxLen, minWeekness, context );
      }
      
      // 3rd pass - generate branch structure implications
      for( List<ArtifactMetadata> branch : orGroup )
      {
        int blen = branch.size();
        if( blen == 1 )
          break;
        
        SatVar currLit = context.find( branch.get(0) );
        SatVar nextLit = null;
        
        for( int i=1; i < blen; i++ )
        {
          nextLit = context.find( branch.get(i) );
System.out.println(nextLit._md+" -> "+currLit._md + " : -x"+ nextLit._no+" +x"+currLit._no+" >= -1");
          // generate implication nextLit -> currLit
          solver.addPseudoBoolean( 
              SatHelper.getSmallOnes( nextLit._no, currLit._no )
            , SatHelper.getBigOnes( -1, 1 )
            , true
            , new BigInteger("0") 
                        );
          solver.addPseudoBoolean( 
              SatHelper.getSmallOnes( nextLit._no, currLit._no )
            , SatHelper.getBigOnes( -1, 1 )
            , false
            , new BigInteger("1") 
                      );
          currLit = nextLit;
        }
      }
      
      constraint.finalAdjust( orGroup.get(0), groupSize, minWeekness, context );
  
      if( constraint != null )
      {
        constraint.cardinality = groupSize * maxLen;
System.out.println("Contraint is "+constraint.toString() );
        SatClause clause = constraint.getClause();
          solver.addPseudoBoolean( 
                        SatHelper.getSmallOnes( clause._vars )
                      , SatHelper.getBigOnes( clause._coeff )
                      , true
                      , new BigInteger(""+constraint.cardinality) 
                                  );
      }
      
      return constraint;
    }
    catch (ContradictionException e)
    {
      throw new SatException( e );
    }
  }
  //-----------------------------------------------------------------------
  public SatConstraint addPivot( List<ArtifactMetadata> pivot )
  throws SatException
  {
System.out.println("Pivot: " + pivot );
    SatConstraint constraint = new SatConstraint( pivot, context, SatContext.WEAK_VAR );

    try
    {
      int [] vars1 = constraint.getVarray();
      int varCount1 = vars1.length;

      solver.addPseudoBoolean( 
          SatHelper.getSmallOnes( vars1 )
        , SatHelper.getBigOnes( varCount1 )
        , true
        , new BigInteger("1") 
                    );

      int [] vars2 = constraint.getVarray();
      int varCount2 = vars2.length;

      solver.addPseudoBoolean( 
            SatHelper.getSmallOnes( vars2 )
          , SatHelper.getBigOnes( varCount2, true )
          , true
          , new BigInteger("-1") 
                    );
    }
    catch (ContradictionException e)
    {
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
        for( SatVar v : context.variables )
        {
          boolean yes = solver.model( v.getNo() );
          if( yes )
            res.add( v.getMd() );
        }
      }
    }
    catch (TimeoutException e)
    {
      throw new SatException( e );
    }
    return res;
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
