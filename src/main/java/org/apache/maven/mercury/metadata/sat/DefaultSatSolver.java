package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.metadata.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
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
    solver.newVar( context.varCount );
    
    try
    {
      addNode( tree );
    }
    catch (ContradictionException e)
    {
      throw new SatException(e);
    }
  }
  //-----------------------------------------------------------------------
  private final void addPB( IVecInt lits, IVec<BigInteger> coeff, boolean ge, BigInteger cardinality )
  throws ContradictionException
  {
    solver.addPseudoBoolean( lits, coeff, ge, cardinality );
    
    System.out.print("PB: ");
    for( int i=0; i<lits.size(); i++ )
      System.out.print( " "+coeff.get(i)+" x"+lits.get(i) );
    System.out.println(" "+( ge ? ">=" : "<")+" "+cardinality );
  }
  //-----------------------------------------------------------------------
  private Map<ArtifactMetadata, List<MetadataTreeNode>> processChildren(
                                        List<ArtifactMetadata> queries
                                        , List<MetadataTreeNode> children
                                                              )
  throws SatException
  {
    HashMap<ArtifactMetadata, List<MetadataTreeNode>> res = new HashMap<ArtifactMetadata, List<MetadataTreeNode>>( queries.size() );
    for( ArtifactMetadata q : queries )
    {
      if( queries == null || queries.size() < 1 )
        return null;
      
      if( children == null || children.size() < 1 )
        throw new SatException("there are queries, but not results. Queries: "+queries);
      
      List<MetadataTreeNode> bucket = new ArrayList<MetadataTreeNode>(4);
      String queryGA = q.getGA();
      
      for( MetadataTreeNode tn : children )
      {
        if( tn.getMd() == null )
          throw new SatException("resulting tree node without metadata for query "+q );
        
        if( queryGA.equals( tn.getMd().getGA()) )
        {
          bucket.add(tn);
        }
      }
      
      if( bucket.size() < 1 )
        throw new SatException("No children for query "+queryGA );
      
      res.put( q, bucket );
    }

    return res;
  } 
  //-----------------------------------------------------------------------
  private void addNode( MetadataTreeNode node )
  throws ContradictionException, SatException
  {
    if( node == null )
      return;
    
    if( node.getMd() == null )
      throw new SatException("found a node without metadata");
    
    SatVar nodeLit = context.findOrAdd( node.getMd() );

    if( node.getParent() == null )
      {
        addPB( SatHelper.getSmallOnes( nodeLit.getLiteral() )
                              , SatHelper.getBigOnes(1,false)
                              , true, BigInteger.valueOf(1)
                              );
      }
      
      if( ! node.hasChildren() )
        return;
    
    Map<ArtifactMetadata,List<MetadataTreeNode>> kids = processChildren( node.getQueries(), node.getChildren() );
    
    // leaf node
    if( kids == null )
      return;
    
    for( Map.Entry<ArtifactMetadata,List<MetadataTreeNode>> kid : kids.entrySet() )
    {
      ArtifactMetadata query = kid.getKey();
      List<MetadataTreeNode> range = kid.getValue();

      if( range.size() > 1 )
      {
        int [] literals = addRange( range, query.isOptional() );
        int litCount =  literals.length;
        
        addPB( SatHelper.getSmallOnes( SatHelper.toIntArray( nodeLit.getLiteral(), literals ) )
                              , SatHelper.getBigOnes(-1,litCount,false)
                              , true, BigInteger.ZERO
                              );
        for( MetadataTreeNode tn : range )
        {
          addNode( tn );
        }
      }
      else
      {
        MetadataTreeNode child = range.get(0);
        SatVar kidLit = context.findOrAdd( child.getMd() );
        
        addPB( SatHelper.getSmallOnes( new int [] { nodeLit.getLiteral(), kidLit.getLiteral() } )
                              , SatHelper.getBigOnes( -1, 1 )
                              , true, BigInteger.ZERO
                              );
        addNode( child );
      }

    }
  }
  //-----------------------------------------------------------------------
  private int [] addRange( List<MetadataTreeNode> range, boolean optional )
  throws ContradictionException, SatException
  {
    SatVar literal;
    
    int [] literals = new int[ range.size() ];
    
    int count = 0;
    
    for( MetadataTreeNode tn : range )
    {
      literal = context.findOrAdd( tn.getMd() );
      literals[count++] = literal.getLiteral();
    }
    
    if( optional ) // Sxi >= 0
    {
      addPB( SatHelper.getSmallOnes( literals )
          , SatHelper.getBigOnes( count, false )
          , true, BigInteger.ZERO
          );
    }
    else // Sxi = 1
    {
      addPB( 
          SatHelper.getSmallOnes( literals )
        , SatHelper.getBigOnes( count, false )
        , true
        , new BigInteger("1") 
                    );

      addPB( 
            SatHelper.getSmallOnes( literals )
          , SatHelper.getBigOnes( count, true )
          , true
          , new BigInteger("-1") 
                    );
    }
    
    return literals;
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
          boolean yes = solver.model( v.getLiteral() );
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
