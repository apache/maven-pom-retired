package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

 /**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
class SatContext
{
  List<SatVar> variables;
  int varCount = 0;
  //-----------------------------------------------------------------------
  public SatContext(int estimatedTreeSize )
  {
    variables = new ArrayList<SatVar>( estimatedTreeSize );
  }
  //-----------------------------------------------------------------------
  public SatVar find( ArtifactMetadata md )
  throws SatException
  {
    for( SatVar var : variables )
    {
      if( var.compareTo(md) == 0 )
        return var;
    }
    
    SatVar var = new SatVar( md, ++varCount );
    variables.add( var );
    
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
      sb.append(comma+" x"+var._no+" > "+var._md.toString() );
      comma = ',';
    }
    return sb.toString()+']';
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
