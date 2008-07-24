package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.ArtifactMetadata;

 /**
  * This class hold all variables fed to the SAT solver. Because of the
  * tree unwinding algorithm all pivots (optional nodes) should be supplied first 
  * 
  * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
  */
class SatContext
{
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
System.out.println(md+" -> x"+var._literal);
          return var;
        }
      }
    }
    
    SatVar var = new SatVar( md, ++varCount );
    variables.add( var );
    
System.out.println(md+" -> x"+var._literal);
    return var; 
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
