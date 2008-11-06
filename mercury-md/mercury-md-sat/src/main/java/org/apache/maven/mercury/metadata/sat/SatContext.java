package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;

 /**
  * This class hold all variables fed to the SAT solver. Because of the
  * tree unwinding algorithm all pivots (optional nodes) should be supplied first 
  * 
  * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
  */
class SatContext
{
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( SatContext.class );

  List<SatVar> variables;
  int varCount = 0;
  //-----------------------------------------------------------------------
  public SatContext( int estimatedTreeSize )
  {
    variables = new ArrayList<SatVar>( estimatedTreeSize );
  }
  //-----------------------------------------------------------------------
  public SatVar findOrAdd( ArtifactMetadata md )
  throws SatException
  {
    if( md == null )
      throw new SatException( "cannot create a literal out of a null metadata: "+md );

    for( SatVar var : variables )
    {
      {
        if( var._md.sameGAV(md) )
        {
if( _log.isDebugEnabled() )
  _log.debug(md+" -> x"+var._literal);
          return var;
        }
      }
    }
    
    SatVar var = new SatVar( md, ++varCount );
    variables.add( var );
    
if( _log.isDebugEnabled() )
  _log.debug(md+" -> x"+var._literal);

    return var; 
  }
  //-----------------------------------------------------------------------
  public ArtifactMetadata getMd( int literal )
  {
    for( SatVar v : variables )
      if( v._literal == literal )
        return v.getMd();
    
    return null;
  }
  //-----------------------------------------------------------------------
  @Override
  public String toString()
  {
    if(varCount < 1)
      return "";
    StringBuilder sb = new StringBuilder( 32*varCount );
    char comma = '[';
    
    for( SatVar var : variables )
    {
      sb.append(comma+" x"+var._literal+"="+var._md.toString() );
      comma = ',';
    }
    return sb.toString()+']';
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
