package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Default SAT4J implementation.
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DefaultSatSolver
implements SatSolver
{
//  private static final Log _log = LogFactoryImpl.getLog( DefaultSatSolver.class );
  
  protected SatContext _context;
  protected IPBSolver _solver = SolverFactory.newEclipseP2();
  protected MetadataTreeNode _root;
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
    _context = new SatContext( nNodes );
    _solver.newVar( _context.varCount );
    
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
  public final void applyPolicies( List<Comparator<MetadataTreeNode>> comparators )
  throws SatException
  {
    if( comparators == null || comparators.size() < 1 )
      return;
    
    // TODO og: assumption - around 128 GA's per tree
    Map<String, List<MetadataTreeNode>> buckets = new HashMap<String, List<MetadataTreeNode>>(128);
    fillBuckets( buckets, _root );
    sortBuckets( buckets, comparators );
  }
  
  private void sortBuckets(
        Map<String, List<MetadataTreeNode>> buckets
      , List<Comparator<MetadataTreeNode>> comparators
      )
  {
    for( List<MetadataTreeNode> bucket : buckets.values() )
    {
      for( Comparator<MetadataTreeNode> comparator : comparators )
      {
        Collections.sort( bucket, comparator);
      }
    }
    
  }
  
  private void fillBuckets(
        Map<String, List<MetadataTreeNode>> buckets
      , MetadataTreeNode node
                          )
  {
    String ga = node.getMd().getGA();
    List<MetadataTreeNode> bucket = buckets.get(ga);
    if( bucket == null )
    {
      // TODO og: assumption - around 32 different versions per GA
      bucket = new ArrayList<MetadataTreeNode>( 32 );
      buckets.put(ga, bucket );
    }
    
    bucket.add( node );
    
    if( ! node.hasChildren() )
      return;
    
    for( MetadataTreeNode kid : node.getChildren() )
    {
      fillBuckets( buckets, kid );
    }

    
    
  }
  //-----------------------------------------------------------------------
  private final void addPB( IVecInt lits, IVec<BigInteger> coeff, boolean ge, BigInteger cardinality )
  throws ContradictionException
  {
    _solver.addPseudoBoolean( lits, coeff, ge, cardinality );
    
    System.out.print("PB: ");
    for( int i=0; i<lits.size(); i++ )
    {
      int co = Integer.parseInt( ""+coeff.get(i) );
      String sign = co < 0 ? "-" : "+";
      int    val = Math.abs(co);
      String space = val == 1 ? "" : " ";
      
      System.out.print( " " + sign + (val==1?"":val) + space  + "x"+lits.get(i) );
    }
    System.out.println(( ge ? " >= " : " < ")+" "+cardinality );
  }
  //-----------------------------------------------------------------------
  private final Map<ArtifactMetadata, List<MetadataTreeNode>> processChildren(
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
  private final void addNode( MetadataTreeNode node )
  throws ContradictionException, SatException
  {
    if( node == null )
      return;
    
    if( node.getMd() == null )
      throw new SatException("found a node without metadata");
    
    SatVar nodeLit = _context.findOrAdd( node.getMd() );

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
        SatVar kidLit = _context.findOrAdd( child.getMd() );
        
        addPB( SatHelper.getSmallOnes( new int [] { nodeLit.getLiteral(), kidLit.getLiteral() } )
                              , SatHelper.getBigOnes( -1, 1 )
                              , true, BigInteger.ZERO
                              );
        addNode( child );
      }

    }
  }
  //-----------------------------------------------------------------------
  private final int [] addRange( List<MetadataTreeNode> range, boolean optional )
  throws ContradictionException, SatException
  {
    SatVar literal;
    
    int [] literals = new int[ range.size() ];
    
    int count = 0;
    
    for( MetadataTreeNode tn : range )
    {
      literal = _context.findOrAdd( tn.getMd() );
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
  protected final static SatSolver create( int nVars )
  throws SatException
  {
    return new DefaultSatSolver( nVars );
  }
  
  protected DefaultSatSolver( int nVars )
  throws SatException
  {
    _context = new SatContext( nVars );
    _solver.newVar( nVars );
  }
  //-----------------------------------------------------------------------
  public final List<ArtifactMetadata> solve()
  throws SatException
  {
    List<ArtifactMetadata> res = null;
    
    try
    {
      if( _solver.isSatisfiable() )
      {
        res = new ArrayList<ArtifactMetadata>( _context.varCount );
        for( SatVar v : _context.variables )
        {
          boolean yes = _solver.model( v.getLiteral() );
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
