package org.apache.maven.mercury.metadata.sat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

/**
  * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
  */
public class SatConstraint
{
  Map<Integer,Integer> variables;
  int cardinality = 0;
  boolean ge = true;
  //----------------------------------------------------------------------------
  public SatConstraint( List<ArtifactMetadata> mdl, SatContext context )
  throws SatException
  {
    if( mdl == null || mdl.size() < 1 )
      throw new SatException("cannot create constraint out of null or empty branch: "+mdl);
    
    variables = new HashMap<Integer,Integer>( mdl.size() );
    
    for( ArtifactMetadata md : mdl )
    {
      SatVar var = context.find( md );
      add( var );
    }
  }
  //----------------------------------------------------------------------------
  private void add( SatVar var )
  {
    Integer varNo = new Integer( var.getNo() );

    // TODO ?? check if this does not break the solver
    if( variables.containsKey(varNo) )
      return;
    
    variables.put( varNo, new Integer(1) );
    ++cardinality;
  }
  //----------------------------------------------------------------------------
  public void addOr(List<ArtifactMetadata> branch, SatContext context )
  throws SatException
  {
    for( ArtifactMetadata md : branch )
    {
      SatVar var = context.find( md );
      add( var );
    }
  }
  //----------------------------------------------------------------------------
  public SatClause getClause()
  throws SatException
  {
    if( variables == null || variables.size() < 1 )
      throw new SatException("cannot create int array - no variables in the constraint");
    
    SatClause sc = new SatClause( variables.size() );
    for( Map.Entry<Integer, Integer> e : variables.entrySet() )
    {
      sc.add( e.getKey(), e.getValue() );
    }
 
    return sc;
  }
  //----------------------------------------------------------------------------
  public int [] getVarray()
  throws SatException
  {
    if( variables == null || variables.size() < 1 )
      throw new SatException("cannot create int array - no variables in the constraint");
    
    int [] res = new int[ variables.size() ];
    int ptr = 0;
    for( Integer v : variables.keySet() )
    {
      res[ ptr++ ] = v.intValue();
    }
 
    return res;
  }
  //----------------------------------------------------------------------------
  public SatClause getNegatedClause()
  throws SatException
  {
    if( variables == null || variables.size() < 1 )
      throw new SatException("cannot create int array - no variables in the constraint");
    
    SatClause sc = new SatClause( variables.size() );
    for( Map.Entry<Integer, Integer> e : variables.entrySet() )
    {
      e.setValue( new Integer( -e.getValue().intValue() ) );
      sc.add( e.getKey(), e.getValue() );
    }
 
    return sc;
  }
  //----------------------------------------------------------------------------
  @Override
  public String toString()
  {
    if( variables == null || variables.size() < 1 )
      return "null";
    
    StringBuilder sb = new StringBuilder(128);
    SatClause sc = new SatClause( variables.size() );
    for( Map.Entry<Integer, Integer> e : variables.entrySet() )
    {
      sb.append( " +"+e.getValue()+"*x"+e.getKey() );
    }
    return sb.toString();
  }
  
  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
}
