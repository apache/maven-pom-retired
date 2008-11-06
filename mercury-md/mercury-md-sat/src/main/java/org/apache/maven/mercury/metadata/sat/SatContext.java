package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

 /**
  * This class hold all variables fed to the SAT solver. Because of the
  * tree unwinding algorithm all pivots (optional nodes) should be supplied first 
  * 
  * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
  */
class SatContext
{
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( SatContext.class );

  Map<MetadataTreeNode,SatVar> variables;
  int varCount = 0;
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

    
    var = new SatVar( n, ++varCount );
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
