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
  boolean pivot = false;
  //----------------------------------------------------------------------------
  public SatConstraint( List<ArtifactMetadata> mdl, SatContext context, boolean weak )
  throws SatException
  {
    if( mdl == null || mdl.size() < 1 )
      throw new SatException("cannot create constraint out of null or empty branch: "+mdl);
    
    variables = new HashMap<Integer,Integer>( mdl.size() );
    
    SatVar var;
    for( ArtifactMetadata md : mdl )
    {
      var = context.find(md);

      if( var == null )
      {
        if( weak )
          var = context.addWeak( md );
        else
          var = context.addStrong( md );
      }

      add( var );
    }
  }
  //----------------------------------------------------------------------------
  public void addOrGroupMember( List<ArtifactMetadata> branch, SatContext context )
  throws SatException
  {
    for( ArtifactMetadata md : branch )
    {
      SatVar var = context.find(md);
      
      if( var == null )
        var = context.addStrong(md);
      
      add( var );
    }
  }
  //----------------------------------------------------------------------------
  /**
   * @return new coeff of a given literal
   */
  private int add( SatVar var )
  {
    Integer varNo = new Integer( var.getNo() );

    if( variables.containsKey(varNo) )
    {
// TODO looks like after all we don't have to increment them.
      int res = variables.get( varNo ).intValue();

      // only strong can survive
      if( ! var.isWeak() )
        variables.put( varNo, new Integer(++res) );
      
      return res;
    }

    variables.put( varNo, new Integer(1) );
    return 1;
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
    return sb.toString()+" >= "+cardinality;
  }
  //----------------------------------------------------------------------------
  /**
   * @param literal no
   * @return new coeff of a given literal
   * @throws SatException if the literal was not found
   * 
   */
  private int increment( SatVar var )
  throws SatException
  {
    if( var == null )
      throw new SatException("Cannot increment a null variable");

    Integer varNo = new Integer( var.getNo() );

    if( variables.containsKey(varNo) )
    {
      int res = variables.get( varNo ).intValue() + 1;
      variables.put( varNo, new Integer(res) );
      
      return res;
    }
    
    throw new SatException("Literal "+varNo+" not found");
  }
  //----------------------------------------------------------------------------
  /**
   * increment the given strong variable to adjust to the max length of a group
   * 
   * @param mdl branch - member of an OR group
   * @param maxlen - length of every branch to adjust to
   * @throws SatException - if first element is not strong. Should never happen
   *  because of the tree nature of our problem 
   */
  public void adjust( List<ArtifactMetadata> branch, int maxLen, int minWeekness, SatContext context )
  throws SatException
  {
    if( branch == null || branch.size() < 1 )
      throw new SatException( "Cannot adjust an empty branch: "+branch );
    
    if( maxLen < 1 )
      throw new SatException( "Max length cannot be less than zero. It is: " + maxLen );
    
    if( branch.size() > maxLen )
      throw new SatException( "Maximum branch length "+maxLen+" cannot be less than current branch length "+branch.size() );
    
    ArtifactMetadata md = branch.get(0);
    if( md == null )
      throw new SatException( "Cannot start a branch with a null metadata: "+md );
    
    SatVar strong = context.find( md );
    if( strong == null )
      throw new SatException( "Cannot start a branch with a null variable: "+strong );
    
    if( strong.isWeak() )
      throw new SatException( "Cannot start a branch with a weak variable: "+strong );
    
    int nWeak = 0;
    
    for( ArtifactMetadata amd : branch )
    {
      SatVar lit = context.find(amd);
      if( lit.isWeak() )
        ++nWeak;
    }

    // and now - why we are here :)
    int adjustBy = (nWeak - minWeekness) + ( maxLen - branch.size() );

System.out.println("Adjusting branch "+branch+" ("+md+") by "+adjustBy+", ctx: "+context.toString());

    while( adjustBy-- > 0 )
      increment(strong);
  }
  public void finalAdjust( List<ArtifactMetadata> branch, int groupSize, int minWeekness, SatContext context )
  throws SatException
  {
    if( groupSize == 1 )
      return;
    
    if( branch == null || branch.size() < 1 )
      throw new SatException( "Cannot adjust an empty branch: "+branch );
    
    ArtifactMetadata md = branch.get(0);
    if( md == null )
      throw new SatException( "Cannot start a branch with a null metadata: "+md );
    
    SatVar strong = context.find( md );
    if( strong == null )
      throw new SatException( "Cannot start a branch with a null variable: "+strong );
    
    if( strong.isWeak() )
      throw new SatException( "Cannot start a branch with a weak variable: "+strong );
    // and now - why we are here :)

    int adjustBy = (groupSize-1)* minWeekness;

System.out.println("Finally adjusting branch "+branch+" ("+md+") by "+adjustBy+", ctx: "+context.toString());

    while( adjustBy-- > 0 )
      increment(strong);
  }
  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
}
