package org.apache.maven.mercury.metadata.sat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

 /**
  * This class hold all variables fed to the SAT solver. Because of the
  * tree unwinding algorithm all pivots (optional nodes) should be supplied first 
  * 
  * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
  */
class SatContext
{
  public static final boolean STRONG_VAR = false;
  public static final boolean WEAK_VAR = true;
  
  List<SatVar> variables;
  int varCount = 0;
  boolean pivotsFinished = false;
  //-----------------------------------------------------------------------
  public SatContext(int estimatedTreeSize )
  {
    variables = new ArrayList<SatVar>( estimatedTreeSize );
  }
  //-----------------------------------------------------------------------
  public SatVar addWeak( ArtifactMetadata md )
  throws SatException
  {
    if( pivotsFinished )
      throw new SatException( "cannot mix Pivots and OR Groups. Pivots are always first." );

    if( md == null )
      throw new SatException( "cannot create a literal out of a null metadata: "+md );

    for( SatVar var : variables )
    {
      {
        if( ! var._optional )
          throw new SatException( "Literal x"+var._no+" ia already marked as strong. Cannot add it as weak." );

        if( var._md.sameGAV(md) )
          return var;
      }
    }
    
    SatVar var = new SatVar( md, ++varCount, WEAK_VAR );
    variables.add( var );
    
    return var; 
  }
  //-----------------------------------------------------------------------
  public SatVar addStrong( ArtifactMetadata md )
  throws SatException
  {

    if( md == null )
      throw new SatException( "cannot create a literal out of a null metadata: "+md );

    // force-finish pivots on the first non-pivot
    pivotsFinished = true;
    
    for( SatVar var : variables )
    {
      if( var.compareTo(md) == 0 )
      {
        if( var._optional )
          throw new SatException( "Literal x"+var._no+" ia already marked as weak. Cannot add it as strong." );

        if( var._md.sameGAV(md) )
          return var;
      }
    }
    
    SatVar var = new SatVar( md, ++varCount, STRONG_VAR );
    variables.add( var );
    
    return var; 
  }
  //-----------------------------------------------------------------------
  private SatVar findOrAdd( ArtifactMetadata md, boolean optional )
  throws SatException
  {
    for( SatVar var : variables )
    {
      if( var.compareTo(md) == 0 )
        return var;
    }
    
    SatVar var = new SatVar( md, ++varCount, optional );
    variables.add( var );
    
    return var; 
  }
  //-----------------------------------------------------------------------
  public SatVar find( ArtifactMetadata md )
  {
    for( SatVar var : variables )
    {
      if( var.compareTo(md) == 0 )
        return var;
    }
    
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
      sb.append(comma+" "+(var.isWeak()?'w':'s')+var._no+"="+var._md.toString() );
      comma = ',';
    }
    return sb.toString()+']';
  }
  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
}
