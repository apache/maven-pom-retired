package org.apache.maven.mercury.metadata.sat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.event.EventManager;
import org.apache.maven.mercury.event.EventTypeEnum;
import org.apache.maven.mercury.event.GenericEvent;
import org.apache.maven.mercury.event.MercuryEvent;
import org.apache.maven.mercury.event.MercuryEventListener;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.apache.maven.mercury.metadata.MetadataTreeNodeGAComparator;
import org.apache.maven.mercury.metadata.MetadataTreeNodeGAVComparator;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
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
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( DefaultSatSolver.class ); 
  private static final Language _lang = new DefaultLanguage( DefaultSatSolver.class );
  
  protected SatContext _context;
  protected IPBSolver _solver = SolverFactory.newEclipseP2();
  protected MetadataTreeNode _root;
  
  protected EventManager _eventManager;
  
  protected static final Comparator<MetadataTreeNode> gaComparator = new MetadataTreeNodeGAComparator();
  //-----------------------------------------------------------------------
  public static SatSolver create( MetadataTreeNode tree )
  throws SatException
  {
    return new DefaultSatSolver( tree );
  }
  //-----------------------------------------------------------------------
  public static SatSolver create( MetadataTreeNode tree, EventManager eventManager )
  throws SatException
  {
    return new DefaultSatSolver( tree, eventManager );
  }
  //-----------------------------------------------------------------------
  public DefaultSatSolver( MetadataTreeNode tree, EventManager eventManager )
  throws SatException
  {
    this._eventManager = eventManager;
    GenericEvent event = null;
    
    if( tree == null)
      throw new SatException("cannot create a solver for an empty [null] tree");
    
    try
    {
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.satSolver, EVENT_CREATE_SOLVER, tree.toString() );
        
      if( tree.getId() == 0 )
        MetadataTreeNode.reNumber( tree, 1 );
      
      int nNodes = tree.countDistinctNodes();
  
      _log.debug( "SatContext: # of variables: "+nNodes );
  
      _context = new SatContext( nNodes );
      _solver.newVar( tree.countNodes() );
      _root = tree;
      
      try
      {
        addNode( tree );
      }
      catch (ContradictionException e)
      {
        throw new SatException(e);
      }
    }
    finally
    {
      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
  }
  //-----------------------------------------------------------------------
  public DefaultSatSolver( MetadataTreeNode tree )
  throws SatException
  {
    this( tree, null );
  }
  //-----------------------------------------------------------------------
  public final void applyPolicies( List<Comparator<MetadataTreeNode>> comparators )
  throws SatException
  {
    if( comparators == null || comparators.size() < 1 )
      return;
    
    if( _root == null )
      throw new SatException( "cannot apply policies to a null tree" );
    
    // TODO og: assumption - around 128 GA's per tree. If more - map reallocates - slow down.
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
    
    IVecInt vars            = new VecInt( 128 );
    IVec<BigInteger> coeffs = new Vec<BigInteger>( 128 );
    
    int count = 0;
    
    for( String key : buckets.keySet() )
    {
      List<MetadataTreeNode> bucket = buckets.get( key );
  
// this is needed if optimization is "maximize"       
//      Collections.reverse(  bucket );
      
      int bucketSize = bucket.size(); 
      
      boolean bigBucket = bucketSize > 1;

if( _log.isDebugEnabled() )
  _log.debug( "\n\nBucket "+key );

      IVecInt bucketVars = new VecInt( bucketSize );
      
      for( int i=0; i<bucketSize; i++ )
      {
        MetadataTreeNode n  = bucket.get(i);

if( _log.isDebugEnabled() )
  _log.debug( n.toString() );

        SatVar var = _context.findOrAdd(n);
        int varLiteral = var.getLiteral(); 
        
        bucketVars.push( varLiteral );
        
        if( bigBucket )
        {
          vars.push( varLiteral );
          
          long cf = (long)Math.pow( 2, count++ );
          
          coeffs.push( BigInteger.valueOf( cf ) );

          if( _log.isDebugEnabled() )
            _log.debug( "    "+cf+" x"+var.getLiteral() );
        }

      }

      try
      {
        if( bucketVars != null && !bucketVars.isEmpty() )
        {
          _solver.addAtMost( bucketVars, 1 );
          _solver.addAtLeast( bucketVars, 1 );
        }
      }
      catch( ContradictionException e )
      {
        throw new SatException(e);
      }
      
if( _log.isDebugEnabled() )
  _log.debug( "\n" );
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

      // the best fit now first, and we don't need duplicate GAVs
      removeDuplicateGAVs( bucket );

    }
  }
  //-----------------------------------------------------------------------
  // remove duplicates, preserving the order. The first one is the most fit,
  // so need to delete from tail
  protected static final void removeDuplicateGAVs( List<MetadataTreeNode> bucket )
  {
    if( bucket == null || bucket.size() < 2 )
      return;

    Comparator<MetadataTreeNode> gav = new MetadataTreeNodeGAVComparator();
    
    int len = bucket.size();
    int [] dups = new int[ len-1 ];
    int cnt = 0;
    
    for( int i=1; i<len; i++ )
    {
      MetadataTreeNode ti = bucket.get(i);
      
      for( int j=0; j<i; j++ )
        if( gav.compare( ti, bucket.get(j) ) == 0 )
        {
          dups[cnt++] = i;
          break;
        }
    }
    
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
      buckets.put( ga, bucket );
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
    
if( _log.isDebugEnabled() )
  _log.debug("PB: ");
    
    for( int i=0; i<lits.size(); i++ )
    {
      int co = Integer.parseInt( ""+coeff.get(i) );
      String sign = co < 0 ? "-" : "+";
      int    val = Math.abs(co);
      String space = val == 1 ? "" : " ";
      
if( _log.isDebugEnabled() )
  _log.debug( " " + sign + (val==1?"":val) + space  + "x"+lits.get(i) );
    }
if( _log.isDebugEnabled() )
  _log.debug(( ge ? " >= " : " < ")+" "+cardinality );
  }
  //-----------------------------------------------------------------------
  private final Map<ArtifactBasicMetadata, List<MetadataTreeNode>> processChildren(
                                                        List<ArtifactBasicMetadata> queries
                                                        , List<MetadataTreeNode> children
                                                                              )
  throws SatException
  {
    if( queries == null || queries.size() < 1 )
      return null;
    
    if( children == null || children.size() < 1 )
      throw new SatException("there are queries, but not results. Queries: "+queries);
    
    HashMap<ArtifactBasicMetadata, List<MetadataTreeNode>> res = new HashMap<ArtifactBasicMetadata, List<MetadataTreeNode>>( queries.size() );
    for( ArtifactBasicMetadata q : queries )
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
    
    SatVar nodeLit = _context.findOrAdd( node );

    // this one is a must :)
    if( node.getParent() == null )
      addPB( SatHelper.getSmallOnes( nodeLit.getLiteral() ), SatHelper.getBigOnes(1,false), true, BigInteger.ONE );
    
    if( ! node.hasChildren() )
      return;
    
    Map<ArtifactBasicMetadata,List<MetadataTreeNode>> kids = processChildren( node.getQueries(), node.getChildren() );
    
    // leaf node in this scope
    if( kids == null )
      return;
    
    for( Map.Entry<ArtifactBasicMetadata,List<MetadataTreeNode>> kid : kids.entrySet() )
    {
      ArtifactBasicMetadata query = kid.getKey();
      
      List<MetadataTreeNode> range = kid.getValue();

      if( range.size() > 1 )
      {
        addRange( nodeLit.getLiteral(), range, query.isOptional() );
        for( MetadataTreeNode tn : range )
        {
          addNode( tn );
        }
      }
      else
      {
        MetadataTreeNode child = range.get(0);
        SatVar kidLit = _context.findOrAdd( child );
        
        addPB( SatHelper.getSmallOnes( new int [] { nodeLit.getLiteral(), kidLit.getLiteral() } )
            , SatHelper.getBigOnes( 1, -1 )
            , true, BigInteger.ZERO
            );
//        addRange( nodeLit.getLiteral(), range, query.isOptional() );
        addNode( child );
      }

    }
  }
  //-----------------------------------------------------------------------
  private final int [] addRange( int parentLiteral, List<MetadataTreeNode> range, boolean optional )
  throws ContradictionException, SatException
  {
    SatVar literal;
    
    int [] literals = new int[ range.size() ];
    
    int count = 0;
    
    for( MetadataTreeNode tn : range )
    {
      literal = _context.findOrAdd( tn );
      literals[count++] = literal.getLiteral();

      // implication to parent
      addPB( SatHelper.getSmallOnes( new int [] { parentLiteral, literal.getLiteral() } )
          , SatHelper.getBigOnes( 1, -1 )
          , true, BigInteger.ZERO
          );
    }
    
    IVecInt rangeVector = SatHelper.getSmallOnes( literals );
    
    if( optional ) // Sxi >= 0
    {
if( _log.isDebugEnabled() )
  _log.debug( "optional range: atMost 1: "+ SatHelper.vectorToString( rangeVector) );
    
      _solver.addAtMost( rangeVector, 1 );
    }
    else // Sxi = 1
    {
if( _log.isDebugEnabled() )
  _log.debug( "range: " + SatHelper.vectorToString( rangeVector) );

    IConstr atLeast = _solver.addAtLeast( rangeVector, 1 );
    if( _log.isDebugEnabled() )
      _log.debug( "atLeast: " + SatHelper.vectorToString( atLeast) );

    IConstr atMost  = _solver.addAtMost( rangeVector, 1 );
    if( _log.isDebugEnabled() )
      _log.debug( "atMost: " + SatHelper.vectorToString( atMost) );

    }
    
    return literals;
  }
  //-----------------------------------------------------------------------
  public final List<ArtifactMetadata> solve()
  throws SatException
  {
    List<ArtifactMetadata> res = null;
    GenericEvent event = null;
    
    try
    {
      if( _eventManager != null )
        event = new GenericEvent( EventTypeEnum.satSolver, EVENT_SOLVE, _root.toString() );
      
      if( _solver.isSatisfiable() )
      {
        res = new ArrayList<ArtifactMetadata>( _root.countNodes() );
        
        int [] model = _solver.model();

if( _log.isDebugEnabled() )
  if( model != null )
  {
    StringBuilder sb = new StringBuilder();
    String comma = "";
    for( int m : model )
    {
      sb.append( comma+m );
      comma = ", ";
    }
    _log.debug( '['+sb.toString()+']' );
  }
  else 
    _log.debug( "model is null" );

        for( int i : model )
          if( i > 0 )
            res.add( _context.getMd( i ) );
      }
    }
    catch (TimeoutException e)
    {
      throw new SatException( e );
    }
    finally
    {
      if( _eventManager != null )
      {
        event.stop();
        _eventManager.fireEvent( event );
      }
    }
    return res;
  }
  //-----------------------------------------------------------------------
  public final MetadataTreeNode solveAsTree()
  throws SatException
  {
    try
    {
      if( _solver.isSatisfiable() )
      {
        int [] model = _solver.model();

if( _log.isDebugEnabled() )
  if( model != null )
  {
    StringBuilder sb = new StringBuilder();
    String comma = "";
    for( int m : model )
    {
      sb.append( comma+m );
      comma = ", ";
    }
    _log.debug( '['+sb.toString()+']' );
  }
  else 
    _log.debug( "model is null" );

        return _context.getSolutionSubtree( _root, model );
      }
      return null;
    }
    catch (TimeoutException e)
    {
      throw new SatException( e );
    }
    
  }
  //-----------------------------------------------------------------------
  public void register( MercuryEventListener listener )
  {
    if( _eventManager == null )
      _eventManager = new EventManager();

    _eventManager.register( listener );
  }

  public void setEventManager( EventManager eventManager )
  {
    _eventManager = eventManager;
  }

  public void unRegister( MercuryEventListener listener )
  {
    if( _eventManager != null )
      _eventManager.unRegister( listener );
  }
  //-----------------------------------------------------------------------
}
