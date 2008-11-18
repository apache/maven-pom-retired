package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.metadata.MetadataTreeNode;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

 /**
  * This class hold all variables fed to the SAT solver. Because of the
  * tree unwinding algorithm all pivots (optional nodes) should be supplied first 
  * 
  * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
  */
class SatContext
{
  private static final Language _lang = new DefaultLanguage(SatContext.class);
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( SatContext.class );

  Map<MetadataTreeNode,SatVar> variables;
  //-----------------------------------------------------------------------
  public SatContext( int estimatedTreeSize )
  {
    variables = new HashMap<MetadataTreeNode,SatVar>( estimatedTreeSize );
  }
  //-----------------------------------------------------------------------
  public SatVar findOrAdd( MetadataTreeNode n )
  throws SatException
  {
    if( n == null )
      throw new SatException( "cannot create a literal out of a null metadata: "+n );
    
    SatVar var = variables.get( n );
    
    if( var != null )
    {
      if( _log.isDebugEnabled() )
        _log.debug( var.toString() );
      return var;
    }
    
    var = new SatVar( n );
    variables.put( n, var );
    
  if( _log.isDebugEnabled() )
    _log.debug( var.toString() );

    return var; 
  }
  //-----------------------------------------------------------------------
  public ArtifactMetadata getMd( int literal )
  {
    for( SatVar v : variables.values() )
      if( v._literal == literal )
        return v.getMd();
    
    return null;
  }
  //-----------------------------------------------------------------------
  private static final boolean isSolution( int m, int [] model )
  {
    for( int mm : model )
      if( mm == m )
        return true;
    
    return false;
  }
  //-----------------------------------------------------------------------
  public MetadataTreeNode getSolutionSubtree( MetadataTreeNode tree, int [] model )
  {
    if( tree == null )
      throw new IllegalArgumentException( _lang.getMessage( "null.tree" ) );
    
    if( tree.getMd() == null )
      throw new IllegalArgumentException( _lang.getMessage( "null.tree.md" ) );
    
    if( model == null )
      throw new IllegalArgumentException( _lang.getMessage( "null.model" ) );
    
    if( model.length < 1 )
      throw new IllegalArgumentException( _lang.getMessage( "empty.model" ) );
    
    int sz = 0;
    
    for( int m : model )
      if( m > 0 )
        ++sz;
      
    if( sz == 0)
      return null;
    
    MetadataTreeNode res = MetadataTreeNode.deepCopy( tree );
    
    cleanTree( res, model );
    
    return res;
  }
  //-----------------------------------------------------------------------
  private static final void cleanTree( MetadataTreeNode tn, int [] model )
  {
    if( ! tn.hasChildren() )
      return;
    
    List<MetadataTreeNode> badKids = new ArrayList<MetadataTreeNode>();
    
    for( MetadataTreeNode kid : tn.getChildren() )
      if( ! isSolution( kid.getId(), model ) )
        badKids.add( kid );
      
    tn.getChildren().removeAll( badKids );
      
    if( ! tn.hasChildren() )
      return;
      
    for( MetadataTreeNode kid : tn.getChildren() )
      cleanTree( kid, model );
  }
  //-----------------------------------------------------------------------
//  @Override
//  public String toString()
//  {
//    if(varCount < 1)
//      return "";
//    StringBuilder sb = new StringBuilder( 32*varCount );
//    char comma = '[';
//    
//    for( SatVar var : variables )
//    {
//      sb.append(comma+" x"+var._literal+"="+var._md.toString() );
//      comma = ',';
//    }
//    return sb.toString()+']';
//  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
