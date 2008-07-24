package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.ArtifactScopeEnum;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.apache.maven.mercury.metadata.MetadataTreeNodeGAComparator;
import org.apache.maven.mercury.metadata.MetadataTreeNodeGAVComparator;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
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
  protected ArtifactScopeEnum _scope;
  protected static final Comparator<MetadataTreeNode> gaComparator = new MetadataTreeNodeGAComparator();
  //-----------------------------------------------------------------------
  public static SatSolver create( MetadataTreeNode tree, ArtifactScopeEnum scope )
  throws SatException
  {
    return new DefaultSatSolver( tree, scope );
  }
  //-----------------------------------------------------------------------
  public DefaultSatSolver( MetadataTreeNode tree, ArtifactScopeEnum scope )
  throws SatException
  {
    if( tree == null)
      throw new SatException("cannot create a solver for an empty [null] tree");
    
    int nNodes = tree.countNodes();
    _context = new SatContext( nNodes );
    _solver.newVar( _context.varCount );
    _root = tree;
    _scope = scope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : scope;
    
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
    
    if( _root == null )
      throw new SatException( "cannot apply policies to a null tree" );
    
    // TODO og: assumption - around 128 GA's per tree
    Map<String, List<MetadataTreeNode>> buckets = new HashMap<String, List<MetadataTreeNode>>(128);
    fillBuckets( buckets, _root );
    sortBuckets( buckets, comparators );
    useBuckets( buckets );
  }
  //-----------------------------------------------------------------------
  private void useBuckets( Map<String, List<MetadataTreeNode>> buckets )
  throws SatException
  {
    if( buckets == null || buckets.size() < 1 )
      return;
    
    IVecInt vars = new VecInt( 128 );
    IVec<BigInteger> coeffs = new Vec<BigInteger>( 128 );

    for( List<MetadataTreeNode> bucket : buckets.values() )
    {
      if( bucket.size() < 2 )
        continue;
      
      for( int i=0; i<bucket.size(); i++ )
      {
        MetadataTreeNode n  = bucket.get(i);
        ArtifactMetadata md = n.getMd();
        SatVar var = _context.findOrAdd(md);
        vars.push( var.getLiteral() );
        coeffs.push( BigInteger.valueOf( (long)Math.pow( 2, i ) ) );
      }
    }
    
    if( vars.isEmpty() )
      return;
    
    _solver.setObjectiveFunction( new ObjectiveFunction( vars, coeffs ) );
  }
  //-----------------------------------------------------------------------
  protected static final void sortBuckets(
        Map<String, List<MetadataTreeNode>> buckets
      , List<Comparator<MetadataTreeNode>> comparators
                                          )
  {
    Comparator<MetadataTreeNode> lastComparator;
    for( List<MetadataTreeNode> bucket : buckets.values() )
    {
      lastComparator = gaComparator;
      for( Comparator<MetadataTreeNode> comparator : comparators )
      {
        sortBucket( bucket, comparator, lastComparator );
        lastComparator = comparator;
      }
      // due to the nature of Comparator need to reverse the result
      // as the best fit is now last
      Collections.reverse( bucket );

      // we don't need duplicate GAVs
      removeDuplicateGAVs( bucket );
 
    }
  }
  //-----------------------------------------------------------------------
  private static final void removeDuplicateGAVs(List<MetadataTreeNode> bucket)
  {
    if( bucket == null || bucket.size() < 2 )
      return;

    Comparator<MetadataTreeNode> gav = new MetadataTreeNodeGAVComparator();
    
    int len = bucket.size();
    int [] dups = new int[ len-1 ];
    int cnt = 0;
    
    for( int i=1; i<len; i++ )
      for( int j=0; j<i; j++ )
        if( gav.compare( bucket.get(i), bucket.get(j) ) == 0 )
          dups[cnt++] = i;
    
    if( cnt > 0 )
      for( int i=0; i<cnt; i++ )
        bucket.remove( dups[cnt-i-1] );
  }
  //-----------------------------------------------------------------------
  /**
   * reorders the bucket's lastComparator equal subsets with comparator.
   */
  protected static final void sortBucket(
               List<MetadataTreeNode> bucket
               , Comparator<MetadataTreeNode> comparator
               , Comparator<MetadataTreeNode> lastComparator
               )
  {
    if( bucket == null || bucket.size() < 2 )
      return;
    
    int bLen = bucket.size();
    MetadataTreeNode [] temp = bucket.toArray( new MetadataTreeNode[ bLen ] );
    
    int wStart = -1;
    int wLen = 0;
    MetadataTreeNode [] work = new MetadataTreeNode[ bLen ];
    
    MetadataTreeNode lastNode = null;
    for( int i=0; i<bLen; i++ )
    {
      MetadataTreeNode n = temp[i];
      
      if( lastNode == null )
      {
        lastNode = n;
        continue;
      }
      
      if( lastComparator.compare(lastNode, n) == 0 )
      {
        if( wLen == 0 )
        {
          work[ wLen++ ] = lastNode;
          wStart = i-1;
        }
        
        work[ wLen++ ] = n;

        lastNode = n;

        if( i < (bLen-1) )
          continue;
      }
      
      if( wLen > 0 ) // eq list formed
      {
        reorder( work, wLen, comparator );
        for( int j=0; j<wLen; j++ )
          temp[ wStart+j ] = work[ j ];
        wLen = 0;
        wStart = -1;
      }
      
      lastNode = n;
    }
    
    bucket.clear();
    for( int i=0; i<bLen; i++ )
      bucket.add( temp[ i ] );
  }
  //-----------------------------------------------------------------------
  private static final void reorder(
      MetadataTreeNode[] work
      , int wLen
      , Comparator<MetadataTreeNode> comparator
                     )
  {
    MetadataTreeNode[] temp = new MetadataTreeNode[ wLen ];
    
    for( int i=0; i< wLen; i++ )
      temp[i] = work[i];
    
    Arrays.sort( temp, comparator );
    
    for( int i=0; i<wLen; i++ )
      work[i] = temp[i];
    
  }
  //-----------------------------------------------------------------------
  protected static final void fillBuckets(
        Map<String, List<MetadataTreeNode>> buckets
      , MetadataTreeNode node
                          )
  {
    String ga = node.getMd().getGA();
    List<MetadataTreeNode> bucket = buckets.get(ga);
    if( bucket == null )
    {
      // TODO og: assumption - around 32 GAVs per GA
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
    if( queries == null || queries.size() < 1 )
      return null;
    
    if( children == null || children.size() < 1 )
      throw new SatException("there are queries, but not results. Queries: "+queries);
    
    HashMap<ArtifactMetadata, List<MetadataTreeNode>> res = new HashMap<ArtifactMetadata, List<MetadataTreeNode>>( queries.size() );
    for( ArtifactMetadata q : queries )
    {
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
      if( ! _scope.encloses( query.getArtifactScope()) )
        continue;
      
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
